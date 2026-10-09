package us.bringardner.swing.menu.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import us.bringardner.swing.menu.RecentItems;
import us.bringardner.swing.menu.RecentItemsMenu;

/** The recent list without any UI. */
public class TestRecentItems {

	private static final String BASE = "us/bringardner/swing/menu/test/TestRecentItems";
	private static final RecentItems.Codec<String> STRINGS = new RecentItems.Codec<String>() {
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

	private Preferences node;

	@BeforeEach
	public void setUp() {
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
	public void newestFirstWithinTheMaximum() throws IOException {
		RecentItems<String> list = new RecentItems<>(node, STRINGS);
		assertEquals(RecentItems.DEFAULT_MAX_ITEMS, list.getMaxItems());
		list.setMaxItems(2);
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("2");
		assertEquals(Arrays.asList("2", "3"), list.getItems());
		assertEquals("2\n3\n", node.get(RecentItems.PREF_RECENT_LIST, null));

		RecentItems<String> again = new RecentItems<>(node, STRINGS);
		assertEquals(2, again.getMaxItems());
		assertEquals(Arrays.asList("2", "3"), again.getItems());
	}

	@Test
	public void malformedLinesAndDuplicatesAreLeftOut() {
		node.put(RecentItems.PREF_RECENT_LIST, "a\nbad line\n\nb\na\n");
		assertEquals(Arrays.asList("a", "b"), new RecentItems<>(node, STRINGS).getItems());
	}

	@Test
	public void setItemsAndClear() throws IOException {
		RecentItems<String> list = new RecentItems<>(node, STRINGS);
		list.setItems(Arrays.asList("a", "b", "a"));
		assertEquals(Arrays.asList("a", "b"), list.getItems());
		list.clear();
		assertTrue(list.getItems().isEmpty());
		assertEquals("", node.get(RecentItems.PREF_RECENT_LIST, null));
		assertThrows(UnsupportedOperationException.class, ()->list.getItems().add("x"));
	}

	@Test
	public void removeIfSavesOnlyWhenSomethingGoes() throws IOException {
		RecentItems<String> list = new RecentItems<>(node, STRINGS);
		list.setItems(Arrays.asList("a", "b", "c"));
		assertFalse(list.removeIf(s->s.equals("x")));
		assertTrue(list.removeIf(s->s.equals("b")));
		assertEquals(Arrays.asList("a", "c"), list.getItems());
		assertEquals("a\nc\n", node.get(RecentItems.PREF_RECENT_LIST, null));
	}

	@Test
	public void mergeDecidesWhichEntryIsKept() throws IOException {
		RecentItems<String> list = new RecentItems<>(node, STRINGS);
		String older = new String("x");
		list.add(older);
		list.add("y");
		list.add(new String("x"), (newer, old)->old);
		assertSame(older, list.getItems().get(0));
		assertEquals(2, list.getItems().size());
	}

	@Test
	public void theSwingMenuSharesTheList() throws IOException {
		RecentItems<String> list = new RecentItems<>(node, STRINGS);
		list.setItems(Arrays.asList("a", "b"));
		RecentItemsMenu<String> menu = new RecentItemsMenu<>("Recent", node, new RecentItemsMenu.Codec<String>() {
			@Override
			public String encode(String item) {
				return STRINGS.encode(item);
			}

			@Override
			public String decode(String line) {
				return STRINGS.decode(line);
			}
		});
		assertEquals(Arrays.asList("a", "b"), menu.getItems());
		menu.addItem("c");
		assertEquals(Arrays.asList("c", "a", "b"), new RecentItems<>(node, STRINGS).getItems());
	}
}
