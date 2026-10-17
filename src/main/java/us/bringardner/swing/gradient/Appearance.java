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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * What a user can choose about the gradient look: its colors (a {@link Scheme}) and the font.
 * {@link #load(Preferences)} and {@link #save(Preferences)} keep it in an application's
 * preferences; {@link #apply()} puts it into effect, in the windows already open too.
 * {@code SettingsDialog} is the dialog for choosing it.
 */
public final class Appearance {

	/** Preference keys. The font keys are the ones parley-files-viewer has always used. */
	public static final String FONT_FAMILY = "FontFamily";
	public static final String FONT_STYLE = "FontStyle";
	public static final String FONT_SIZE = "FontSize";
	public static final String GRADIENT_START = "GradientStart";
	public static final String GRADIENT_END = "GradientEnd";

	/**
	 * A color scheme: the light color the gradient starts with (top left) and the dark one it ends
	 * with (bottom right), and a name to show the user.
	 */
	public static final class Scheme {

		private final String name;
		private final Color start;
		private final Color end;

		public Scheme(String name, Color start, Color end) {
			this.name = Objects.requireNonNull(name, "name");
			this.start = Objects.requireNonNull(start, "start");
			this.end = Objects.requireNonNull(end, "end");
		}

		/**
		 * A scheme made from one color: a light shade of it for the start and a dark one for the
		 * end, with the brightness of the gold scheme's, so black text reads on the light end and
		 * white text on the dark end whatever the color.
		 *
		 * @param name the name to show
		 * @param color any color
		 * @return the scheme
		 */
		public static Scheme fromColor(String name, Color color) {
			float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
			Color light = Color.getHSBColor(hsb[0], hsb[1] * 0.72f, 0.95f);
			Color dark = Color.getHSBColor(hsb[0], Math.min(1f, hsb[1]), 0.59f);
			return new Scheme(name, light, dark);
		}

		public String getName() {
			return name;
		}

		public Color getStart() {
			return start;
		}

		public Color getEnd() {
			return end;
		}

		/** True if the scheme has these colors, whatever its name. */
		public boolean matches(Color start, Color end) {
			return this.start.equals(start) && this.end.equals(end);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/** The gold scheme the look has always had: {@link GradientColors}' defaults. */
	public static final Scheme GOLD = new Scheme("Gold", GradientColors.DEFAULT_START, GradientColors.DEFAULT_END);

	/** The schemes to choose from, gold first. */
	public static final List<Scheme> SCHEMES = Collections.unmodifiableList(List.of(
			GOLD,
			Scheme.fromColor("Copper", new Color(184, 115, 51)),
			Scheme.fromColor("Sage", new Color(122, 150, 88)),
			Scheme.fromColor("Ocean", new Color(52, 122, 168)),
			Scheme.fromColor("Slate", new Color(96, 110, 132)),
			Scheme.fromColor("Plum", new Color(132, 78, 138))));

	/** The UIManager fonts before any {@link #apply()} changed them, to go back to. */
	private static Map<Object, Object> lookFonts;
	private static Font appliedFont;

	private final Color start;
	private final Color end;
	private final Font font;

	/**
	 * @param start the light color
	 * @param end the dark color
	 * @param font the font for everything, or null for the look and feel's own fonts
	 */
	public Appearance(Color start, Color end, Font font) {
		this.start = Objects.requireNonNull(start, "start");
		this.end = Objects.requireNonNull(end, "end");
		this.font = font;
	}

	/** The gold scheme and the look and feel's fonts. */
	public static Appearance defaults() {
		return new Appearance(GOLD.getStart(), GOLD.getEnd(), null);
	}

	/** What is in effect now. */
	public static Appearance current() {
		return new Appearance(GradientColors.getStart(), GradientColors.getEnd(), appliedFont);
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
		Color start = new Color(prefs.getInt(GRADIENT_START, GOLD.getStart().getRGB()));
		Color end = new Color(prefs.getInt(GRADIENT_END, GOLD.getEnd().getRGB()));
		Font font = null;
		String family = prefs.get(FONT_FAMILY, null);
		if( family != null ) {
			font = new Font(family, prefs.getInt(FONT_STYLE, Font.PLAIN), prefs.getInt(FONT_SIZE, 12));
		}
		return new Appearance(start, end, font);
	}

	/**
	 * Saves the appearance in preferences. A null font removes the saved one.
	 *
	 * @param prefs the application's preferences node
	 */
	public void save(Preferences prefs) {
		prefs.putInt(GRADIENT_START, start.getRGB());
		prefs.putInt(GRADIENT_END, end.getRGB());
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
	 * Puts the appearance into effect: sets {@link GradientColors}, the UIManager defaults (if the
	 * gradient look is installed) and the font, and updates the windows that are open.
	 */
	public void apply() {
		Color oldStart = GradientColors.getStart();
		Color oldEnd = GradientColors.getEnd();
		GradientColors.setStart(start);
		GradientColors.setEnd(end);
		if( GradientLook.isInstalled() ) {
			GradientLook.install();
		}
		setFont(font);
		for(Window w : Window.getWindows()) {
			if( w.isDisplayable() ) {
				recolor(w, oldStart, oldEnd);
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
			for(Object key : new ArrayList<>(defaults.keySet())) {
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
	 * Gives the components that were painted with the old colors the new ones: gradient panels
	 * and buttons and table headers set their colors when they are made, so the look and feel
	 * doesn't update them.
	 */
	private void recolor(Container c, Color oldStart, Color oldEnd) {
		for(Component k : c.getComponents()) {
			if( k instanceof GradientPanel || k instanceof GradientButton || k instanceof JViewport ) {
				if( oldStart.equals(k.getBackground()) ) {
					k.setBackground(start);
				} else if( oldEnd.equals(k.getBackground()) ) {
					k.setBackground(end);
				}
				if( oldEnd.equals(k.getForeground()) ) {
					k.setForeground(end);
				} else if( oldStart.equals(k.getForeground()) ) {
					k.setForeground(start);
				}
			}
			if( k instanceof Container ) {
				recolor((Container) k, oldStart, oldEnd);
			}
		}
	}

	public Color getStart() {
		return start;
	}

	public Color getEnd() {
		return end;
	}

	/** @return the font, or null for the look and feel's own */
	public Font getFont() {
		return font;
	}

	/** The scheme with these colors from {@link #SCHEMES}, or null if they are custom. */
	public Scheme getScheme() {
		for(Scheme s : SCHEMES) {
			if( s.matches(start, end) ) {
				return s;
			}
		}
		return null;
	}

	/** A copy with another scheme's colors. */
	public Appearance withScheme(Scheme scheme) {
		return new Appearance(scheme.getStart(), scheme.getEnd(), font);
	}

	/** A copy with another font (null for the look and feel's own). */
	public Appearance withFont(Font font) {
		return new Appearance(start, end, font);
	}

	@Override
	public boolean equals(Object o) {
		if( !(o instanceof Appearance) ) {
			return false;
		}
		Appearance a = (Appearance) o;
		return start.equals(a.start) && end.equals(a.end) && Objects.equals(font, a.font);
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end, font);
	}
}
