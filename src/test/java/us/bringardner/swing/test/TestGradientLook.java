package us.bringardner.swing.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MediaTracker;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import us.bringardner.swing.datetime.test.RunOnEdt;
import us.bringardner.swing.gradient.GradientColors;
import us.bringardner.swing.gradient.GradientLook;
import us.bringardner.swing.gradient.GradientPanel;
import us.bringardner.swing.gradient.Theme;
import us.bringardner.swing.ui.GradientButtonUI;
import us.bringardner.swing.ui.ScrollBarUI;

@ExtendWith(RunOnEdt.class)
public class TestGradientLook {

	@Test
	public void installSetsTheViewersDefaults() {
		GradientLook.install();
		assertTrue(GradientLook.isInstalled());

		Color start = GradientColors.getStart();
		Color end = GradientColors.getEnd();
		assertEquals(start, UIManager.getColor("Panel.background"));
		assertEquals(end, UIManager.getColor("Panel.foreground"));
		assertEquals(ScrollBarUI.class.getCanonicalName(), UIManager.get("ScrollBarUI"));
		assertEquals(start, UIManager.getColor("ScrollBar.track"));
		assertEquals(end, UIManager.getColor("ScrollBar.thumb"));
		assertEquals(end, UIManager.getColor("Tree.selectionBackground"));
		// selected text is white on the dark color: light gold on dark gold was too faint
		assertEquals(Color.white, UIManager.getColor("Tree.selectionForeground"));
		assertEquals(Color.white, UIManager.getColor("TableHeader.foreground"));
		assertEquals(end, UIManager.getColor("Table.gridColor"));
		assertEquals(0, UIManager.getColor("Table.background").getAlpha());
		assertEquals(end, UIManager.getColor("TableHeader.background"));
	}

	@Test
	public void buttonsWithTextAreInTheTheme() {
		Theme old = Theme.current();
		try {
			Theme.setCurrent(Theme.named("Ocean"));
			GradientLook.install();
			assertEquals(GradientButtonUI.class.getName(), UIManager.get("ButtonUI"));

			javax.swing.JButton ok = new javax.swing.JButton("OK");
			assertTrue(ok.getUI() instanceof GradientButtonUI);
			assertFalse(ok.isOpaque());
			// an icon-only button (a toolbar's) and one asked not to be keep the look and feel's
			javax.swing.JButton tool = new javax.swing.JButton(new ImageIcon(new java.awt.image.BufferedImage(16, 16,
					java.awt.image.BufferedImage.TYPE_INT_ARGB)));
			assertFalse(tool.getUI() instanceof GradientButtonUI);
			javax.swing.JButton plain = new javax.swing.JButton("Plain");
			plain.putClientProperty(GradientButtonUI.THEMED, Boolean.FALSE);
			plain.updateUI();
			assertFalse(plain.getUI() instanceof GradientButtonUI);
			// made with no text and given an icon afterwards: a toolbar button after all
			javax.swing.JButton later = new javax.swing.JButton("");
			assertTrue(later.getUI() instanceof GradientButtonUI);
			later.setIcon(tool.getIcon());
			assertFalse(later.getUI() instanceof GradientButtonUI);
			later.setText("Run");
			assertTrue(later.getUI() instanceof GradientButtonUI, "text again: themed again");

			// painted in the theme's colors, the default button too (Aqua paints it the macOS accent)
			javax.swing.JDialog d = new javax.swing.JDialog();
			JPanel p = new JPanel();
			p.add(ok);
			d.setContentPane(p);
			d.getRootPane().setDefaultButton(ok);
			d.pack();
			assertTrue(ok.isDefaultButton());
			java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(ok.getWidth(), ok.getHeight(),
					java.awt.image.BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g = img.createGraphics();
			ok.paint(g);
			g.dispose();
			Color middle = new Color(img.getRGB(3, ok.getHeight() / 2), true);
			Theme t = Theme.current();
			assertTrue(near(middle, t.getEnd(), 60), "the dark end at the left: " + middle);
			Color edge = new Color(img.getRGB(ok.getWidth() / 2, 0), true);
			assertTrue(edge.getAlpha() == 0 || near(edge, t.getEnd(), 60), "outlined in the dark end: " + edge);
			d.dispose();
		} finally {
			Theme.setCurrent(old);
			GradientLook.install();
		}
	}

	private static boolean near(Color a, Color b, int by) {
		return Math.abs(a.getRed() - b.getRed()) + Math.abs(a.getGreen() - b.getGreen())
				+ Math.abs(a.getBlue() - b.getBlue()) <= by;
	}

	@Test
	public void theIconsLoad() {
		GradientLook.install();
		for(String key : new String[] {"Tree.collapsedIcon", "Tree.expandedIcon", "Tree.openIcon",
				"Tree.leafIcon", "Tree.closedIcon", "Table.ascendingSortIcon", "Table.descendingSortIcon"}) {
			Icon icon = UIManager.getIcon(key);
			assertNotNull(icon, key);
			assertTrue(icon.getIconWidth() > 0, key);
		}
		for(String name : new String[] {"V15x15.png", "tree_closed16x16.png", "tree_open16x16.png",
				"UpArrow20x20.png", "DnArrow20x20.png"}) {
			ImageIcon icon = new ImageIcon(TestGradientLook.class.getResource(GradientLook.ICONS + name));
			assertEquals(MediaTracker.COMPLETE, icon.getImageLoadStatus(), name);
		}
	}

	@Test
	public void seeThroughClearsATableAndItsScrollPane() {
		JTable table = new JTable(new Object[][] {{"a", "b"}}, new Object[] {"One", "Two"});
		JScrollPane sp = new JScrollPane(table);
		assertSame(sp, GradientLook.seeThrough(sp));
		assertFalse(sp.isOpaque());
		assertFalse(sp.getViewport().isOpaque());
		assertFalse(table.isOpaque());
		assertEquals(0, table.getBackground().getAlpha());
	}

	@Test
	public void seeThroughStylesATableHeaderWhenItArrives() {
		JTable table = new JTable(new Object[][] {{"a", "b"}}, new Object[] {"One", "Two"});
		JScrollPane sp = GradientLook.seeThrough(new JScrollPane(table));
		// what showing the table does: it puts its header in the scroll pane
		sp.setColumnHeaderView(table.getTableHeader());
		assertNotNull(sp.getColumnHeader());
		assertFalse(sp.getColumnHeader().isOpaque());
		assertEquals(GradientColors.getEnd(), sp.getColumnHeader().getBackground());
	}

	@Test
	public void seeThroughWorksForATreeWithNoHeader() {
		JTree tree = new JTree();
		JScrollPane sp = GradientLook.seeThrough(new JScrollPane(tree));
		assertFalse(tree.isOpaque());
		assertFalse(sp.getViewport().isOpaque());
	}

	@Test
	public void seeThroughPanelsClearsLayoutButNotControls() {
		JPanel outer = new JPanel(new BorderLayout());
		JPanel inner = new JPanel();
		JLabel label = new JLabel("x");
		label.setOpaque(true);
		inner.add(label);
		GradientPanel gradient = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
		gradient.setOpaque(true);
		javax.swing.JCheckBox check = new javax.swing.JCheckBox("x");
		check.setOpaque(true);
		inner.add(check);
		JTextArea text = new JTextArea();
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JScrollPane(text));
		outer.add(inner, BorderLayout.NORTH);
		outer.add(gradient, BorderLayout.SOUTH);
		outer.add(split, BorderLayout.CENTER);

		GradientLook.seeThroughPanels(outer);

		assertFalse(outer.isOpaque());
		assertFalse(inner.isOpaque());
		assertFalse(split.isOpaque());
		assertFalse(((JPanel) split.getLeftComponent()).isOpaque());
		assertTrue(label.isOpaque(), "a control is left alone");
		assertFalse(check.isOpaque(), "a check box shows the gradient");
		assertTrue(text.isOpaque(), "a text area is left alone");
		assertTrue(gradient.isOpaque(), "a gradient panel is left alone");
	}

