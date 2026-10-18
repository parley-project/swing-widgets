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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Folders in the theme's colors, taken as they're painted. */
public class TestFolderIcon {

	private final Theme before = Theme.current();

	@AfterEach
	public void putBack() {
		Theme.setCurrent(before);
	}

	private static BufferedImage paint(FolderIcon icon) {
		BufferedImage b = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = b.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		return b;
	}

	@Test
	public void theThemesLightColorWithItsAccentAround() {
		Color[] gold = FolderIcon.colors(Theme.GOLD);
		assertEquals(Theme.GOLD.getStart(), gold[0], "the fill");
		assertEquals(Theme.GOLD.getAccent(), gold[2], "the outline");
		Color[] dark = FolderIcon.colors(Theme.DARK);
		assertNotEquals(Theme.DARK.getStart(), dark[0], "a dark theme's folder isn't its (near black) light end");
		assertEquals(Theme.DARK.getAccent(), dark[1]);
	}

	@Test
	public void paintsInTheThemeInEffect() {
		FolderIcon icon = new FolderIcon(32, FolderIcon.Kind.CLOSED);
		assertEquals(32, icon.getIconWidth());
		Theme.setCurrent(Theme.GOLD);
		int inGold = paint(icon).getRGB(16, 22);
		Theme.setCurrent(Theme.named("Ocean"));
		int inOcean = paint(icon).getRGB(16, 22);
		assertNotEquals(inGold, inOcean, "the same icon, painted again, follows the theme");
		assertEquals(Theme.named("Ocean").getStart().getRGB(), inOcean);
	}

	@Test
	public void theOtherKinds() {
		Theme.setCurrent(Theme.GOLD);
		BufferedImage notReadable = paint(new FolderIcon(32, FolderIcon.Kind.NOT_READABLE));
		Color c = new Color(notReadable.getRGB(16, 19), true);
		assertEquals(0xD0, c.getRed(), "the red bar");
		BufferedImage open = paint(new FolderIcon(32, FolderIcon.Kind.OPEN));
		assertNotEquals(paint(new FolderIcon(32, FolderIcon.Kind.CLOSED)).getRGB(6, 11), open.getRGB(6, 11));
		assertEquals(FolderIcon.Kind.OPEN, new FolderIcon(20, FolderIcon.Kind.OPEN).getKind());
	}
}
