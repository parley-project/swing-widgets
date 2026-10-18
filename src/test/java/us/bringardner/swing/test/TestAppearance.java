package us.bringardner.swing.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import us.bringardner.swing.datetime.test.RunOnEdt;
import us.bringardner.swing.dialog.SettingsDialog;
import us.bringardner.swing.gradient.Appearance;
import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientColors;
import us.bringardner.swing.gradient.GradientLook;
import us.bringardner.swing.gradient.Theme;

@ExtendWith(RunOnEdt.class)
public class TestAppearance {

	private Preferences prefs;

	@BeforeEach
	public void freshPrefs() throws Exception {
		prefs = Preferences.userRoot().node("us/bringardner/swing/test/appearance-" + System.nanoTime());
	}

	@AfterEach
	public void restore() throws Exception {
		Appearance.defaults().apply();
		prefs.removeNode();
	}

	@Test
	public void theThemesHaveNamesAndGoldIsFirst() {
		assertSame(Theme.GOLD, Theme.THEMES.get(0));
		assertSame(Theme.DARK, Theme.named("dark"));
		assertSame(Theme.LIGHT, Theme.named("Light"));
		assertNull(Theme.named("nope"));
		assertEquals(GradientColors.DEFAULT_START, Theme.GOLD.getStart());
		Set<String> names = new HashSet<>();
		for(Theme t : Theme.THEMES) {
			assertTrue(names.add(t.getName()), "names are distinct");
			assertEquals(t.getName(), t.toString());
			assertFalse(t.isCustom());
		}
		assertTrue(Theme.DARK.isDark());
		assertFalse(Theme.GOLD.isDark());
		assertEquals(8, Theme.THEMES.size());
	}

	@Test
	public void everyThemeIsReadable() {
		for(Theme t : Theme.THEMES) {
			// text, field text, selected text and header text each stand out from what is behind them
			assertTrue(contrast(t.getText(), t.getStart()) >= 4.5, t + " text on the light end");
			assertTrue(contrast(t.getFieldText(), t.getField()) >= 4.5, t + " field text");
			assertTrue(contrast(t.getSelectionText(), t.getSelection()) >= 3, t + " selected text");
			assertTrue(contrast(t.getHeaderText(), t.getHeader()) >= 3, t + " header text");
		}
	}

	@Test
	public void aThemeFromOneColorHasALightAndADarkEnd() {
		for(Color c : new Color[] {Color.red, new Color(30, 90, 40), Color.gray, Color.black, Color.white}) {
			Theme s = Theme.fromColor(c);
			assertTrue(s.isCustom());
			assertEquals(c, s.getBaseColor());
			assertEquals(Theme.CUSTOM, s.getName());
			// black text reads on the light end, white on the dark end, as with gold
			assertTrue(brightness(s.getStart()) > 0.9f, "light: " + s.getStart());
			assertTrue(brightness(s.getEnd()) < 0.65f, "dark: " + s.getEnd());
		}
	}

	@Test
	public void gradientColorsFollowTheTheme() {
		Theme.setCurrent(Theme.DARK);
		assertEquals(Theme.DARK.getStart(), GradientColors.getStart());
		GradientColors.setEnd(Color.blue);
		assertEquals(Color.blue, Theme.current().getEnd());
		assertEquals(Theme.DARK.getText(), Theme.current().getText(), "the rest of the theme stays");
		GradientColors.setEnd(Color.blue);
		Theme.setCurrent(Theme.GOLD);
	}

	@Test
	public void loadWithoutPrefsIsTheDefault() {
		assertEquals(Appearance.defaults(), Appearance.load(null));
		assertEquals(Appearance.defaults(), Appearance.load(prefs));
		assertSame(Theme.GOLD, Appearance.defaults().getTheme());
		assertNull(Appearance.defaults().getFont());
	}

	@Test
	public void saveAndLoad() {
		Theme ocean = Theme.named("Ocean");
		Font f = new Font(Font.SERIF, Font.BOLD, 17);
		Appearance a = Appearance.defaults().withTheme(ocean).withFont(f);
		a.save(prefs);
		Appearance back = Appearance.load(prefs);
		assertEquals(a, back);
		assertEquals(a.hashCode(), back.hashCode());
		assertSame(ocean, back.getTheme());

		// a custom theme is saved as the color it was made from
		Appearance custom = Appearance.defaults().withTheme(Theme.fromColor(Color.orange));
		custom.save(prefs);
		assertEquals(custom, Appearance.load(prefs));
		assertEquals(Theme.CUSTOM, prefs.get(Appearance.THEME, null));
		// back to a named one: the color goes
		Appearance.defaults().withTheme(Theme.DARK).save(prefs);
		assertNull(prefs.get(Appearance.THEME_COLOR, null));
		assertSame(Theme.DARK, Appearance.load(prefs).getTheme());

		// an unknown name: the default
		prefs.put(Appearance.THEME, "Paisley");
		assertSame(Theme.GOLD, Appearance.load(prefs).getTheme());

		// the standard font again: the saved one is removed
		a.withFont(null).save(prefs);
		assertNull(prefs.get(Appearance.FONT_FAMILY, null));
		assertNull(Appearance.load(prefs).getFont());
	}

