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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * A theme: a name and the colors of each part of the gradient look. The gradient runs from
 * {@link #getStart()} (light) to {@link #getEnd()} (dark); text, fields, selections, table headers,
 * outlines and gradient buttons each have their own color, so a dark theme reads as well as a
 * light one.
 * <p>
 * There are two kinds. The built-in themes ({@link #THEMES}) are {@code .theme} files in this jar.
 * The user's own themes, made with the theme designer in the appearance settings, are {@code .theme}
 * files in a folder in their home directory ({@link #userThemesDir()}), so every application built
 * on swing-widgets offers them. {@link #all()} is both. A theme file is a few {@code key=value}
 * lines ({@link #read}, {@link #write}), so one can be copied to another computer, or into this
 * jar's themes to make it a built-in one.
 * <p>
 * {@link #current()} is the theme in effect: the gradient widgets paint with it, and
 * {@link GradientLook#install()} puts it into the UIManager defaults. {@link Appearance} saves the
 * user's choice.
 */
public final class Theme {

	/** The name of a theme made from one color with {@link #fromColor(Color)}. */
	public static final String CUSTOM = "Custom";

	/** Where the built-in themes are, in this jar: one {@code <name>.theme} file each. */
	public static final String THEMES_PATH = "/us/bringardner/swing/themes/";

	/** The file listing the built-in themes' names, in the order they are offered. */
	public static final String THEMES_LIST = "themes.list";

	/** The file extension of a theme file. */
	public static final String EXTENSION = ".theme";

	/** A system property naming the user's themes folder, instead of {@code ~/.bringardner/themes}. */
	public static final String USER_THEMES_PROPERTY = "bringardner.themes.dir";

	/** The keys of a theme file's colors, in the order they are written and of {@link #colors()}. */
	public static final List<String> KEYS = List.of("start", "end", "text", "field", "fieldText", "selection",
			"selectionText", "header", "headerText", "accent", "buttonText");

	private final String name;
	private final Color start;
	private final Color end;
	private final Color text;
	private final Color field;
	private final Color fieldText;
	private final Color selection;
	private final Color selectionText;
	private final Color header;
	private final Color headerText;
	private final Color accent;
	private final Color buttonText;
	private final boolean dark;
	private final Color base;
	private String description;
	private boolean userTheme;

	/**
	 * @param name the name to show the user
	 * @param start the light end of the gradient (behind panels)
	 * @param end the dark end of the gradient
	 * @param text text on the gradient: labels, check boxes, trees and tables you see the gradient through
	 * @param field the background of text fields, text areas and lists
	 * @param fieldText text in fields
	 * @param selection the background of selected rows and text
	 * @param selectionText selected text
	 * @param header the background of table headers
	 * @param headerText table header text
	 * @param accent grid lines, outlines, scroll bar thumbs and the tree and sort arrows
	 * @param buttonText the text on gradient buttons
	 * @param dark true if the theme is dark: light text on dark backgrounds
	 */
	public Theme(String name, Color start, Color end, Color text, Color field, Color fieldText,
			Color selection, Color selectionText, Color header, Color headerText, Color accent,
			Color buttonText, boolean dark) {
		this(name, start, end, text, field, fieldText, selection, selectionText, header, headerText, accent,
				buttonText, dark, null);
	}

	private Theme(String name, Color start, Color end, Color text, Color field, Color fieldText,
			Color selection, Color selectionText, Color header, Color headerText, Color accent,
			Color buttonText, boolean dark, Color base) {
		this.name = Objects.requireNonNull(name, "name");
		this.start = Objects.requireNonNull(start, "start");
		this.end = Objects.requireNonNull(end, "end");
		this.text = Objects.requireNonNull(text, "text");
		this.field = Objects.requireNonNull(field, "field");
		this.fieldText = Objects.requireNonNull(fieldText, "fieldText");
		this.selection = Objects.requireNonNull(selection, "selection");
		this.selectionText = Objects.requireNonNull(selectionText, "selectionText");
		this.header = Objects.requireNonNull(header, "header");
		this.headerText = Objects.requireNonNull(headerText, "headerText");
		this.accent = Objects.requireNonNull(accent, "accent");
		this.buttonText = Objects.requireNonNull(buttonText, "buttonText");
		this.dark = dark;
		this.base = base;
	}

	/**
	 * A theme with these colors, in the order of {@link #KEYS}.
	 *
	 * @param name the name
	 * @param colors the colors, one for each of {@link #KEYS}
	 * @param dark true for light text on dark backgrounds
	 * @return the theme
	 */
	public static Theme of(String name, Color[] colors, boolean dark) {
		if( colors.length != KEYS.size() ) {
			throw new IllegalArgumentException("A theme has " + KEYS.size() + " colors, not " + colors.length);
		}
		Color[] c = colors;
		return new Theme(name, c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7], c[8], c[9], c[10], dark, null);
	}

	/**
	 * A light theme with a gradient between two colors, in the style of the gold one: black text,
	 * white fields, and the dark color (with white text) for selections, headers and outlines.
	 */
	public static Theme gradient(String name, Color start, Color end) {
		// white on the dark color for selected rows and headers: light-on-dark gold is too faint to read
		return new Theme(name, start, end, Color.black, Color.white, Color.black, end, Color.white, end,
				Color.white, end, Color.white, false);
	}

	/**
	 * A theme made from one color: a light shade of it for the start of the gradient and a dark one
	 * for the end, with the brightness of the gold theme's, so black text reads on the light end and
	 * white on the dark end whatever the color.
	 *
	 * @param color any color
	 * @return the theme, named {@link #CUSTOM}
	 */
	public static Theme fromColor(Color color) {
		return fromColor(CUSTOM, color);
	}

	/** As {@link #fromColor(Color)}, with a name. */
	public static Theme fromColor(String name, Color color) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		Color light = Color.getHSBColor(hsb[0], hsb[1] * 0.72f, 0.95f);
		Color darkShade = Color.getHSBColor(hsb[0], Math.min(1f, hsb[1]), 0.59f);
		Theme g = gradient(name, light, darkShade);
		return CUSTOM.equals(name) ? g.withBase(color) : g;
	}

	private Theme withBase(Color base) {
		return new Theme(name, start, end, text, field, fieldText, selection, selectionText, header, headerText,
				accent, buttonText, dark, base);
	}

	// ---------------------------------------------------------------- the built-in themes

	/**
	 * The built-in themes, in the order they are offered: Gold (the default) first. They are the
	 * {@code .theme} files in {@link #THEMES_PATH}, in the order of {@link #THEMES_LIST}.
	 */
	public static final List<Theme> THEMES = Collections.unmodifiableList(loadBuiltIns());

	/** The gold theme the look has always had (and the default). */
	public static final Theme GOLD = builtIn("Gold");

	/** The Parley palette: ink backgrounds, warm off-white text, gold accents. */
	public static final Theme DARK = builtIn("Dark");

	/** Nearly flat and pale, with dark text. */
	public static final Theme LIGHT = builtIn("Light");

	private static volatile Theme current = THEMES.get(0);

	private static List<Theme> loadBuiltIns() {
		List<Theme> ret = new ArrayList<>();
		for(String file : readList(Theme.class.getResourceAsStream(THEMES_PATH + THEMES_LIST))) {
			try (InputStream in = Theme.class.getResourceAsStream(THEMES_PATH + file + EXTENSION)) {
				if( in == null ) {
					throw new IllegalStateException(file + EXTENSION + " is listed in " + THEMES_LIST + " but missing");
				}
				ret.add(read(in));
			} catch (IOException e) {
				throw new IllegalStateException("Can't read the theme " + file, e);
			}
		}
		if( ret.isEmpty() ) {
			throw new IllegalStateException("No themes in " + THEMES_PATH + THEMES_LIST);
		}
		return ret;
	}

	private static Theme builtIn(String name) {
		for(Theme t : THEMES) {
			if( t.name.equals(name) ) {
				return t;
			}
		}
		// a themes.list without it: the first theme stands in
		return THEMES.get(0);
	}

	/**
	 * The names in a themes list: one per line; blank lines and lines starting with # are skipped.
	 *
	 * @param in the list, or null for none
	 * @return the names, in order
	 */
	public static List<String> readList(InputStream in) {
		List<String> ret = new ArrayList<>();
		if( in == null ) {
			return ret;
		}
		try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while( (line = r.readLine()) != null ) {
				line = line.trim();
				if( !line.isEmpty() && !line.startsWith("#") ) {
					ret.add(line);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Can't read a themes list", e);
		}
		return ret;
	}

	/** True if a built-in theme has this name (ignoring case): a user's theme can't take it. */
	public static boolean isBuiltInName(String name) {
		for(Theme t : THEMES) {
			if( t.name.equalsIgnoreCase(name.trim()) ) {
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- the user's themes

	/** The folder of the user's own themes: {@code ~/.bringardner/themes}, or {@link #USER_THEMES_PROPERTY}. */
	public static File userThemesDir() {
		String dir = System.getProperty(USER_THEMES_PROPERTY);
		if( dir != null && !dir.isBlank() ) {
			return new File(dir);
		}
		return new File(new File(System.getProperty("user.home"), ".bringardner"), "themes");
	}

	/**
	 * The user's own themes, by name. A file that can't be read is skipped, so one bad file doesn't
	 * cost the others.
	 */
	public static List<Theme> userThemes() {
		List<Theme> ret = new ArrayList<>();
		File[] files = userThemesDir().listFiles((d, n)->n.endsWith(EXTENSION));
		if( files != null ) {
			for(File f : files) {
				try (InputStream in = new FileInputStream(f)) {
					Theme t = read(in);
					if( !isBuiltInName(t.name) ) {
						t.userTheme = true;
						ret.add(t);
					}
				} catch (IOException | RuntimeException e) {
					// not a theme: leave it
				}
			}
		}
		ret.sort(Comparator.comparing(t->t.name.toLowerCase()));
		return ret;
	}

	/** The built-in themes, then the user's. */
	public static List<Theme> all() {
		List<Theme> ret = new ArrayList<>(THEMES);
		ret.addAll(userThemes());
		return ret;
	}

	/**
	 * Saves a theme as one of the user's own, replacing one of theirs with the same name.
	 *
	 * @param theme the theme; its name can't be a built-in one
	 * @return the saved theme, marked as the user's
	 * @throws IOException if it can't be written
	 * @throws IllegalArgumentException if the name is empty or a built-in theme's
	 */
	public static Theme saveUserTheme(Theme theme) throws IOException {
		String n = theme.name.trim();
		if( n.isEmpty() ) {
			throw new IllegalArgumentException("A theme needs a name");
		}
		if( isBuiltInName(n) ) {
			throw new IllegalArgumentException("\"" + n + "\" is a built-in theme's name");
		}
		File dir = userThemesDir();
		if( !dir.isDirectory() && !dir.mkdirs() ) {
			throw new IOException("Can't make the themes folder " + dir);
		}
		Theme saved = copy(theme, n);
		File target = fileFor(n);
		File old = findUserFile(n);
		if( old != null && old.getName().equalsIgnoreCase(target.getName()) ) {
			// the same file on a disk that ignores case (a rename from "sunrise" to "Sunrise")
			target = old;
		}
		try (OutputStream out = new FileOutputStream(target)) {
			saved.write(out);
		}
		if( old != null && !old.equals(target) ) {
			old.delete();
		}
		saved.userTheme = true;
		return saved;
	}

	/**
	 * Deletes one of the user's own themes.
	 *
	 * @param name its name
	 * @return true if it was there and is gone
	 */
	public static boolean deleteUserTheme(String name) {
		File f = findUserFile(name);
		return f != null && f.delete();
	}

	/** The file a user's theme with this name is in, or null. */
	private static File findUserFile(String name) {
		File[] files = userThemesDir().listFiles((d, n)->n.endsWith(EXTENSION));
		if( files != null ) {
			for(File f : files) {
				try (InputStream in = new FileInputStream(f)) {
					if( read(in).name.equalsIgnoreCase(name.trim()) ) {
						return f;
					}
				} catch (IOException | RuntimeException e) {
					// not a theme
				}
			}
		}
		return null;
	}

	/** The file name for a theme: its name with only letters, digits, spaces, - and _. */
	static File fileFor(String name) {
		String safe = name.trim().replaceAll("[^A-Za-z0-9 _-]", "_");
		return new File(userThemesDir(), safe + EXTENSION);
	}

	private static Theme copy(Theme t, String name) {
		Theme ret = new Theme(name, t.start, t.end, t.text, t.field, t.fieldText, t.selection, t.selectionText,
				t.header, t.headerText, t.accent, t.buttonText, t.dark, null);
		ret.description = t.description;
		return ret;
	}

	// ---------------------------------------------------------------- the file format

	/**
	 * Reads a theme file: {@code key=value} lines (a properties file) with {@code name}, optionally
	 * {@code description}, {@code dark} (true or false), and a {@code #RRGGBB} color for each of
	 * {@link #KEYS}.
	 *
	 * @param in the file
	 * @return the theme
	 * @throws IOException if it can't be read
	 * @throws IllegalArgumentException if a key is missing or a color can't be read
	 */
	public static Theme read(InputStream in) throws IOException {
		Properties p = new Properties();
		p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		String name = p.getProperty("name");
		if( name == null || name.isBlank() ) {
			throw new IllegalArgumentException("A theme needs a name");
		}
		Color[] c = new Color[KEYS.size()];
		for(int i = 0; i < c.length; i++) {
			c[i] = color(p.getProperty(KEYS.get(i)), KEYS.get(i), name);
		}
		Theme t = of(name.trim(), c, Boolean.parseBoolean(p.getProperty("dark", "false").trim()));
		String d = p.getProperty("description");
		t.description = d == null || d.isBlank() ? null : d.trim();
		return t;
	}

	private static Color color(String value, String key, String theme) {
		if( value == null ) {
			throw new IllegalArgumentException("The theme " + theme + " has no " + key);
		}
		try {
			return Color.decode(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("The theme " + theme + "'s " + key + " isn't a color: " + value);
		}
	}

	/**
	 * Writes the theme in the format {@link #read} reads.
	 *
	 * @param out where to write it
	 * @throws IOException if it can't be written
	 */
	public void write(OutputStream out) throws IOException {
		// written by hand rather than with Properties.store, to keep the keys in order and add notes
		StringBuilder b = new StringBuilder();
		b.append("# A theme for the gradient look (us.bringardner.swing.gradient.Theme). Colors are #RRGGBB.\n");
		b.append("# start/end: the gradient, light to dark; text: text on the gradient; field/fieldText: text\n");
		b.append("# fields, text areas and lists; selection/selectionText: selected rows and text;\n");
		b.append("# header/headerText: table headers; accent: grid lines, outlines, scroll bars and the tree and\n");
		b.append("# sort arrows; buttonText: the text on gradient buttons; dark: true for light text on dark.\n");
		b.append(line("name", name));
		if( description != null && !description.isBlank() ) {
			b.append(line("description", description.trim()));
		}
		b.append("dark=").append(dark).append('\n');
		Color[] c = colors();
		for(int i = 0; i < c.length; i++) {
			b.append(KEYS.get(i)).append('=').append(hex(c[i])).append('\n');
		}
		out.write(b.toString().getBytes(StandardCharsets.UTF_8));
		out.flush();
	}

	/** One key=value line, escaped as Properties would write it. */
	private static String line(String key, String value) throws IOException {
		Properties p = new Properties();
		p.setProperty(key, value);
		java.io.StringWriter w = new java.io.StringWriter();
		p.store(w, null);
		// store writes a date comment first; the last line is ours
		String[] lines = w.toString().split("\\R");
		return lines[lines.length - 1] + "\n";
	}

	/** The colors in the order of {@link #KEYS}. */
	public Color[] colors() {
		return new Color[] {start, end, text, field, fieldText, selection, selectionText, header, headerText,
				accent, buttonText};
	}

	/** #RRGGBB. */
	public static String hex(Color c) {
		return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
	}

	/**
	 * How well one color reads on another: the WCAG contrast ratio, from 1 (none) to 21 (black on
	 * white). Text wants 4.5 or more; large or bold text, and parts like selected rows, 3 or more.
	 */
	public static double contrast(Color a, Color b) {
		double la = luminance(a);
		double lb = luminance(b);
		return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
	}

	private static double luminance(Color c) {
		double[] v = {c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0};
		for(int i = 0; i < 3; i++) {
			v[i] = v[i] <= 0.03928 ? v[i] / 12.92 : Math.pow((v[i] + 0.055) / 1.055, 2.4);
		}
		return 0.2126 * v[0] + 0.7152 * v[1] + 0.0722 * v[2];
	}

	// ---------------------------------------------------------------- current

	/** The theme in effect. */
	public static Theme current() {
		return current;
	}

	/**
	 * Makes a theme the one in effect for gradient widgets painted from now on. To update the
	 * UIManager defaults and the windows already open too, use {@link Appearance#apply()}.
	 */
	public static void setCurrent(Theme theme) {
		current = Objects.requireNonNull(theme, "theme");
	}

	/** The built-in or user's theme with this name (ignoring case), or null. */
	public static Theme named(String name) {
		if( name == null ) {
			return null;
		}
		for(Theme t : all()) {
			if( t.name.equalsIgnoreCase(name.trim()) ) {
				return t;
			}
		}
		return null;
	}

	/** A copy with another gradient; it is no longer one of the built-in themes. */
	Theme withGradient(Color start, Color end) {
		return new Theme(CUSTOM, start, end, text, field, fieldText, selection, selectionText, header, headerText,
				accent, buttonText, dark, null);
	}

	/** A copy with another name, for saving as a new theme. */
	public Theme withName(String name) {
		return copy(this, name);
	}

	/** A copy with a description (null for none). */
	public Theme withDescription(String description) {
		Theme t = copy(this, name);
		t.description = description;
		t.userTheme = userTheme;
		return t;
	}

	public String getName() {
		return name;
	}

	/** A short description to show the user, or null. */
	public String getDescription() {
		return description;
	}

	public Color getStart() {
		return start;
	}

	public Color getEnd() {
		return end;
	}

	public Color getText() {
		return text;
	}

	public Color getField() {
		return field;
	}

	public Color getFieldText() {
		return fieldText;
	}

	public Color getSelection() {
		return selection;
	}

	public Color getSelectionText() {
		return selectionText;
	}

	public Color getHeader() {
		return header;
	}

	public Color getHeaderText() {
		return headerText;
	}

	public Color getAccent() {
		return accent;
	}

	public Color getButtonText() {
		return buttonText;
	}

	public boolean isDark() {
		return dark;
	}

	/** True for a theme made from one color with {@link #fromColor(Color)} and not saved. */
	public boolean isCustom() {
		return base != null;
	}

	/** True for one of the user's own themes ({@link #userThemes()}). */
	public boolean isUserTheme() {
		return userTheme;
	}

	/** The color a custom theme was made from, or null. */
	public Color getBaseColor() {
		return base;
	}

	/** True if the colors are the same as another theme's, whatever the names. */
	public boolean sameColors(Theme t) {
		return t != null && dark == t.dark && java.util.Arrays.equals(colors(), t.colors());
	}

	@Override
	public boolean equals(Object o) {
		if( !(o instanceof Theme) ) {
			return false;
		}
		Theme t = (Theme) o;
		return name.equals(t.name) && sameColors(t) && Objects.equals(base, t.base);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, start, end, base);
	}

	@Override
	public String toString() {
		return name;
	}
}