	@Test
	public void applyPutsAWindowsContentOnTheGradient() {
		GradientLook.install();
		javax.swing.JDialog dialog = new javax.swing.JDialog();
		JPanel content = new JPanel(new BorderLayout());
		javax.swing.border.Border margin = new javax.swing.border.EmptyBorder(5, 5, 5, 5);
		content.setBorder(margin);
		JTable table = new JTable(new Object[][] {{"a"}}, new Object[] {"One"});
		JScrollPane sp = new JScrollPane(table);
		JTextArea text = new JTextArea();
		content.add(sp, BorderLayout.CENTER);
		content.add(new JScrollPane(text), BorderLayout.SOUTH);
		dialog.setContentPane(content);

		assertSame(dialog, GradientLook.apply(dialog));

		assertTrue(dialog.getContentPane() instanceof GradientPanel);
		assertSame(content, ((GradientPanel) dialog.getContentPane()).getComponent(0));
		assertSame(margin, ((GradientPanel) dialog.getContentPane()).getBorder(), "the margin moves outside");
		assertEquals(null, content.getBorder());
		assertFalse(content.isOpaque());
		assertFalse(table.isOpaque(), "a table shows the gradient");
		assertTrue(text.isOpaque(), "a text area is left alone");

		// a second call doesn't wrap it again
		GradientLook.apply(dialog);
		assertSame(content, ((GradientPanel) dialog.getContentPane()).getComponent(0));
		dialog.dispose();
	}

	@Test
	public void followAppliesTheLookOnceItIsInstalled() {
		GradientLook.install();
		javax.swing.JDialog dialog = new javax.swing.JDialog();
		assertSame(dialog, GradientLook.follow(dialog));
		assertTrue(dialog.getContentPane() instanceof GradientPanel);
		dialog.dispose();
	}
}
