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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import us.bringardner.swing.datetime.test.RunOnEdt;

/** The standard menus: their order, and About, Settings and Exit by platform. */
@ExtendWith(RunOnEdt.class)
public class TestStandardMenus {

	@BeforeEach
	public void display() {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
	}

	private static List<String> titles(JMenuBar bar) {
		List<String> ret = new ArrayList<>();
		for(int i = 0; i < bar.getMenuCount(); i++) {
			ret.add(bar.getMenu(i).getText());
		}
		return ret;
	}

	private static List<String> items(JMenu menu) {
		List<String> ret = new ArrayList<>();
		for(int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			ret.add(item == null ? "-" : item.getText());
		}
		return ret;
	}

	private StandardMenus menus(AtomicInteger about, AtomicInteger settings) {
		StandardMenus m = new StandardMenus("Test App").about(about::incrementAndGet)
				.settings(settings::incrementAndGet).quit(()->false);
		m.file.add(StandardMenus.item("Open...", KeyEvent.VK_O, ()->{}));
		m.view.add(StandardMenus.item("Refresh", KeyEvent.VK_R, ()->{}));
		return m;
	}

	@Test
	public void elsewhereSettingsAndExitEndFileAndAboutIsInHelp() {
		AtomicInteger about = new AtomicInteger();
		AtomicInteger settings = new AtomicInteger();
		JFrame f = new JFrame("One");
		try {
			JMenuBar bar = menus(about, settings).build(f, false, new JMenu("Run"));
			assertEquals(List.of("File", "View", "Run", "Window", "Help"), titles(bar), "Edit is empty, so left out");
			JMenu file = bar.getMenu(0);
			assertEquals(List.of("Open...", "-", "Settings...", "-", "Exit"), items(file));
			assertEquals(StandardMenus.keys(KeyEvent.VK_O, 0), file.getItem(0).getAccelerator());
			assertEquals(StandardMenus.keys(KeyEvent.VK_COMMA, 0), file.getItem(2).getAccelerator());
			file.getItem(2).doClick();
			assertEquals(1, settings.get());
			assertEquals(List.of("About Test App"), items(bar.getMenu(4)));
			bar.getMenu(4).getItem(0).doClick();
			assertEquals(1, about.get());
		} finally {
			f.dispose();
		}
	}

	@Test
	public void onMacTheyAreInTheApplicationMenu() {
		JFrame f = new JFrame("One");
		try {
			JMenuBar bar = menus(new AtomicInteger(), new AtomicInteger()).build(f, true);
			assertEquals(List.of("File", "View", "Window"), titles(bar), "Help has nothing else: left out");
			assertEquals(List.of("Open..."), items(bar.getMenu(0)));
		} finally {
			f.dispose();
		}
	}

	@Test
	public void theWindowMenuListsTheOpenWindows() {
		JFrame one = new JFrame("One");
		JFrame two = new JFrame("Two");
		try {
			StandardMenus m1 = new StandardMenus("Test App");
			m1.file.add(new JMenuItem("x"));
			m1.install(one);
			new StandardMenus("Test App").install(two);
			one.pack();
			two.pack();
			one.setVisible(true);
			two.setVisible(true);
			m1.listWindows(one);
			List<String> w = items(m1.window);
			assertEquals("Minimize", w.get(0));
			assertEquals(StandardMenus.keys(KeyEvent.VK_M, 0), m1.window.getItem(0).getAccelerator());
			assertTrue(w.contains("One") && w.contains("Two"), w.toString());
			for(int i = 0; i < m1.window.getItemCount(); i++) {
				JMenuItem item = m1.window.getItem(i);
				if( item instanceof JCheckBoxMenuItem ) {
					assertEquals("One".equals(item.getText()), item.isSelected(), "this window checked");
				}
			}
			two.setVisible(false);
			m1.listWindows(one);
			assertFalse(items(m1.window).contains("Two"));
		} finally {
			one.dispose();
			two.dispose();
		}
	}

	@Test
	public void itemsAndKeys() {
		assertNull(StandardMenus.item("Plain", null, ()->{}).getAccelerator());
		assertEquals(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_S, StandardMenus.shortcut() | StandardMenus.SHIFT),
				StandardMenus.keys(KeyEvent.VK_S, StandardMenus.SHIFT));
		assertEquals(System.getProperty("os.name").toLowerCase().startsWith("mac"), StandardMenus.isMac());
		StandardMenus.useScreenMenuBar("Test App");
	}
}
