package us.bringardner.swing.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.MediaTracker;

import javax.swing.ImageIcon;
import javax.swing.JTable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import us.bringardner.swing.datetime.test.RunOnEdt;
import us.bringardner.swing.dialog.MessageDialog;
import us.bringardner.swing.field.PasswordPanel;
import us.bringardner.swing.field.TextFieldPanel;
import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientColors;
import us.bringardner.swing.ui.GlassPane;
import us.bringardner.swing.ui.ScrollBarUI;
import us.bringardner.swing.ui.TableHeaderUI;

/**
 * Tests for the widgets that came from BjlFileSystemViewer that do not need a display
 * (they also run with java.awt.headless=true).
 */
@ExtendWith(RunOnEdt.class)
public class TestWidgets {

	private static final String[] ICONS = {
			"Warning100x100.png", "Error100x100.png", "Globe100x100.png",
			"OpenEyeBw_40_17.png", "ClosedEyeBw_40_17.png" };

	@Test
	public void theIconsAreInTheJar() {
		for(String name : ICONS) {
			assertNotNull(TestWidgets.class.getResource("/us/bringardner/swing/icons/"+name), name);
		}
	}

	@Test
	public void messageDialogIconsLoad() {
		for(ImageIcon icon : new ImageIcon[] {MessageDialog.warningIcon, MessageDialog.ErrorIcon, MessageDialog.MessageIcon}) {
			assertEquals(MediaTracker.COMPLETE, icon.getImageLoadStatus());
			assertTrue(icon.getIconWidth() > 0);
		}
	}

	@Test
	public void gradientColorsDefaultToGoldAndCanBeChanged() {
		assertEquals(new Color(242, 206, 113), GradientColors.getStart());
		assertEquals(new Color(150, 123, 40), GradientColors.getEnd());
		assertEquals(0, GradientColors.TRANSPARENT.getAlpha());

		try {
			GradientColors.setStart(Color.BLUE);
			GradientColors.setEnd(Color.BLACK);
			GradientButton button = new GradientButton("OK");
			assertEquals(Color.BLUE, button.getBackground());
			assertEquals(Color.BLACK, button.getForeground());
			assertThrows(NullPointerException.class, () -> GradientColors.setStart(null));
		} finally {
			GradientColors.setStart(GradientColors.DEFAULT_START);
			GradientColors.setEnd(GradientColors.DEFAULT_END);
		}
		assertSame(GradientColors.DEFAULT_START, new GradientButton("OK").getBackground());
	}

	@Test
	public void fieldsKeepTheirValues() {
		assertEquals("secret", new PasswordPanel("secret").getPassword());
		assertEquals("value", new TextFieldPanel("Name", "value").getText());
	}

	@Test
	public void uiDelegatesAndGlassPaneCanBeCreated() {
		JTable table = new JTable(1, 1);
		assertTrue(TableHeaderUI.createUI(table.getTableHeader()) instanceof TableHeaderUI);
		assertTrue(ScrollBarUI.createUI(null) instanceof ScrollBarUI);
		assertNotNull(new GlassPane());
	}
}
