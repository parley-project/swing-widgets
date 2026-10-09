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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A list of recently used items, newest first, saved with java.util.prefs.Preferences.
 * It uses no UI toolkit: {@link RecentItemsMenu} shows it as a Swing menu, and fx-widgets'
 * RecentItemsMenu as a JavaFX one. Both read and write the same preferences, so they share a list.
 * <p>
 * A {@link Codec} turns each item into one line of text and back. Items are the same entry
 * when they are equal ({@link Object#equals(Object)}).
 *
 * @param <T> the type of item in the list
 */
public class RecentItems<T> {

	/** Turns an item into a single line of text and back. */
	public interface Codec<T> {
		/** @return the item as one line; it must not contain a line break */
		String encode(T item);

		/** @return the item a line holds; throw a RuntimeException if it's malformed, and the line is skipped */
		T decode(String line);
	}

	private static final String NL = "\n";

	/** The list: one encoded item per line, newest first. */
	public static final String PREF_RECENT_LIST = "RecentFiles";
	public static final String PREF_MAX_ITEMS = "MaxRecentFiles";
	public static final int DEFAULT_MAX_ITEMS = 10;

	private final Preferences prefs;
	private final Codec<T> codec;
	private final List<T> items;
	private int maxItems = -1;

	/**
	 * Reads the list from the preferences node. Malformed lines and duplicates are left out.
	 *
	 * @param prefs where the list is saved
	 * @param codec turns items into text and back
	 */
	public RecentItems(Preferences prefs, Codec<T> codec) {
		this.prefs = Objects.requireNonNull(prefs, "prefs");
		this.codec = Objects.requireNonNull(codec, "codec");
		items = read();
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
		items.clear();
		items.addAll(ret);
		store();
	}

	/** Empties the list. */
	public void clear() throws IOException {
		setItems(Collections.emptyList());
	}

	/** Puts an item at the top of the list, replacing an equal entry. */
	public void add(T item) throws IOException {
		add(item, (newer, older)->newer);
	}

	/**
	 * Puts an item at the top of the list. If the list already has it, the old entry is
	 * replaced by what merge returns, given the new item and the old entry. The oldest entries
	 * over the maximum are dropped.
	 */
	public void add(T item, BinaryOperator<T> merge) throws IOException {
		int idx = items.indexOf(item);
		if( idx >=0 ) {
			item = merge.apply(item, items.remove(idx));
		}
		items.add(0, item);

		int mx = getMaxItems();
		while(items.size()>mx && !items.isEmpty()) {
			items.remove(items.size()-1);
		}

		store();
	}

	/**
	 * Drops the entries that stale says no longer exist, and saves the list if any were.
	 *
	 * @return true if any were dropped
	 */
	public boolean removeIf(Predicate<? super T> stale) throws IOException {
		boolean changed = items.removeIf(stale);
		if( changed ) {
			store();
		}
		return changed;
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
}
