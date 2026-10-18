package us.bringardner.swing.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import us.bringardner.swing.datetime.test.RunOnEdt;
import us.bringardner.swing.dialog.SettingsDialog;
import us.bringardner.swing.dialog.ThemeDesigner;
import us.bringardner.swing.gradient.Appearance;
import us.bringardner.swing.gradient.GradientLook;
import us.bringardner.swing.gradient.Theme;
import us.bringardner.swing.gradient.ThemeExtension;
import us.bringardner.swing.gradient.ThemeExtensions;

@ExtendWith(RunOnEdt.class)
public class TestThemeDesign {

	private File dir;
	private String oldDir;

	@BeforeEach
	public void ownThemesFolder() {
		oldDir = System.getProperty(Theme.USER_THEMES_PROPERTY);
		dir = new File(System.getProperty("java.io.tmpdir"), "themes-" + System.nanoTime());
		System.setProperty(Theme.USER_THEMES_PROPERTY, dir.getPath());
	}

	@AfterEach
	public void cleanUp() {
		File[] files = dir.listFiles();
		if( files != null ) {
			for(File f : files) {
				f.delete();
			}
		}
		dir.delete();
		if( oldDir == null ) {
			System.clearProperty(Theme.USER_THEMES_PROPERTY);
		} else {
			System.setProperty(Theme.USER_THEMES_PROPERTY, oldDir);
		}
		Theme.setCurrent(Theme.GOLD);
		ThemeExtensions.unregister(TestExtension.ID);
	}

	/** An application's part of a theme, for the tests: one "color" setting, dark or light by default. */
	static class TestExtension implements ThemeExtension {
		static final String ID = "test-app.editor";
		javax.swing.JTextField field;
		Runnable listener;
		/** The theme the page was last told of. */
		Theme heard;

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public String getTitle() {
			return "Test editor";
		}

		@Override
		public java.util.Map<String, String> defaults(Theme theme) {
			return java.util.Map.of("color", theme.isDark() ? "#000000" : "#FFFFFF");
		}

		@Override
		public Page createPage() {
			field = new javax.swing.JTextField(8);
			return new Page() {
				@Override
				public javax.swing.JComponent getComponent() {
					return field;
				}

				@Override
				public void setValues(java.util.Map<String, String> values, Theme theme) {
					field.setText(values.get("color"));
				}

				@Override
				public java.util.Map<String, String> getValues() {
					return java.util.Map.of("color", field.getText());
				}

				@Override
				public void setChangeListener(Runnable l) {
					listener = l;
				}

				@Override
				public void themeChanged(Theme theme) {
					heard = theme;
				}
			};
		}
	}

	@Test
	public void aThemeKeepsTheApplicationsSettings() throws Exception {
		Theme t = Theme.GOLD.withExtras(java.util.Map.of("b-app.x", "2", "a-app.y", "one = two"));
		assertEquals(java.util.Map.of("a-app.y", "one = two", "b-app.x", "2"), t.getExtras());
		assertTrue(Theme.GOLD.getExtras().isEmpty(), "Gold itself is unchanged");
		assertFalse(t.equals(Theme.GOLD));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		t.write(out);
		String text = out.toString(StandardCharsets.UTF_8);
		assertTrue(text.indexOf("a-app.y") < text.indexOf("b-app.x"), text);
		Theme back = Theme.read(new ByteArrayInputStream(out.toByteArray()));
		assertEquals(t, back);

		// kept through copies, and a null takes one out
		assertEquals(t.getExtras(), t.withName("Other").withDescription("d").getExtras());
		assertEquals(java.util.Map.of("a-app.y", "one = two"), t.withExtras(java.util.Collections.singletonMap("b-app.x", null)).getExtras());
		assertThrows(IllegalArgumentException.class, ()->t.withExtras(java.util.Map.of("start", "#000000")));
		assertThrows(IllegalArgumentException.class, ()->t.withExtras(java.util.Map.of(" ", "x")));

		// and saved with a user theme
		Theme.saveUserTheme(t.withName("Mine"));
		assertEquals(t.getExtras(), Theme.named("Mine").getExtras());
	}