	@Test
	public void theViewersSavedFontLoads() {
		// what parley-files-viewer has saved all along
		prefs.put("FontFamily", Font.MONOSPACED);
		prefs.putInt("FontStyle", Font.ITALIC);
		prefs.putInt("FontSize", 14);
		Font f = Appearance.load(prefs).getFont();
		assertEquals(Font.MONOSPACED, f.getFamily());
		assertEquals(Font.ITALIC, f.getStyle());
		assertEquals(14, f.getSize());
	}

	@Test
	public void applyChangesColorsFontsAndOpenWindows() {
		GradientLook.install();
		JFrame frame = new JFrame();
		GradientButton button = new GradientButton("b");
		frame.add(button);
		frame.pack();
		Theme plum = Theme.named("Plum");
		Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 19);

		Appearance.defaults().withTheme(plum).withFont(f).apply();

		assertEquals(plum.getStart(), GradientColors.getStart());
		assertEquals(plum.getEnd(), UIManager.getColor("Panel.foreground"));
		assertEquals(19, UIManager.getFont("Label.font").getSize());
		assertEquals(plum.getButtonText(), button.getTextColor(), "a gradient button follows the theme");
		assertEquals(Appearance.current(), Appearance.defaults().withTheme(plum).withFont(f));

		Appearance.defaults().withTheme(Theme.DARK).apply();
		assertEquals(Theme.DARK.getText(), UIManager.getColor("Label.foreground"), "dark: light text");
		assertEquals(Theme.DARK.getField(), UIManager.getColor("TextField.background"));
		assertEquals(Theme.DARK.getHeader(), UIManager.getColor("TableHeader.background"));

		Appearance.defaults().apply();
		assertNotEquals(19, UIManager.getFont("Label.font").getSize(), "the standard fonts come back");
		assertNotEquals(Theme.DARK.getText(), UIManager.getColor("Label.foreground"), "the look's own text color comes back");
		frame.dispose();
	}

	@Test
	public void installWithPrefsUsesWhatWasSaved() {
		Theme sage = Theme.named("Sage");
		Appearance.defaults().withTheme(sage).save(prefs);
		Appearance a = GradientLook.install(prefs);
		assertSame(sage, a.getTheme());
		assertEquals(sage.getStart(), UIManager.getColor("Panel.background"));
	}

	@Test
	public void okSavesAndAppliesCancelDoesNothing() {
		GradientLook.install();
		Theme copper = Theme.named("Copper");

		SettingsDialog cancelled = new SettingsDialog(prefs);
		cancelled.selectTheme(copper);
		cancelled.cancel();
		assertFalse(cancelled.isAccepted());
		assertEquals(GradientColors.DEFAULT_START, GradientColors.getStart());
		assertEquals(Appearance.defaults(), Appearance.load(prefs));

		SettingsDialog d = new SettingsDialog(null, prefs);
		d.selectTheme(copper);
		d.ok();
		assertTrue(d.isAccepted());
		assertEquals(copper.getStart(), GradientColors.getStart());
		assertSame(copper, Appearance.load(prefs).getTheme());
	}

	@Test
	public void customColorResetAndFont() {
		GradientLook.install();
		SettingsDialog d = new SettingsDialog();
		d.selectColor(new Color(200, 60, 70));
		assertTrue(d.getAppearance().getTheme().isCustom(), "a custom color makes a custom theme");
		Font f = new Font(Font.SERIF, Font.PLAIN, 15);
		d.selectFont(f);
		assertEquals(f, d.getAppearance().getFont());
		d.selectFont(null);
		assertNull(d.getAppearance().getFont());
		d.reset();
		assertEquals(Appearance.defaults(), d.getAppearance());
		d.ok();
		// nothing changed, so nothing to save
		assertNull(prefs.get(Appearance.THEME, null));
		assertNotNull(d.getAppearance());
	}

	@Test
	public void aDialogOpensOnACustomTheme() {
		GradientLook.install();
		Appearance.defaults().withTheme(Theme.fromColor(Color.orange)).apply();
		SettingsDialog d = new SettingsDialog();
		assertTrue(d.getAppearance().getTheme().isCustom());
		d.cancel();
	}

	/** WCAG contrast ratio. */
	private static double contrast(Color a, Color b) {
		double la = luminance(a), lb = luminance(b);
		return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
	}

	private static double luminance(Color c) {
		double[] v = {c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0};
		for(int i = 0; i < 3; i++) {
			v[i] = v[i] <= 0.03928 ? v[i] / 12.92 : Math.pow((v[i] + 0.055) / 1.055, 2.4);
		}
		return 0.2126 * v[0] + 0.7152 * v[1] + 0.0722 * v[2];
	}

	private static float brightness(Color c) {
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2];
	}
}
