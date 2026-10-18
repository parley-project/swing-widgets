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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.RootPaneContainer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.IconUIResource;

import us.bringardner.swing.ui.GradientButtonUI;
import us.bringardner.swing.ui.ScrollBarUI;

/**
 * The gradient look, in one call: what parley-files-viewer looks like, for any Swing application.
 * <p>
 * {@link #install()} sets the UIManager defaults so that panels are the light gradient color,
 * scroll bars are gradient, trees and tables are see-through with the dark color for grid lines,
 * the table header and the tree selection, and the tree and sort icons are the gold arrows. Call
 * it once, before creating any windows. The colors are {@link GradientColors}', so set those
 * first for other colors.
 * <p>
 * The gradient itself comes from a {@link GradientPanel}: make it a window's content pane, and
 * use {@link #seeThrough(JScrollPane)} on scroll panes whose tree or table should show it.
 */
public final class GradientLook {

	/** Where the icons are, in this jar. */
	public static final String ICONS = "/us/bringardner/swing/icons/";

	private static volatile boolean installed;

	private GradientLook() {
	}

	/** True once {@link #install()} has run. */
	public static boolean isInstalled() {
		return installed;
	}

	/**
	 * Sets the UIManager defaults for the gradient look. Call it before creating any windows; it
	 * works with the current look and feel (Aqua on macOS, Metal elsewhere).
	 */
	public static void install() {
		Theme t = Theme.current();
		UIDefaults d = UIManager.getDefaults();
		rememberLook(d);
		Color clear = GradientColors.TRANSPARENT;

		// the gradient behind panels
		put(d, "Panel.background", t.getStart());
		put(d, "Panel.foreground", t.getEnd());

		d.put("ScrollBarUI", ScrollBarUI.class.getCanonicalName());
		// buttons with text in the theme's colors, not the look and feel's (Aqua's default button is
		// the macOS accent color); icon-only buttons keep the look and feel's
		d.put("ButtonUI", GradientButtonUI.class.getName());
		put(d, "ScrollBar.track", t.getStart());
		put(d, "ScrollBar.thumb", t.getAccent());

		// text on the gradient; light themes keep the look and feel's own dark text
		boolean ownText = !t.isDark() && Color.black.equals(t.getText());
		for(String key : TEXT_KEYS) {
			role(d, key, ownText ? null : t.getText());
		}

		// fields, text areas and lists; light themes with white fields keep the look's own
		boolean ownFields = !t.isDark() && Color.white.equals(t.getField()) && Color.black.equals(t.getFieldText());
		for(String prefix : FIELD_PREFIXES) {
			role(d, prefix + ".background", ownFields ? null : t.getField());
			role(d, prefix + ".foreground", ownFields ? null : t.getFieldText());
			role(d, prefix + ".caretForeground", ownFields ? null : t.getFieldText());
			role(d, prefix + ".inactiveBackground", ownFields ? null : t.getField());
			role(d, prefix + ".selectionBackground", ownFields ? null : t.getSelection());
			role(d, prefix + ".selectionForeground", ownFields ? null : t.getSelectionText());
		}
		role(d, "Viewport.background", ownFields ? null : t.getField());
		if( !isAqua() ) {
			// Aqua draws these natively (dark ones with a dark macOS appearance); elsewhere the theme does
			for(String key : new String[] {"ComboBox.background", "PopupMenu.background", "MenuItem.background",
					"Menu.background", "MenuBar.background", "CheckBoxMenuItem.background", "RadioButtonMenuItem.background"}) {
				role(d, key, t.isDark() ? t.getField() : null);
			}
			for(String key : new String[] {"ComboBox.foreground", "MenuItem.foreground", "Menu.foreground",
					"CheckBoxMenuItem.foreground", "RadioButtonMenuItem.foreground"}) {
				role(d, key, t.isDark() ? t.getFieldText() : null);
			}
			// Metal paints the menu bar with its own light gradient: a dark theme paints it plainly
			if( t.isDark() ) {
				d.put("MenuBarUI", javax.swing.plaf.basic.BasicMenuBarUI.class.getName());
			} else if( lookValues.get("MenuBarUI") != null ) {
				d.put("MenuBarUI", lookValues.get("MenuBarUI"));
			}
			for(String key : new String[] {"MenuItem.selectionBackground", "Menu.selectionBackground",
					"ComboBox.selectionBackground"}) {
				role(d, key, t.isDark() ? t.getSelection() : null);
			}
			for(String key : new String[] {"MenuItem.selectionForeground", "Menu.selectionForeground",
					"ComboBox.selectionForeground"}) {
				role(d, key, t.isDark() ? t.getSelectionText() : null);
			}
		}

		// trees and tables: see-through, the theme's selection, grid, header and arrows
		put(d, "Tree.selectionBackground", t.getSelection());
		put(d, "Tree.selectionForeground", t.getSelectionText());
		put(d, "Tree.selectionBorderColor", t.getSelection());
		Icon node = icon("V15x15.png", t);
		d.put("Tree.collapsedIcon", new IconUIResource(icon("tree_closed16x16.png", t)));
		d.put("Tree.expandedIcon", new IconUIResource(icon("tree_open16x16.png", t)));
		d.put("Tree.openIcon", new IconUIResource(node));
		d.put("Tree.leafIcon", new IconUIResource(node));
		d.put("Tree.closedIcon", new IconUIResource(node));
		d.put("Tree.textBackground", clear);

		put(d, "Table.gridColor", t.getAccent());
		d.put("Table.background", clear);
		d.put("Table.textBackground", clear);
		put(d, "Table.selectionBackground", t.getSelection());
		put(d, "Table.selectionForeground", t.getSelectionText());
		put(d, "TableHeader.background", t.getHeader());
		put(d, "TableHeader.foreground", t.getHeaderText());
		d.put("Table.ascendingSortIcon", new IconUIResource(icon("UpArrow20x20.png", t)));
		d.put("Table.descendingSortIcon", new IconUIResource(icon("DnArrow20x20.png", t)));

		installed = true;
	}