	@Test
	public void anExtensionsValuesAreTheThemesOrTheDefaults() {
		TestExtension x = new TestExtension();
		assertEquals(java.util.Map.of("color", "#FFFFFF"), Theme.GOLD.values(x));
		assertEquals(java.util.Map.of("color", "#000000"), Theme.DARK.values(x));
		assertFalse(Theme.GOLD.hasValues(x));

		Theme t = Theme.GOLD.withExtras(java.util.Map.of("other.k", "v")).withValues(x, java.util.Map.of("color", "#123456"));
		assertTrue(t.hasValues(x));
		assertEquals(java.util.Map.of("color", "#123456"), t.values(x));
		assertEquals("#123456", t.getExtras().get("test-app.editor.color"));
		assertEquals("v", t.getExtras().get("other.k"), "the others are kept");
		assertEquals(java.util.Map.of("color", "#FFFFFF"), t.withValues(x, java.util.Map.of()).values(x), "replaced: defaults again");
	}

	@Test
	public void aPageHearsOfTheThemesColors() throws Exception {
		TestExtension x = new TestExtension();
		ThemeExtensions.register(x);
		try {
			ThemeDesigner d = new ThemeDesigner(null, Theme.GOLD);
			assertEquals(Theme.GOLD.getStart(), x.heard.getStart(), "the theme started from");
			d.setColor(0, new java.awt.Color(0x123456));
			assertEquals(new java.awt.Color(0x123456), x.heard.getStart(), "a color changed");
			d.cancel();
		} finally {
			ThemeExtensions.unregister(TestExtension.ID);
		}
	}

	@Test
	public void extensionsAreRegisteredById() {
		TestExtension x = new TestExtension();
		ThemeExtensions.register(x);
		ThemeExtensions.register(new TestExtension());
		assertEquals(1, ThemeExtensions.all().stream().filter(e->e.getId().equals(TestExtension.ID)).count());
		ThemeExtensions.unregister(TestExtension.ID);
		assertTrue(ThemeExtensions.all().stream().noneMatch(e->e.getId().equals(TestExtension.ID)));
		assertThrows(IllegalArgumentException.class, ()->ThemeExtensions.register(new TestExtension() {
			@Override
			public String getId() {
				return "has space";
			}
		}));
		assertThrows(NullPointerException.class, ()->ThemeExtensions.register(null));
	}

	@Test
	public void theDesignerHasAPageForEachExtension() throws Exception {
		GradientLook.install();
		TestExtension x = new TestExtension();
		ThemeExtensions.register(x);

		// from a built-in theme: its defaults
		ThemeDesigner d = new ThemeDesigner(null, Theme.DARK);
		assertEquals("#000000", x.field.getText());
		x.field.setText("#ABCDEF");
		x.listener.run();
		d.setThemeName("Night");
		assertTrue(d.save());
		assertEquals("#ABCDEF", Theme.named("Night").getExtras().get("test-app.editor.color"));

		// a new theme takes its parent's values
		ThemeDesigner child = new ThemeDesigner(null, Theme.named("Night"));
		assertEquals("#ABCDEF", x.field.getText());
		child.setThemeName("Night owl");
		assertTrue(child.save());
		assertEquals("#ABCDEF", Theme.named("Night owl").values(x).get("color"));

		// changing the start: that theme's values
		ThemeDesigner other = new ThemeDesigner(null, Theme.named("Night"));
		other.startFrom(Theme.GOLD);
		assertEquals("#FFFFFF", x.field.getText());
		other.cancel();

		// editing keeps them, and changes them
		ThemeDesigner edit = new ThemeDesigner(null, Theme.named("Night owl"), true);
		assertEquals("#ABCDEF", x.field.getText());
		x.field.setText("#111111");
		assertTrue(edit.save());
		assertEquals("#111111", Theme.named("Night owl").values(x).get("color"));
		assertEquals("#ABCDEF", Theme.named("Night").values(x).get("color"), "the parent is unchanged");
	}

