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
		assertEquals(start, UIManager.getColor("Tree.selectionForeground"));
		assertEquals(end, UIManager.getColor("Table.gridColor"));
		assertEquals(0, UIManager.getColor("Table.background").getAlpha());
		assertEquals(end, UIManager.getColor("TableHeader.background"));
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