	/** Keys for text drawn on the gradient. */
	static final String[] TEXT_KEYS = {"Label.foreground", "CheckBox.foreground", "RadioButton.foreground",
			"TitledBorder.titleColor", "Tree.textForeground", "Tree.foreground", "Table.foreground",
			"TabbedPane.foreground", "Slider.foreground", "OptionPane.messageForeground"};

	/** Components with a field background. */
	static final String[] FIELD_PREFIXES = {"TextField", "FormattedTextField", "PasswordField", "TextArea",
			"TextPane", "EditorPane", "List", "Spinner"};

	/** The look and feel's values for the keys the theme may change, to go back to. */
	private static java.util.Map<Object, Object> lookValues;

	private static synchronized void rememberLook(UIDefaults d) {
		if( lookValues != null ) {
			return;
		}
		lookValues = new java.util.HashMap<>();
		// keys(), not keySet(): on older Java, keySet() of UIManager's defaults misses the look's keys
		for(Object key : java.util.Collections.list(d.keys())) {
			lookValues.put(key, d.get(key));
		}
	}

	/** Sets a key to a color, or back to the look and feel's own for null. */
	private static void role(UIDefaults d, String key, Color c) {
		if( c != null ) {
			put(d, key, c);
		} else if( lookValues.containsKey(key) ) {
			d.put(key, lookValues.get(key));
		} else {
			d.remove(key);
		}
	}

	private static void put(UIDefaults d, String key, Color c) {
		d.put(key, new ColorUIResource(c));
	}

	static boolean isAqua() {
		return "Mac OS X".equals(UIManager.getLookAndFeel().getName())
				|| UIManager.getLookAndFeel().getClass().getName().contains("Aqua");
	}

	/**
	 * Installs the gradient look with the appearance the user saved (with {@code SettingsDialog}):
	 * their colors and font, or the defaults for what they haven't chosen. Call it once, before
	 * creating any windows, instead of {@link #install()}.
	 *
	 * @param prefs the application's preferences node, or null for the defaults
	 * @return the appearance now in effect
	 */
	public static Appearance install(java.util.prefs.Preferences prefs) {
		Appearance a = Appearance.load(prefs);
		if( a.getTheme().isDark() && System.getProperty("os.name", "").toLowerCase().startsWith("mac")
				&& System.getProperty("apple.awt.application.appearance") == null ) {
			// native macOS controls and title bars go dark too, if AWT hasn't started yet
			System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
		}
		a.apply();
		install();
		return a;
	}

	/**
	 * Lets the gradient behind a scroll pane show through it and its tree, table or list, and gives
	 * a table's header the dark color, as in parley-files-viewer.
	 *
	 * @param scrollPane the scroll pane
	 * @return the scroll pane
	 */
	public static JScrollPane seeThrough(JScrollPane scrollPane) {
		clear(scrollPane);
		clear(scrollPane.getViewport());
		Component view = scrollPane.getViewport().getView();
		if( view instanceof JComponent ) {
			clear((JComponent) view);
		}
		// A table's header only arrives when the table is shown, so style it now if it is here,
		// and whenever it is set
		styleHeader(scrollPane.getColumnHeader());
		scrollPane.addPropertyChangeListener("columnHeader", e->styleHeader(scrollPane.getColumnHeader()));
		return scrollPane;
	}