	@Test
	public void settingsOfAnApplicationNotRunningAreKept() throws Exception {
		GradientLook.install();
		Theme.saveUserTheme(Theme.GOLD.withName("Shared").withExtras(java.util.Map.of("ide.keywords", "#0000FF")));
		// designed in an application that doesn't know "ide"
		ThemeDesigner d = new ThemeDesigner(null, Theme.named("Shared"));
		d.setThemeName("Shared 2");
		assertTrue(d.save());
		assertEquals("#0000FF", Theme.named("Shared 2").getExtras().get("ide.keywords"));
		ThemeDesigner e = new ThemeDesigner(null, Theme.named("Shared"), true);
		e.setColor(0, java.awt.Color.white);
		assertTrue(e.save());
		assertEquals("#0000FF", Theme.named("Shared").getExtras().get("ide.keywords"));
	}

	@Test
	public void theBuiltInThemesAreTheFilesInOrder() {
		List<String> names = Theme.readList(Theme.class.getResourceAsStream(Theme.THEMES_PATH + Theme.THEMES_LIST));
		assertEquals(Theme.THEMES.size(), names.size());
		for(int i = 0; i < names.size(); i++) {
			assertEquals(names.get(i), Theme.THEMES.get(i).getName());
			assertNotNull(Theme.class.getResource(Theme.THEMES_PATH + names.get(i) + Theme.EXTENSION));
		}
		assertEquals("Gold", Theme.THEMES.get(0).getName());
		assertSame(Theme.GOLD, Theme.THEMES.get(0));
		assertTrue(Theme.DARK.isDark());
		assertNotNull(Theme.GOLD.getDescription());
		assertTrue(Theme.readList(null).isEmpty());
	}

	@Test
	public void writeAndReadAreTheSame() throws Exception {
		for(Theme t : Theme.THEMES) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.write(out);
			Theme back = Theme.read(new ByteArrayInputStream(out.toByteArray()));
			assertEquals(t, back, t.getName());
			assertEquals(t.getDescription(), back.getDescription());
		}
		// a name and description with characters the properties format treats specially
		Theme odd = Theme.GOLD.withName(" Gold: #2 = best ").withDescription("a: b = c # d");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		odd.write(out);
		Theme back = Theme.read(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("Gold: #2 = best", back.getName());
		assertEquals("a: b = c # d", back.getDescription());
	}

	@Test
	public void badFilesSayWhatIsWrong() {
		assertThrows(IllegalArgumentException.class, ()->read("dark=false"));
		Exception e = assertThrows(IllegalArgumentException.class, ()->read("name=X\nstart=#FFFFFF"));
		assertTrue(e.getMessage().contains("end"), e.getMessage());
		e = assertThrows(IllegalArgumentException.class, ()->read(fullTheme("X").replace("#000000", "blue")));
		assertTrue(e.getMessage().contains("isn't a color"), e.getMessage());
		assertThrows(IllegalArgumentException.class, ()->Theme.of("x", new Color[3], false));
	}

	@Test
	public void userThemesAreSavedListedAndDeleted() throws Exception {
		assertTrue(Theme.userThemes().isEmpty(), "no folder yet: no themes");
		Theme saved = Theme.saveUserTheme(Theme.named("Ocean").withName("Sunrise"));
		assertTrue(saved.isUserTheme());
		assertTrue(new File(dir, "Sunrise.theme").isFile());
		assertEquals(List.of("Sunrise"), names(Theme.userThemes()));
		assertSame(null, Theme.named("Nope"));
		assertTrue(Theme.named("sunrise").isUserTheme(), "found by name, any case");
		assertEquals(Theme.THEMES.size() + 1, Theme.all().size());

		// a name with characters a file name can't have
		Theme.saveUserTheme(Theme.GOLD.withName("A/B: c"));
		assertTrue(new File(dir, "A_B_ c.theme").isFile());
		assertEquals(List.of("A/B: c", "Sunrise"), names(Theme.userThemes()));

		// a file that isn't a theme, and one with a built-in name, are left out
		try (FileOutputStream o = new FileOutputStream(new File(dir, "broken.theme"))) {
			o.write("name=Broken\n".getBytes(StandardCharsets.UTF_8));
		}
		try (FileOutputStream o = new FileOutputStream(new File(dir, "gold.theme"))) {
			o.write(fullTheme("Gold").getBytes(StandardCharsets.UTF_8));
		}
		assertEquals(List.of("A/B: c", "Sunrise"), names(Theme.userThemes()));

		assertTrue(Theme.deleteUserTheme("sunrise"));
		assertFalse(Theme.deleteUserTheme("Sunrise"));
		assertEquals(List.of("A/B: c"), names(Theme.userThemes()));
	}

