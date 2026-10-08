package us.bringardner.swing.menu.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import us.bringardner.swing.menu.RecentItemsMenu;

/** Tests for RecentItemsMenu that do not need a display. */
public class TestRecentItemsMenu {

	private static final String BASE = "us/bringardner/swing/menu/test/TestRecentItemsMenu";
	private static final RecentItemsMenu.Codec<String> STRINGS = new RecentItemsMenu.Codec<String>() {
		@Override
		public String encode(String item) {
			return item;
		}

		@Override
		public String decode(String line) {
			if( line.startsWith("bad") ) {
				throw new IllegalArgumentException(line);
			}
			return line;
		}
	};

	/** Items in this set are stale. */
	private static final Set<String> gone = new HashSet<>();

	static class Menu extends RecentItemsMenu<String> {
		private static final long serialVersionUID = 1L;

		Menu(Preferences prefs) throws IOException {
			super("Recent", prefs, STRINGS);
		}

		@Override
		protected String getLabel(String item) {
			return "<"+item+">";
		}

		@Override
		protected boolean isStale(String item) {
			return gone.contains(item);
		}

		@Override
		protected Object openItem(String item) throws IOException {
			if( item.equals("cancel") ) {
				return null;
			}
			return item.toUpperCase();
		}
	}

	private Preferences node;

	@BeforeEach
	public void setUp() {
		gone.clear();
		node = Preferences.userRoot().node(BASE+"/"+UUID.randomUUID());
	}

	@AfterEach
	public void tearDown() throws BackingStoreException {
		Preferences parent = node.parent();
		node.removeNode();
		parent.flush();
	}

	@AfterAll
	public static void removeBase() throws BackingStoreException {
		Preferences.userRoot().node(BASE).removeNode();
		Preferences.userRoot().flush();
	}

	@Test
	public void newestFirstAndMaxSurviveARestart() throws IOException {
		Menu menu = new Menu(node);
		assertEquals(RecentItemsMenu.DEFAULT_MAX_ITEMS, menu.getMaxItems());
		menu.setMaxItems(2);
		menu.addItem("1");
		menu.addItem("2");
		menu.addItem("3");
		menu.addItem("2");
		assertEquals(Arrays.asList("2", "3"), menu.getItems());

		Menu again = new Menu(node);
		assertEquals(2, again.getMaxItems());
		assertEquals(Arrays.asList("2", "3"), again.getItems());
		assertEquals("2\n3\n", node.get(RecentItemsMenu.PREF_RECENT_LIST, null));
	}

	@Test
	public void menuHasControlsThenOneItemPerEntry() throws IOException {
		Menu menu = new Menu(node);
		menu.setItems(Arrays.asList("a", "b", "a"));
		assertEquals(4, menu.getItemCount());
		assertEquals("Max Items:10", menu.getItem(0).getText());
		assertEquals("Clear Recent List", menu.getItem(1).getText());
		assertEquals("<a>", menu.getItem(2).getText());
		assertEquals("<b>", menu.getItem(3).getText());

		menu.getItem(1).doClick(0);
		assertTrue(menu.getItems().isEmpty());
		assertEquals(2, menu.getItemCount());
		assertEquals("", node.get(RecentItemsMenu.PREF_RECENT_LIST, null));
	}

	@Test
	public void malformedLinesAndStaleItemsAreDropped() throws IOException {
		node.put(RecentItemsMenu.PREF_RECENT_LIST, "a\nbad line\n\nb\na\nc\n");
		gone.add("b");

		Menu menu = new Menu(node);
		assertEquals(Arrays.asList("a", "c"), menu.getItems());
		assertEquals("a\nc\n", node.get(RecentItemsMenu.PREF_RECENT_LIST, null));
	}

	@Test
	public void choosingAnEntryMovesItUpAndFiresWithTheOpenedValue() throws IOException {
		Menu menu = new Menu(node);
		menu.setItems(Arrays.asList("a", "b", "cancel"));
		List<Object> sources = new ArrayList<>();
		menu.addActionListener(e -> sources.add(e.getSource()));

		menu.getItem(3).doClick(0);
		assertEquals(Arrays.asList("B"), sources);
		assertEquals(Arrays.asList("b", "a", "cancel"), menu.getItems());

		// openItem returning null does nothing
		menu.getItem(4).doClick(0);
		assertEquals(1, sources.size());
		assertEquals(Arrays.asList("b", "a", "cancel"), menu.getItems());
	}

	@Test
	public void mergeDecidesWhichEntryIsKept() throws IOException {
		String older = new String("x");
		RecentItemsMenu<String> menu = new RecentItemsMenu<String>("Recent", node, STRINGS) {
			private static final long serialVersionUID = 1L;

			@Override
			protected String merge(String newer, String old) {
				return old;
			}
		};
		menu.addItem(older);
		menu.addItem("y");
		menu.addItem(new String("x"));
		assertSame(older, menu.getItems().get(0));
		assertEquals(2, menu.getItems().size());
	}
}