	private static void styleHeader(JViewport header) {
		if( header != null ) {
			header.setBackground(Theme.current().getHeader());
			header.setOpaque(false);
		}
	}

	/**
	 * Puts a window on the gradient: its content goes inside a {@link GradientPanel}, its plain
	 * layout panels and split panes become see-through, and so do its trees, tables and lists
	 * (with {@link #seeThrough(JScrollPane)}). Call it once the window's content is built.
	 *
	 * @param window a JFrame, JDialog or other root pane container
	 * @return the window
	 */
	public static <W extends RootPaneContainer> W apply(W window) {
		Container content = window.getContentPane();
		if( !(content instanceof GradientPanel) ) {
			GradientPanel gradient = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
			gradient.setLayout(new BorderLayout());
			if( content instanceof JComponent ) {
				// keep the border (often the window's margin) on the outside
				gradient.setBorder(((JComponent) content).getBorder());
				((JComponent) content).setBorder(null);
			}
			gradient.add(content, BorderLayout.CENTER);
			window.setContentPane(gradient);
		}
		seeThroughPanels(content);
		seeThroughViews(content);
		return window;
	}

	/**
	 * For a library's windows: puts the window on the gradient if the application installed the
	 * gradient look, and leaves it alone otherwise. Call it once the window's content is built.
	 *
	 * @param window a JFrame, JDialog or other root pane container
	 * @return the window
	 */
	public static <W extends RootPaneContainer> W follow(W window) {
		return installed ? apply(window) : window;
	}

	/** seeThrough every scroll pane in a container whose view is a tree, table or list. */
	private static void seeThroughViews(Container container) {
		for(Component c : container.getComponents()) {
			if( c instanceof JScrollPane ) {
				Component view = ((JScrollPane) c).getViewport().getView();
				if( view instanceof javax.swing.JTable || view instanceof javax.swing.JTree
						|| view instanceof javax.swing.JList ) {
					seeThrough((JScrollPane) c);
				}
			} else if( c instanceof Container ) {
				seeThroughViews((Container) c);
			}
		}
	}

	/**
	 * Makes a panel and the plain panels in it see-through, so a gradient behind them shows, and the
	 * check boxes and radio buttons in them too. It stops at scroll panes, text components and other
	 * controls: only containers that are just layout are cleared.
	 *
	 * @param panel the panel
	 */
	public static void seeThroughPanels(Container panel) {
		if( panel instanceof javax.swing.JPanel && !(panel instanceof GradientPanel) ) {
			((JComponent) panel).setOpaque(false);
		}
		for(Component c : panel.getComponents()) {
			if( c instanceof javax.swing.JPanel && !(c instanceof GradientPanel) ) {
				seeThroughPanels((Container) c);
			} else if( c instanceof javax.swing.JCheckBox || c instanceof javax.swing.JRadioButton ) {
				// check boxes and radio buttons paint a block of the look's color behind them otherwise
				((JComponent) c).setOpaque(false);
			} else if( c instanceof javax.swing.JSplitPane || c instanceof javax.swing.JToolBar ) {
				((JComponent) c).setOpaque(false);
				seeThroughPanels((Container) c);
			}
		}
	}

	private static void clear(JComponent c) {
		c.setOpaque(false);
		c.setBackground(GradientColors.TRANSPARENT);
	}

	private static Icon icon(String name) {
		return new ImageIcon(GradientLook.class.getResource(ICONS + name));
	}

	/** One of the look's icons, tinted to the theme's accent (the gold theme's are as drawn). */
	static Icon icon(String name, Theme t) {
		ImageIcon gold = (ImageIcon) icon(name);
		if( t.getAccent().equals(Theme.GOLD.getAccent()) ) {
			return gold;
		}
		return new ImageIcon(tint(gold.getImage(), t.getAccent()));
	}

	/**
	 * Gives an image the hue of a color, keeping its shading: each pixel keeps its brightness and
	 * transparency, takes the color's hue and, unless it is close to gray (white outlines), its
	 * saturation.
	 */
	static java.awt.image.BufferedImage tint(java.awt.Image image, Color color) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		java.awt.image.BufferedImage src = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = src.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		float[] target = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		java.awt.image.BufferedImage ret = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				int argb = src.getRGB(x, y);
				int a = argb >>> 24;
				float[] hsb = Color.RGBtoHSB((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, null);
				float sat = hsb[1] < 0.12f ? hsb[1] : target[1];
				int rgb = Color.HSBtoRGB(target[0], sat, hsb[2]) & 0xffffff;
				ret.setRGB(x, y, (a << 24) | rgb);
			}
		}
		return ret;
	}
}