	@Test
	public void aUserThemeCantTakeABuiltInOrEmptyName() {
		assertThrows(IllegalArgumentException.class, ()->Theme.saveUserTheme(Theme.GOLD.withName("dark")));
		assertThrows(IllegalArgumentException.class, ()->Theme.saveUserTheme(Theme.GOLD.withName("  ")));
		assertTrue(Theme.isBuiltInName(" Ocean "));
		assertFalse(Theme.isBuiltInName("Sunrise"));
	}

	@Test
	public void aSavedUserThemeIsRememberedByName() throws Exception {
		Theme mine = Theme.saveUserTheme(Theme.DARK.withName("Night"));
		java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot()
				.node("us/bringardner/swing/test/design-" + System.nanoTime());
		try {
			Appearance.defaults().withTheme(mine).save(prefs);
			Theme back = Appearance.load(prefs).getTheme();
			assertEquals("Night", back.getName());
			assertTrue(back.isUserTheme());
			assertTrue(back.sameColors(Theme.DARK));
			// deleted since: the default
			Theme.deleteUserTheme("Night");
			assertSame(Theme.GOLD, Appearance.load(prefs).getTheme());
		} finally {
			prefs.removeNode();
		}
	}

	@Test
	public void theDesignerStartsFromAThemeAndSavesANewOne() throws Exception {
		GradientLook.install();
		ThemeDesigner d = new ThemeDesigner(null, Theme.named("Ocean"));
		assertTrue(d.getTheme().sameColors(Theme.named("Ocean")), "starts with its colors");
		assertEquals("Ocean 2", d.getTheme().getName(), "and a new name");

		d.setColor(0, new Color(0xFFE9C9));
		d.setColor(2, new Color(0x888888));
		d.setDark(false);
		d.setThemeName("Sunrise");
		assertEquals(new Color(0xFFE9C9), d.getTheme().getStart());
		assertTrue(Theme.named("Ocean").getStart().equals(new Color(0x7AC2F2)), "the starting theme is untouched");

		assertTrue(d.save());
		Theme saved = d.getSaved();
		assertEquals("Sunrise", saved.getName());
		assertTrue(saved.isUserTheme());
		assertEquals(new Color(0x888888), Theme.named("Sunrise").getText());

		// starting again from the saved one suggests "Sunrise 2"; a taken name can't be saved
		ThemeDesigner again = new ThemeDesigner(null, saved);
		assertEquals("Sunrise 2", again.getTheme().getName());
		again.setThemeName("ocean");
		assertFalse(again.save(), "a built-in name");
		again.setThemeName("Sunrise");
		assertFalse(again.save(), "a theme's name already");
		again.setThemeName("");
		assertFalse(again.save(), "no name");
		again.setThemeName(Theme.CUSTOM);
		assertFalse(again.save(), "the custom theme's name");
		again.cancel();
		assertNull(again.getSaved());
	}

