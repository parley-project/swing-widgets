/**
 * <PRE>
 *
 * Copyright 1998-2026 <A href="http://bringardner.us/tony">Tony Bringardner</A>
 *
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       <A href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</A>
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  </PRE>
 *
 *
 *	@author Tony Bringardner
 */
package us.bringardner.swing.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * A "Recent ..." menu, newest first, saved with java.util.prefs.Preferences.
 * <p>
 * The menu starts with an item to set the maximum number of entries and one to clear
 * the list, followed by one item per entry. Choosing an entry calls {@link #openItem(Object)},
 * moves the entry to the top and passes the event to the menu's ActionListeners with the
 * value openItem returned as its source.
 * <p>
 * A {@link Codec} turns each item into one line of text and back. Items are the same
 * entry when they are equal ({@link Object#equals(Object)}).
 * <p>
 * Subclasses can change the label shown for an item ({@link #getLabel(Object)}), drop items
 * that no longer exist ({@link #isStale(Object)}), and keep state from the entry an item
 * replaces ({@link #merge(Object, Object)}). The list is read and the menu is built by the
 * constructor, so these methods are called before a subclass's fields are set; they should
 * not use them.
 *
 * @param <T> the type of item in the list
 */
public class RecentItemsMenu<T> extends JMenu {

	/** Turns an item into a single line of text and back. */
	public interface Codec<T> {
		/** @return the item as one line; it must not contain a line break */
		String encode(T item);

		/** @return the item a line holds; throw a RuntimeException if it's malformed, and the line is skipped */
		T decode(String line);
	}

	private static final long serialVersionUID = 1L;
	private static final String NL = "\n";

	/** The list: one encoded item per line, newest first. */
	public static final String PREF_RECENT_LIST = "RecentFiles";
	public static final String PREF_MAX_ITEMS = "MaxRecentFiles";
	public static final int DEFAULT_MAX_ITEMS = 10;

	private final Preferences prefs;
	private final Codec<T> codec;
	private List<T> items;
	private int maxItems = -1;

	/**
	 * Reads the list from the preferences node and builds the menu.
	 *
	 * @param title the menu's text, e.g. "Recent Files"
	 * @param prefs where the list is saved
	 * @param codec turns items into text and back
	 * @throws IOException if the list can't be saved after dropping stale items
	 */
	public RecentItemsMenu(String title, Preferences prefs, Codec<T> codec) throws IOException {
		super(title);
		this.prefs = Objects.requireNonNull(prefs, "prefs");
		this.codec = Objects.requireNonNull(codec, "codec");
		buildMenu();
	}

	/** @return the entries, newest first */
	public List<T> getItems() {
		return Collections.unmodifiableList(items);
	}

	/** Replaces the list. Duplicates are left out. */
	public void setItems(List<T> list) throws IOException {
		List<T> ret = new ArrayList<>();
		for(T item : list) {
			if( !ret.contains(item) ) {
				ret.add(item);
			}
		}
		items = ret;
		store();
		buildMenu();
	}

	/**
	 * Puts an item at the top of the list. If the list already has it, the old entry is
	 * replaced by {@link #merge(Object, Object)}. The oldest entries over the maximum are dropped.
	 */
	public void addItem(T item) throws IOException {
		int idx = items.indexOf(item);
		if( idx >=0 ) {
			item = merge(item, items.remove(idx));
		}
		items.add(0, item);

		int mx = getMaxItems();
		while(items.size()>mx && !items.isEmpty()) {
			items.remove(items.size()-1);
		}

		store();
		buildMenu();
	}

	public int getMaxItems() {
		if( maxItems < 0 ) {
			maxItems = prefs.getInt(PREF_MAX_ITEMS, DEFAULT_MAX_ITEMS);
		}
		return maxItems;
	}

	/** Sets the maximum number of entries. It applies the next time an item is added. */
	public void setMaxItems(int max) throws IOException {
		maxItems = max;
		prefs.putInt(PREF_MAX_ITEMS, max);
		flush();
	}

	/** @return the text shown for an item. The default is its toString(). */
	protected String getLabel(T item) {
		return String.valueOf(item);
	}

	/** @return true if the item no longer exists and should be dropped. The default is false. */
	protected boolean isStale(T item) {
		return false;
	}

	/**
	 * Called by {@link #addItem(Object)} when the list already has the item.
	 *
	 * @param newer the item being added
	 * @param older the entry it replaces
	 * @return the entry to keep. The default is newer.
	 */
	protected T merge(T newer, T older) {
		return newer;
	}

	/**
	 * Called when the user chooses an entry.
	 *
	 * @return the source of the event passed to the ActionListeners, or null to do nothing.
	 *         The default is the item.
	 * @throws IOException shown to the user as an error
	 */
	protected Object openItem(T item) throws IOException {
		return item;
	}

	/** @return the text of the item that sets the maximum. */
	protected String getMaxItemsText(int max) {
		return "Max Items:"+max;
	}

	/** Shows an error to the user. */
	protected void showError(String message, Exception e) {
		JOptionPane.showMessageDialog(this, e, message, JOptionPane.ERROR_MESSAGE);
	}

	private List<T> read() {
		List<T> ret = new ArrayList<>();
		String text = prefs.get(PREF_RECENT_LIST, null);
		if( text != null ) {
			for(String line : text.split(NL)) {
				if( line.isEmpty() ) {
					continue;
				}
				try {
					T item = codec.decode(line);
					if( item != null && !ret.contains(item) ) {
						ret.add(item);
					}
				} catch (RuntimeException e) {
					// skip a malformed line rather than lose the list
				}
			}
		}

		return ret;
	}

	private void store() throws IOException {
		StringBuilder buf = new StringBuilder();
		for(T item : items) {
			String line = codec.encode(item)+NL;
			// a preference value is limited in length; the oldest entries that don't fit are dropped
			if( buf.length()+line.length() > Preferences.MAX_VALUE_LENGTH ) {
				break;
			}
			buf.append(line);
		}
		prefs.put(PREF_RECENT_LIST, buf.toString());
		flush();
	}

	private void flush() throws IOException {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new IOException("Can't save preferences", e);
		}
	}

	private void buildMenu() throws IOException {
		removeAll();
		final int mx = getMaxItems();
		JMenuItem item = new JMenuItem(getMaxItemsText(mx));
		item.addActionListener((e)->{
			String tmp = JOptionPane.showInputDialog("Max Items", mx);
			if( tmp != null ) {
				try {
					setMaxItems(Integer.parseInt(tmp.trim()));
					buildMenu();
				} catch (NumberFormatException e2) {
					// not a number: unchanged
				} catch (IOException e2) {
					showError("Can't save the maximum", e2);
				}
			}
		});
		add(item);
		item = new JMenuItem("Clear Recent List");
		item.addActionListener((e)->{
			try {
				setItems(new ArrayList<T>());
			} catch (IOException e1) {
				showError("Can't clear the recent list",e1);
			}
		});
		add(item);

		if( items == null ) {
			items = read();
		}

		boolean changed = items.removeIf(this::isStale);

		for(T entry : items) {
			item = new JMenuItem(getLabel(entry));
			item.addActionListener((e)->open(entry, e));
			add(item);
		}

		if( changed ) {
			store();
		}
	}

	private void open(T entry, ActionEvent e) {
		Object source;
		try {
			source = openItem(entry);
		} catch (IOException | RuntimeException e1) {
			showError("Can't open "+getLabel(entry), e1);
			return;
		}
		if( source == null ) {
			return;
		}
		try {
			addItem(entry);
		} catch (IOException e1) {
			showError("Can't save the recent list", e1);
		}
		e.setSource(source);
		for(ActionListener l : getActionListeners()) {
			l.actionPerformed(e);
		}
	}
}
