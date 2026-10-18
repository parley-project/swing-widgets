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
package us.bringardner.swing.gradient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * What a user chooses about the gradient look: a {@link Theme} and the font.
 * {@link #load(Preferences)} and {@link #save(Preferences)} keep it in an application's
 * preferences; {@link #apply()} puts it into effect, in the windows already open too.
 * {@code SettingsDialog} is the dialog for choosing it.
 */
public final class Appearance {

	/** Preference keys. The font keys are the ones parley-files-viewer has always used. */
	public static final String FONT_FAMILY = "FontFamily";
	public static final String FONT_STYLE = "FontStyle";
	public static final String FONT_SIZE = "FontSize";
	/** The theme's name; for a custom theme, {@link Theme#CUSTOM} and the color in {@link #THEME_COLOR}. */
	public static final String THEME = "Theme";
	public static final String THEME_COLOR = "ThemeColor";

	/** The UIManager fonts before any {@link #apply()} changed them, to go back to. */
	private static Map<Object, Object> lookFonts;
	private static Font appliedFont;

	private final Theme theme;
	private final Font font;

	/**
	 * @param theme the theme
	 * @param font the font for everything, or null for the look and feel's own fonts
	 */
	public Appearance(Theme theme, Font font) {
		this.theme = Objects.requireNonNull(theme, "theme");
		this.font = font;
	}

	/** The gold theme and the look and feel's fonts. */
	public static Appearance defaults() {
		return new Appearance(Theme.GOLD, null);
	}

	/** What is in effect now. */
	public static Appearance current() {
		return new Appearance(Theme.current(), appliedFont);
	}

	/**
	 * The appearance saved in preferences; the defaults for what isn't there.
	 *
	 * @param prefs the application's preferences node, or null for the defaults
	 * @return the appearance
	 */
	public static Appearance load(Preferences prefs) {
		if( prefs == null ) {
			return defaults();
		}
		Theme theme = Theme.GOLD;
		String name = prefs.get(THEME, null);
		if( Theme.CUSTOM.equals(name) ) {
			theme = Theme.fromColor(new Color(prefs.getInt(THEME_COLOR, Theme.GOLD.getEnd().getRGB())));
		} else if( name != null && Theme.named(name) != null ) {
			theme = Theme.named(name);
		}
		Font font = null;
		String family = prefs.get(FONT_FAMILY, null);
		if( family != null ) {
			font = new Font(family, prefs.getInt(FONT_STYLE, Font.PLAIN), prefs.getInt(FONT_SIZE, 12));
		}
		return new Appearance(theme, font);
	}

	/**
	 * Saves the appearance in preferences. A null font removes the saved one.
	 *
	 * @param prefs the application's preferences node
	 */
	public void save(Preferences prefs) {
		prefs.put(THEME, theme.isCustom() ? Theme.CUSTOM : theme.getName());
		if( theme.isCustom() ) {
			prefs.putInt(THEME_COLOR, theme.getBaseColor().getRGB());
		} else {
			prefs.remove(THEME_COLOR);
		}
		if( font == null ) {
			prefs.remove(FONT_FAMILY);
			prefs.remove(FONT_STYLE);
			prefs.remove(FONT_SIZE);
		} else {
			prefs.put(FONT_FAMILY, font.getFamily());
			prefs.putInt(FONT_STYLE, font.getStyle());
			prefs.putInt(FONT_SIZE, font.getSize());
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// kept in memory; saved when the JVM next manages to
		}
	}

	/**
	 * Puts the appearance into effect: makes the theme current, sets the UIManager defaults (if the
	 * gradient look is installed) and the font, and updates the windows that are open.
	 */
	public void apply() {
		Theme old = Theme.current();
		Theme.setCurrent(theme);
		// macOS title bars and native controls, dark or not, now rather than at the next start
		if( old.isDark() != theme.isDark() && MacAppearance.follow(theme) ) {
			// AppKit changes it a moment later: the native parts are painted again then
			javax.swing.Timer later = new javax.swing.Timer(300, e->{
				for(Window w : Window.getWindows()) {
					if( w.isDisplayable() ) {
						SwingUtilities.updateComponentTreeUI(w);
						w.repaint();
					}
				}
			});
			later.setRepeats(false);
			later.start();
		}
		if( GradientLook.isInstalled() ) {
			GradientLook.install();
		}
		setFont(font);
		for(Window w : Window.getWindows()) {
			if( w.isDisplayable() ) {
				recolor(w, old);
				SwingUtilities.updateComponentTreeUI(w);
				w.repaint();
			}
		}
	}

	/** Sets every UIManager font to this one, or back to the look and feel's own for null. */
	private static synchronized void setFont(Font font) {
		UIDefaults defaults = UIManager.getDefaults();
		if( lookFonts == null ) {
			lookFonts = new HashMap<>();
			// keys(), not keySet(): before Java 12 or so, keySet() of UIManager's defaults has only the
			// application's own keys, none of the look and feel's, so no font would change
			for(Object key : java.util.Collections.list(defaults.keys())) {
				if( key.toString().toLowerCase().endsWith("font") ) {
					lookFonts.put(key, defaults.get(key));
				}
			}
		}
		for(Map.Entry<Object, Object> e : lookFonts.entrySet()) {
			defaults.put(e.getKey(), font == null ? e.getValue() : new FontUIResource(font));
		}
		appliedFont = font;
	}

	/**
	 * Components given the old theme's gradient colors by hand (code written before themes) get the
	 * new theme's. Those that leave their colors to the look follow the theme by themselves.
	 */
	private void recolor(Container c, Theme old) {
		for(Component k : c.getComponents()) {
			if( k instanceof GradientPanel || k instanceof javax.swing.JViewport ) {
				swap(k, old);
			}
			if( k instanceof Container ) {
				recolor((Container) k, old);
			}
		}
	}

	private void swap(Component k, Theme old) {
		if( !k.isBackgroundSet() || !k.isForegroundSet() ) {
			return;
		}
		Color bg = k.getBackground();
		Color fg = k.getForeground();
		if( !(bg instanceof javax.swing.plaf.UIResource) ) {
			if( old.getStart().equals(bg) ) {
				k.setBackground(theme.getStart());
			} else if( old.getEnd().equals(bg) ) {
				k.setBackground(theme.getEnd());
			}
		}
		if( !(fg instanceof javax.swing.plaf.UIResource) ) {
			if( old.getEnd().equals(fg) ) {
				k.setForeground(theme.getEnd());
			} else if( old.getStart().equals(fg) ) {
				k.setForeground(theme.getStart());
			}
		}
	}

	public Theme getTheme() {
		return theme;
	}

	/** The light color of the theme's gradient. */
	public Color getStart() {
		return theme.getStart();
	}

	/** The dark color of the theme's gradient. */
	public Color getEnd() {
		return theme.getEnd();
	}

	/** @return the font, or null for the look and feel's own */
	public Font getFont() {
		return font;
	}

	/** A copy with another theme. */
	public Appearance withTheme(Theme theme) {
		return new Appearance(theme, font);
	}

	/** A copy with another font (null for the look and feel's own). */
	public Appearance withFont(Font font) {
		return new Appearance(theme, font);
	}

	@Override
	public boolean equals(Object o) {
		if( !(o instanceof Appearance) ) {
			return false;
		}
		Appearance a = (Appearance) o;
		return theme.equals(a.theme) && Objects.equals(font, a.font);
	}

	@Override
	public int hashCode() {
		return Objects.hash(theme, font);
	}
}
