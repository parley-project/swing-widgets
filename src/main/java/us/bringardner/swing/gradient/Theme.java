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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A theme: a name and the colors of each part of the gradient look. The gradient runs from
 * {@link #getStart()} (light) to {@link #getEnd()} (dark); text, fields, selections, table headers,
 * outlines and gradient buttons each have their own color, so a dark theme reads as well as a
 * light one.
 * <p>
 * {@link #THEMES} are the built-in ones; {@link #fromColor(Color)} makes a theme from any one
 * color. {@link #current()} is the theme in effect: the gradient widgets paint with it, and
 * {@link GradientLook#install()} puts it into the UIManager defaults. Users choose a theme with
 * {@code SettingsDialog}; {@link Appearance} saves the choice.
 */
public final class Theme {

	/** The name of a theme made from one color. */
	public static final String CUSTOM = "Custom";

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

	static Theme fromColor(String name, Color color) {
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

	/** The gold theme the look has always had (and the default). */
	public static final Theme GOLD = gradient("Gold", GradientColors.DEFAULT_START, GradientColors.DEFAULT_END);

	/** The Parley palette: ink backgrounds, warm off-white text, gold accents. */
	public static final Theme DARK = new Theme("Dark",
			new Color(0x3A3428), new Color(0x0E0D0B),
			new Color(0xE8E2D0),
			new Color(0x17150F), new Color(0xE8E2D0),
			new Color(0x6B5620), new Color(0xF5EFDD),
			new Color(0x372C0F), new Color(0xD0AA42),
			new Color(0xD0AA42),
			new Color(0xF5EFDD), true);

	/** Nearly flat and pale, with dark text. */
	public static final Theme LIGHT = new Theme("Light",
			new Color(0xFFFFFF), new Color(0xDAD7CE),
			new Color(0x1E1E1E),
			Color.white, new Color(0x1E1E1E),
			new Color(0xD8CBA6), new Color(0x1E1E1E),
			new Color(0xCFCABD), new Color(0x1E1E1E),
			new Color(0x8A7B57),
			new Color(0x1E1E1E), false);

	/** The built-in themes, in the order they are offered: Gold (the default) first. */
	public static final List<Theme> THEMES = Collections.unmodifiableList(List.of(
			GOLD, DARK, LIGHT,
			fromColor("Copper", new Color(184, 115, 51)),
			fromColor("Sage", new Color(122, 150, 88)),
			fromColor("Ocean", new Color(52, 122, 168)),
			fromColor("Slate", new Color(96, 110, 132)),
			fromColor("Plum", new Color(132, 78, 138))));

	private static volatile Theme current = GOLD;

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

	/** The built-in theme with this name (ignoring case), or null. */
	public static Theme named(String name) {
		for(Theme t : THEMES) {
			if( t.name.equalsIgnoreCase(name) ) {
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

	public String getName() {
		return name;
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

	/** True for a theme made from one color with {@link #fromColor(Color)}. */
	public boolean isCustom() {
		return base != null;
	}

	/** The color a custom theme was made from, or null for a built-in one. */
	public Color getBaseColor() {
		return base;
	}

	@Override
	public boolean equals(Object o) {
		if( !(o instanceof Theme) ) {
			return false;
		}
		Theme t = (Theme) o;
		return name.equals(t.name) && start.equals(t.start) && end.equals(t.end) && text.equals(t.text)
				&& field.equals(t.field) && fieldText.equals(t.fieldText) && selection.equals(t.selection)
				&& selectionText.equals(t.selectionText) && header.equals(t.header)
				&& headerText.equals(t.headerText) && accent.equals(t.accent) && buttonText.equals(t.buttonText)
				&& dark == t.dark && Objects.equals(base, t.base);
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