	@Test
	public void theDesignerChangesAUsersThemeInPlace() throws Exception {
		GradientLook.install();
		Theme mine = Theme.saveUserTheme(Theme.named("Sage").withName("Garden").withDescription("Greens"));
		assertThrows(IllegalArgumentException.class, ()->new ThemeDesigner(null, Theme.GOLD, true),
				"a built-in theme can't be edited");

		ThemeDesigner d = new ThemeDesigner(null, mine, true);
		assertTrue(d.isEditing());
		assertEquals("Garden", d.getTheme().getName(), "keeps its name");
		d.setColor(5, new Color(0x224422));
		assertTrue(d.save(), "its own name is fine");
		assertEquals(List.of("Garden"), names(Theme.userThemes()));
		Theme changed = Theme.named("Garden");
		assertEquals(new Color(0x224422), changed.getSelection());
		assertEquals("Greens", changed.getDescription());

		// renamed: one theme, under the new name
		d = new ThemeDesigner(null, changed, true);
		d.setThemeName("Dark");
		assertFalse(d.save(), "not a built-in name");
		d.setThemeName("Allotment");
		assertTrue(d.save());
		assertEquals(List.of("Allotment"), names(Theme.userThemes()));

		// only the case changed: still one file
		d = new ThemeDesigner(null, Theme.named("Allotment"), true);
		d.setThemeName("allotment");
		assertTrue(d.save());
		assertEquals(List.of("allotment"), names(Theme.userThemes()));
		assertEquals(1, dir.listFiles().length);

		// another of the user's names is taken
		Theme.saveUserTheme(Theme.GOLD.withName("Sun"));
		d = new ThemeDesigner(null, Theme.named("allotment"), true);
		d.setThemeName("sun");
		assertFalse(d.save());
		d.cancel();
		assertTrue(Theme.named("allotment").getSelection().equals(new Color(0x224422)), "canceled: unchanged");
	}

	@Test
	public void theDesignerCanStartFromAColor() {
		GradientLook.install();
		ThemeDesigner d = new ThemeDesigner(null, Theme.GOLD);
		d.startFromColor(new Color(200, 60, 70));
		assertTrue(d.getTheme().sameColors(Theme.fromColor(new Color(200, 60, 70))));
		assertEquals("My theme 2", d.getTheme().getName());
		d.cancel();
	}

	@Test
	public void theDialogOffersDesignedThemesAndDeletesThem() throws Exception {
		GradientLook.install();
		SettingsDialog s = new SettingsDialog();
		Theme mine = Theme.saveUserTheme(Theme.LIGHT.withName("Paper"));
		s.designed(mine);
		assertEquals("Paper", s.getAppearance().getTheme().getName());
		// edited elsewhere, then shown again
		Theme.saveUserTheme(Theme.named("Paper").withName("Paper").withDescription("Edited"));
		s.designed(Theme.named("Paper"));
		assertEquals("Edited", s.getAppearance().getTheme().getDescription());
		s.deleteTheme(Theme.named("Paper"));
		assertNull(Theme.named("Paper"));
		assertSame(Theme.GOLD, s.getAppearance().getTheme());
		// a built-in one can't be deleted
		s.deleteTheme(Theme.DARK);
		assertNotNull(Theme.named("Dark"));
		s.cancel();
	}

	@Test
	public void contrastIsTheWcagRatio() {
		assertEquals(21.0, Theme.contrast(Color.black, Color.white), 0.01);
		assertEquals(1.0, Theme.contrast(Color.gray, Color.gray), 0.01);
		assertEquals(Theme.contrast(Color.red, Color.white), Theme.contrast(Color.white, Color.red), 0.0001);
		assertEquals("#7AC2F2", Theme.hex(new Color(0x7AC2F2)));
	}

	private static Theme read(String s) throws Exception {
		return Theme.read(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
	}

	private static String fullTheme(String name) {
		StringBuilder b = new StringBuilder("name=" + name + "\n");
		for(String k : Theme.KEYS) {
			b.append(k).append("=#000000\n");
		}
		return b.toString();
	}

	private static List<String> names(List<Theme> themes) {
		List<String> ret = new java.util.ArrayList<>();
		for(Theme t : themes) {
			ret.add(t.getName());
		}
		return ret;
	}
}
