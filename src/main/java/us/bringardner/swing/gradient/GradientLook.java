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
		Color start = GradientColors.getStart();
		Color end = GradientColors.getEnd();
		Color clear = GradientColors.TRANSPARENT;
		UIDefaults d = UIManager.getDefaults();

		d.put("Panel.background", new ColorUIResource(start));
		d.put("Panel.foreground", new ColorUIResource(end));

		d.put("ScrollBarUI", ScrollBarUI.class.getCanonicalName());
		d.put("ScrollBar.track", new ColorUIResource(start));
		d.put("ScrollBar.thumb", new ColorUIResource(end));

		Icon node = icon("V15x15.png");
		d.put("Tree.selectionBackground", end);
		d.put("Tree.selectionForeground", start);
		d.put("Tree.collapsedIcon", new IconUIResource(icon("tree_closed16x16.png")));
		d.put("Tree.expandedIcon", new IconUIResource(icon("tree_open16x16.png")));
		d.put("Tree.openIcon", new IconUIResource(node));
		d.put("Tree.leafIcon", new IconUIResource(node));
		d.put("Tree.closedIcon", new IconUIResource(node));
		d.put("Tree.textBackground", clear);

		d.put("Table.gridColor", end);
		d.put("Table.background", clear);
		d.put("Table.textBackground", clear);
		d.put("TableHeader.background", new ColorUIResource(end));
		d.put("TableHeader.foreground", new ColorUIResource(end));
		d.put("Table.ascendingSortIcon", new IconUIResource(icon("UpArrow20x20.png")));
		d.put("Table.descendingSortIcon", new IconUIResource(icon("DnArrow20x20.png")));

		installed = true;
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
			header.setBackground(GradientColors.getEnd());
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
	 * Makes a panel and the plain panels in it see-through, so a gradient behind them shows. It
	 * stops at scroll panes, text components and other controls: only containers that are just
	 * layout are cleared.
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
}
