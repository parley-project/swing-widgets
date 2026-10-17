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
import us.bringardner.swing.gradient.Appearance.Scheme;
import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientColors;
import us.bringardner.swing.gradient.GradientLook;

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
	public void theSchemesHaveNamesAndGoldIsFirst() {
		assertSame(Appearance.GOLD, Appearance.SCHEMES.get(0));
		assertEquals(GradientColors.DEFAULT_START, Appearance.GOLD.getStart());
		Set<String> names = new HashSet<>();
		for(Scheme s : Appearance.SCHEMES) {
			assertTrue(names.add(s.getName()), "names are distinct");
			assertEquals(s.getName(), s.toString());
		}
		assertTrue(Appearance.SCHEMES.size() >= 5);
	}

	@Test
	public void aSchemeFromOneColorHasALightAndADarkEnd() {
		for(Color c : new Color[] {Color.red, new Color(30, 90, 40), Color.gray, Color.black, Color.white}) {
			Scheme s = Scheme.fromColor("x", c);
			// black text reads on the light end, white on the dark end, as with gold
			assertTrue(brightness(s.getStart()) > 0.9f, "light: " + s.getStart());
			assertTrue(brightness(s.getEnd()) < 0.65f, "dark: " + s.getEnd());
		}
	}

	@Test
	public void loadWithoutPrefsIsTheDefault() {
		assertEquals(Appearance.defaults(), Appearance.load(null));
		assertEquals(Appearance.defaults(), Appearance.load(prefs));
		assertSame(Appearance.GOLD, Appearance.defaults().getScheme());
		assertNull(Appearance.defaults().getFont());
	}

	@Test
	public void saveAndLoad() {
		Scheme ocean = Appearance.SCHEMES.get(3);
		Font f = new Font(Font.SERIF, Font.BOLD, 17);
		Appearance a = Appearance.defaults().withScheme(ocean).withFont(f);
		a.save(prefs);
		Appearance back = Appearance.load(prefs);
		assertEquals(a, back);
		assertEquals(a.hashCode(), back.hashCode());
		assertSame(ocean, back.getScheme());

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
		Scheme plum = Appearance.SCHEMES.get(5);
		Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 19);

		Appearance.defaults().withScheme(plum).withFont(f).apply();

		assertEquals(plum.getStart(), GradientColors.getStart());
		assertEquals(plum.getEnd(), UIManager.getColor("Panel.foreground"));
		assertEquals(19, UIManager.getFont("Label.font").getSize());
		assertEquals(plum.getStart(), button.getBackground(), "an open window's button is recolored");
		assertEquals(plum.getEnd(), button.getForeground());
		assertEquals(Appearance.current(), Appearance.defaults().withScheme(plum).withFont(f));

		Appearance.defaults().apply();
		assertNotEquals(19, UIManager.getFont("Label.font").getSize(), "the standard fonts come back");
		frame.dispose();
	}

	@Test
	public void installWithPrefsUsesWhatWasSaved() {
		Scheme sage = Appearance.SCHEMES.get(2);
		Appearance.defaults().withScheme(sage).save(prefs);
		Appearance a = GradientLook.install(prefs);
		assertSame(sage, a.getScheme());
		assertEquals(sage.getStart(), UIManager.getColor("Panel.background"));
	}

	@Test
	public void okSavesAndAppliesCancelDoesNothing() {
		GradientLook.install();
		Scheme copper = Appearance.SCHEMES.get(1);

		SettingsDialog cancelled = new SettingsDialog(prefs);
		cancelled.selectScheme(copper);
		cancelled.cancel();
		assertFalse(cancelled.isAccepted());
		assertEquals(GradientColors.DEFAULT_START, GradientColors.getStart());
		assertEquals(Appearance.defaults(), Appearance.load(prefs));

		SettingsDialog d = new SettingsDialog(null, prefs);
		d.selectScheme(copper);
		d.ok();
		assertTrue(d.isAccepted());
		assertEquals(copper.getStart(), GradientColors.getStart());
		assertSame(copper, Appearance.load(prefs).getScheme());
	}

	@Test
	public void customColorResetAndFont() {
		GradientLook.install();
		SettingsDialog d = new SettingsDialog();
		d.selectColor(new Color(200, 60, 70));
		assertNull(d.getAppearance().getScheme(), "a custom color is no named scheme");
		Font f = new Font(Font.SERIF, Font.PLAIN, 15);
		d.selectFont(f);
		assertEquals(f, d.getAppearance().getFont());
		d.selectFont(null);
		assertNull(d.getAppearance().getFont());
		d.reset();
		assertEquals(Appearance.defaults(), d.getAppearance());
		d.ok();
		// nothing changed, so nothing to save
		assertNull(prefs.get(Appearance.GRADIENT_START, null));
		assertNotNull(d.getAppearance());
	}

	@Test
	public void aDialogOpensOnACustomScheme() {
		GradientLook.install();
		Appearance.defaults().withScheme(Scheme.fromColor("Custom", Color.orange)).apply();
		SettingsDialog d = new SettingsDialog();
		assertNull(d.getAppearance().getScheme());
		d.cancel();
	}

	private static float brightness(Color c) {
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2];
	}
}
