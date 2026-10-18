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
package us.bringardner.swing.menu;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.BooleanSupplier;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * The menus every application has, in the same order and places in all of them: File, Edit, View,
 * the application's own menus, Window and Help.
 * <p>
 * About, Settings... and Quit follow the platform: on macOS they are in the application menu at the
 * top of the screen (with ⌘, and ⌘Q); elsewhere Settings... and Exit end the File menu and About
 * is in Help. Shortcuts use the platform's shortcut key: ⌘ on macOS, Ctrl elsewhere.
 * <p>
 * An application fills {@link #file}, {@link #edit} and {@link #view} with its own items (empty menus
 * are left out), passes its own menus to {@link #install}, and gets a menu bar for each window: on
 * macOS at the top of the screen ({@link #useScreenMenuBar}), elsewhere in the window.
 *
 * <pre>
 * StandardMenus.useScreenMenuBar("My App");          // in main, first
 * ...
 * StandardMenus menus = new StandardMenus("My App")
 *         .about(this::showAbout).settings(this::showSettings).quit(this::okToQuit);
 * menus.file.add(StandardMenus.item("Open...", KeyEvent.VK_O, this::open));
 * menus.install(frame, runMenu);
 * </pre>
 */
public class StandardMenus {

	/** The standard menus; the application adds its items to them. */
	public final JMenu file = new JMenu("File");
	public final JMenu edit = new JMenu("Edit");
	public final JMenu view = new JMenu("View");
	public final JMenu window = new JMenu("Window");
	public final JMenu help = new JMenu("Help");

	private final String appName;
	private Runnable about;
	private Runnable settings;
	private BooleanSupplier quit;

	/** @param appName the application's name, as About shows it ("About fsh-ide") */
	public StandardMenus(String appName) {
		this.appName = appName;
	}

	/** True on macOS, where About, Settings and Quit are in the application menu. */
	public static boolean isMac() {
		return System.getProperty("os.name", "").toLowerCase().startsWith("mac");
	}

	/**
	 * On macOS, puts each window's menu bar at the top of the screen and names the application menu.
	 * Call it first thing in main, before any window is made. Elsewhere it does nothing.
	 */
	public static void useScreenMenuBar(String appName) {
		if( isMac() ) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("apple.awt.application.name", appName);
		}
	}

	/**
	 * The platform's shortcut key: ⌘ on macOS, Ctrl elsewhere. Headless (a test with no display) the
	 * toolkit can't say, so it is worked out from the platform.
	 */
	public static int shortcut() {
		if( java.awt.GraphicsEnvironment.isHeadless() ) {
			return isMac() ? java.awt.event.InputEvent.META_DOWN_MASK : java.awt.event.InputEvent.CTRL_DOWN_MASK;
		}
		return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
	}

	/** The shortcut key and a key, with any other modifiers (InputEvent.SHIFT_DOWN_MASK and so on). */
	public static KeyStroke keys(int key, int modifiers) {
		return KeyStroke.getKeyStroke(key, shortcut() | modifiers);
	}

	/** A menu item that runs an action. */
	public static JMenuItem item(String text, Runnable action) {
		JMenuItem item = new JMenuItem(text);
		item.addActionListener(e->action.run());
		return item;
	}

	/** A menu item with the shortcut key and a key (KeyEvent.VK_...). */
	public static JMenuItem item(String text, int key, Runnable action) {
		return item(text, keys(key, 0), action);
	}

	/** A menu item with a key stroke (or none, for null). */
	public static JMenuItem item(String text, KeyStroke keys, Runnable action) {
		JMenuItem item = item(text, action);
		if( keys != null ) {
			item.setAccelerator(keys);
		}
		return item;
	}

	/** What About shows. */
	public StandardMenus about(Runnable action) {
		this.about = action;
		return this;
	}

	/** What Settings... opens. */
	public StandardMenus settings(Runnable action) {
		this.settings = action;
		return this;
	}

	/**
	 * What Quit (Exit) does: close the windows, asking about unsaved work if need be, and say whether
	 * the application may end. If it may, the application exits.
	 */
	public StandardMenus quit(BooleanSupplier okToQuit) {
		this.quit = okToQuit;
		return this;
	}

	/**
	 * Gives a window the menu bar: File, Edit, View, the application's menus, Window and Help (a
	 * menu with nothing in it is left out). On macOS, About, Settings and Quit go to the application
	 * menu; elsewhere into File and Help.
	 *
	 * @param frame the window
	 * @param appMenus the application's own menus, between View and Window
	 * @return the menu bar
	 */
	public JMenuBar install(JFrame frame, JMenu... appMenus) {
		JMenuBar bar = build(frame, isMac(), appMenus);
		frame.setJMenuBar(bar);
		if( isMac() ) {
			appMenuHandlers();
		}
		return bar;
	}

	/** The menu bar for a window, as on macOS (mac) or elsewhere: for install, and for tests. */
	JMenuBar build(JFrame frame, boolean mac, JMenu... appMenus) {
		if( !mac ) {
			if( settings != null ) {
				separate(file);
				file.add(item("Settings...", KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, shortcut()), settings));
			}
			if( quit != null ) {
				separate(file);
				file.add(item("Exit", null, this::quit));
			}
			if( about != null ) {
				separate(help);
				help.add(item("About " + appName, about));
			}
		}
		windowMenu(frame);
		JMenuBar bar = new JMenuBar();
		for(JMenu m : new JMenu[] {file, edit, view}) {
			if( m.getMenuComponentCount() > 0 ) {
				bar.add(m);
			}
		}
		for(JMenu m : appMenus) {
			bar.add(m);
		}
		bar.add(window);
		if( help.getMenuComponentCount() > 0 ) {
			bar.add(help);
		}
		return bar;
	}

	private static void separate(JMenu menu) {
		if( menu.getMenuComponentCount() > 0 ) {
			menu.addSeparator();
		}
	}

	/** Window: Minimize, then the application's open windows, the one in front checked. */
	private void windowMenu(JFrame frame) {
		JMenuItem minimize = item("Minimize", KeyEvent.VK_M, ()->frame.setExtendedState(frame.getExtendedState() | Frame.ICONIFIED));
		window.add(minimize);
		window.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				listWindows(frame);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		listWindows(frame);
	}

	/** The open windows under Minimize: the application's frames that are showing. */
	void listWindows(JFrame frame) {
		while( window.getMenuComponentCount() > 1 ) {
			window.remove(1);
		}
		boolean first = true;
		for(Frame f : Frame.getFrames()) {
			if( f.isDisplayable() && f.isVisible() && f instanceof JFrame && ((JFrame) f).getJMenuBar() != null ) {
				if( first ) {
					window.addSeparator();
					first = false;
				}
				String title = f.getTitle() == null || f.getTitle().isEmpty() ? appName : f.getTitle();
				JCheckBoxMenuItem item = new JCheckBoxMenuItem(title, f == frame);
				item.addActionListener(e->{
					f.setExtendedState(f.getExtendedState() & ~Frame.ICONIFIED);
					f.toFront();
					f.requestFocus();
				});
				window.add(item);
			}
		}
	}

	/** Quit or Exit: the application's say, then exit. */
	void quit() {
		if( quit == null || quit.getAsBoolean() ) {
			System.exit(0);
		}
	}

	/** The application's say on quitting, asked on the event thread (it may show a dialog). */
	private boolean okToQuitOnEdt() {
		if( SwingUtilities.isEventDispatchThread() ) {
			return quit.getAsBoolean();
		}
		boolean[] ok = new boolean[1];
		try {
			SwingUtilities.invokeAndWait(()->ok[0] = quit.getAsBoolean());
		} catch (Exception ex) {
			return false;
		}
		return ok[0];
	}

	/** On macOS, About, Settings and Quit in the application menu (once for the application). */
	private void appMenuHandlers() {
		if( !Desktop.isDesktopSupported() ) {
			return;
		}
		Desktop d = Desktop.getDesktop();
		if( about != null && d.isSupported(Desktop.Action.APP_ABOUT) ) {
			d.setAboutHandler(e->SwingUtilities.invokeLater(about));
		}
		if( settings != null && d.isSupported(Desktop.Action.APP_PREFERENCES) ) {
			d.setPreferencesHandler(e->SwingUtilities.invokeLater(settings));
		}
		if( quit != null && d.isSupported(Desktop.Action.APP_QUIT_HANDLER) ) {
			d.setQuitHandler((e, response)->{
				if( okToQuitOnEdt() ) {
					response.performQuit();
				} else {
					response.cancelQuit();
				}
			});
		}
	}

	/** The shift key, for items such as Save As... (shift and S). */
	public static final int SHIFT = InputEvent.SHIFT_DOWN_MASK;

	/** The alt (option) key. */
	public static final int ALT = InputEvent.ALT_DOWN_MASK;
}
