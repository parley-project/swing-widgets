package us.bringardner.swing.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import us.bringardner.swing.datetime.test.RunOnEdt;
import us.bringardner.swing.dialog.SettingsDialog;
import us.bringardner.swing.dialog.SettingsPage;
import us.bringardner.swing.dialog.SettingsPages;
import us.bringardner.swing.gradient.GradientLook;

/** An application's own tabs in the Settings dialog. */
@ExtendWith(RunOnEdt.class)
public class TestSettingsPages {

	static class Page implements SettingsPage {
		final JLabel label = new JLabel("mine");
		String problem;
		Exception failure;
		int oks;
		int cancels;

		@Override
		public String getTitle() {
			return "Mine";
		}

		@Override
		public JComponent getComponent() {
			return label;
		}

		@Override
		public String problem() {
			return problem;
		}

		@Override
		public void ok() throws Exception {
			if( failure != null ) {
				throw failure;
			}
			oks++;
		}

		@Override
		public void cancel() {
			cancels++;
		}
	}

	/** The dialog, with what it would have told the user. */
	static class Dialog extends SettingsDialog {
		private static final long serialVersionUID = 1L;
		final List<String> told = new ArrayList<>();

		Dialog() {
			super(null, null);
		}

		@Override
		protected void tell(String title, String message, int type) {
			told.add(title + ": " + message);
		}
	}

	@AfterEach
	public void unregister() {
		SettingsPages.unregister("test.mine");
	}

	@Test
	public void withoutPagesItIsTheAppearanceAlone() {
		GradientLook.install();
		SettingsDialog d = new SettingsDialog();
		assertEquals("Appearance", d.getTitle());
		assertTrue(d.getPages().isEmpty());
		assertNull(find(d.getContentPane(), JTabbedPane.class));
		d.cancel();
	}

	@Test
	public void aPageIsATabAfterAppearance() {
		GradientLook.install();
		SettingsPages.register("test.mine", Page::new);
		Dialog d = new Dialog();
		assertEquals("Settings", d.getTitle());
		JTabbedPane tabs = find(d.getContentPane(), JTabbedPane.class);
		assertNotNull(tabs);
		assertEquals(2, tabs.getTabCount());
		assertEquals("Appearance", tabs.getTitleAt(0));
		assertEquals("Mine", tabs.getTitleAt(1));
		Page p = (Page) d.getPages().get(0);
		d.showPage(p);
		assertEquals(1, tabs.getSelectedIndex());

		// a problem keeps it open, on the page
		tabs.setSelectedIndex(0);
		p.problem = "not yet";
		d.ok();
		assertFalse(d.isAccepted());
		assertEquals(1, tabs.getSelectedIndex());
		assertEquals(List.of("Mine: not yet"), d.told);
		assertEquals(0, p.oks);

		// so does a failure to keep the changes
		p.problem = null;
		p.failure = new java.io.IOException("disk full");
		d.ok();
		assertFalse(d.isAccepted());
		assertTrue(d.told.get(1).contains("disk full"));

		p.failure = null;
		d.ok();
		assertTrue(d.isAccepted());
		assertEquals(1, p.oks);
	}

	@Test
	public void cancelTellsThePages() {
		GradientLook.install();
		SettingsPages.register("test.mine", Page::new);
		Dialog d = new Dialog();
		d.cancel();
		assertEquals(1, ((Page) d.getPages().get(0)).cancels);
		assertFalse(d.isAccepted());
	}

	@Test
	public void eachDialogMakesItsOwnPage() {
		SettingsPages.register("test.mine", Page::new);
		assertFalse(SettingsPages.create().get(0) == SettingsPages.create().get(0));
		SettingsPages.register("test.mine", Page::new);
		assertEquals(1, SettingsPages.create().size(), "the same id replaces");
	}

	@SuppressWarnings("unchecked")
	static <T> T find(Container c, Class<T> type) {
		for(Component x : c.getComponents()) {
			if( type.isInstance(x) ) {
				return (T) x;
			}
			if( x instanceof Container ) {
				T r = find((Container) x, type);
				if( r != null ) {
					return r;
				}
			}
		}
		return null;
	}
}
