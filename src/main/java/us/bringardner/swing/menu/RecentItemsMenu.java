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
import java.util.List;
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
 * The list itself is a {@link RecentItems}, which uses no UI toolkit; fx-widgets' JavaFX
 * RecentItemsMenu uses it too. A {@link Codec} turns each item into one line of text and back.
 * Items are the same entry when they are equal ({@link Object#equals(Object)}).
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
	public interface Codec<T> extends RecentItems.Codec<T> {
	}

	private static final long serialVersionUID = 1L;

	/** The list: one encoded item per line, newest first. */
	public static final String PREF_RECENT_LIST = RecentItems.PREF_RECENT_LIST;
	public static final String PREF_MAX_ITEMS = RecentItems.PREF_MAX_ITEMS;
	public static final int DEFAULT_MAX_ITEMS = RecentItems.DEFAULT_MAX_ITEMS;

	private final RecentItems<T> list;

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
		list = new RecentItems<>(prefs, codec);
		buildMenu();
	}

	/** @return the list the menu shows */
	public RecentItems<T> getRecentItems() {
		return list;
	}

	/** @return the entries, newest first */
	public List<T> getItems() {
		return list.getItems();
	}

	/** Replaces the list. Duplicates are left out. */
	public void setItems(List<T> items) throws IOException {
		list.setItems(items);
		buildMenu();
	}

	/**
	 * Puts an item at the top of the list. If the list already has it, the old entry is
	 * replaced by {@link #merge(Object, Object)}. The oldest entries over the maximum are dropped.
	 */
	public void addItem(T item) throws IOException {
		list.add(item, this::merge);
		buildMenu();
	}

	public int getMaxItems() {
		return list.getMaxItems();
	}

	/** Sets the maximum number of entries. It applies the next time an item is added. */
	public void setMaxItems(int max) throws IOException {
		list.setMaxItems(max);
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
				list.clear();
				buildMenu();
			} catch (IOException e1) {
				showError("Can't clear the recent list",e1);
			}
		});
		add(item);

		list.removeIf(this::isStale);

		for(T entry : list.getItems()) {
			item = new JMenuItem(getLabel(entry));
			item.addActionListener((e)->open(entry, e));
			add(item);
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
