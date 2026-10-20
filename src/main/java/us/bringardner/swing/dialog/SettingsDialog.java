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
package us.bringardner.swing.dialog;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import us.bringardner.swing.gradient.Appearance;
import us.bringardner.swing.gradient.GradientLook;
import us.bringardner.swing.gradient.Theme;

/**
 * The appearance settings: the font, and a theme chosen from the built-in ones and the user's own.
 * "New theme..." makes a new theme from any of them ({@link ThemeDesigner}); the user's own
 * themes can be deleted. A preview shows the choice before it is used. OK puts it into effect in
 * every open window and, given a preferences node, saves it; Cancel changes nothing.
 * <p>
 * An application loads the saved settings at start-up with {@code GradientLook.install(prefs)}
 * and opens this dialog with {@link #showDialog(Window, Preferences)}.
 * <p>
 * An application can add its own tabs ({@link SettingsPages}): then the dialog is "Settings", with
 * the appearance on the first tab, and OK keeps every tab's changes.
 */
public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	static final String SAMPLE_TEXT = "The quick brown fox jumps over the lazy dog";

	private final Preferences prefs;
	private final Appearance original;
	private Appearance chosen;
	private boolean accepted;

	private final JLabel fontSample = new JLabel("<html><div style='width:280px'>" + SAMPLE_TEXT + "</div></html>");
	private final JLabel fontName = new JLabel();
	private final JButton standardFontButton = new JButton("Use the standard font");
	private final List<ThemeCard> cards = new ArrayList<>();
	private final JPanel grid = new JPanel(new GridLayout(0, 5, 10, 10));
	private final JLabel themeName = new JLabel();
	private final JButton editButton = new JButton("Edit theme...");
	private final JButton deleteButton = new JButton("Delete theme");
	private final ThemePreview preview = new ThemePreview();
	/** The applications' own tabs (none: no tabs). */
	private final List<SettingsPage> pages = SettingsPages.create();
	private JTabbedPane tabs;
	private JButton resetButton;

	/** Opens the dialog, without saving the choice anywhere: OK only puts it into effect. */
	public SettingsDialog() {
		this(null, null);
	}

	/**
	 * @param prefs where OK saves the choice (the node the application loads from at start-up), or
	 *        null to only put it into effect
	 */
	public SettingsDialog(Preferences prefs) {
		this(null, prefs);
	}

	/**
	 * @param owner the window it belongs to, or null
	 * @param prefs where OK saves the choice, or null to only put it into effect
	 */
	public SettingsDialog(Window owner, Preferences prefs) {
		super(owner, "Appearance", ModalityType.APPLICATION_MODAL);
		this.prefs = prefs;
		this.original = Appearance.current();
		this.chosen = original;
		// closing it any way but OK cancels: the pages take back what they changed
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				cancel();
			}
		});
		build();
		show(chosen);
		pack();
		// the applications' tabs may want more room (a list, say)
		setResizable(!pages.isEmpty());
		setLocationRelativeTo(owner);
	}

	/**
	 * Shows the settings and waits.
	 *
	 * @param owner the window it belongs to, or null
	 * @param prefs where OK saves the choice (the node the application loads from at start-up), or
	 *        null to only put it into effect
	 * @return true if the user pressed OK and changed something
	 */
	public static boolean showDialog(Window owner, Preferences prefs) {
		SettingsDialog d = new SettingsDialog(owner, prefs);
		d.setVisible(true);
		return d.isAccepted() && !d.getAppearance().equals(d.original);
	}

	/** True once OK was pressed. */
	public boolean isAccepted() {
		return accepted;
	}

	/** What is chosen in the dialog now (in effect only after OK). */
	public Appearance getAppearance() {
		return chosen;
	}

	// ---------------------------------------------------------------- actions

	/** Chooses a theme. */
	public void selectTheme(Theme theme) {
		chosen = chosen.withTheme(theme);
		show(chosen);
	}

	/** Chooses a theme made from one color (as the theme designer can start from). */
	public void selectColor(Color color) {
		selectTheme(Theme.fromColor(color));
	}

	/** Chooses a font, or null for the look and feel's standard font. */
	public void selectFont(Font font) {
		chosen = chosen.withFont(font);
		show(chosen);
	}

	/** Back to the gold theme and the standard font (in effect only after OK). */
	public void reset() {
		chosen = Appearance.defaults();
		show(chosen);
	}

	/**
	 * OK: keeps the applications' tabs' changes, puts the appearance into effect, saves it if there
	 * is a preferences node, and closes. A tab that can't keep its changes is shown, with why, and
	 * the dialog stays open.
	 */
	public void ok() {
		for(SettingsPage p : pages) {
			String problem = p.problem();
			if( problem != null ) {
				showPage(p);
				tell(p.getTitle(), problem, JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		for(SettingsPage p : pages) {
			try {
				p.ok();
			} catch (Exception e) {
				showPage(p);
				tell(p.getTitle(), "Can't keep the changes: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		accepted = true;
		dispose();
		if( !chosen.equals(original) ) {
			chosen.apply();
			if( prefs != null ) {
				chosen.save(prefs);
			}
		}
	}

	/** Cancel: closes without changing anything. */
	public void cancel() {
		for(SettingsPage p : pages) {
			p.cancel();
		}
		accepted = false;
		dispose();
	}

	protected void actionChangeFont() {
		FontDialog d = new FontDialog();
		Font current = chosen.getFont() != null ? chosen.getFont() : new JLabel().getFont();
		Font f = d.showDialog(current);
		if( f != null ) {
			selectFont(f);
		}
	}

	/** New theme...: the designer, starting from the theme chosen here; a saved one is chosen. */
	protected void actionDesign() {
		Theme t = ThemeDesigner.design(this, chosen.getTheme());
		if( t != null ) {
			designed(t);
		}
	}

	/** Edit theme...: the designer, changing the chosen theme (one of the user's own) in place. */
	protected void actionEdit() {
		Theme t = chosen.getTheme();
		if( !t.isUserTheme() ) {
			return;
		}
		Theme changed = ThemeDesigner.edit(this, t);
		if( changed != null ) {
			designed(changed);
		}
	}

	/** A theme the designer saved: it gets a card and is chosen. */
	public void designed(Theme t) {
		fillGrid();
		selectTheme(t);
		pack();
	}

	/** Delete theme: deletes the chosen theme, one of the user's own, once they say so. */
	protected void actionDelete() {
		Theme t = chosen.getTheme();
		if( !t.isUserTheme() ) {
			return;
		}
		int answer = JOptionPane.showConfirmDialog(this, "Delete your theme \"" + t.getName() + "\"?",
				"Delete Theme", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if( answer == JOptionPane.OK_OPTION ) {
			deleteTheme(t);
		}
	}

	/** Deletes one of the user's themes; the gold theme is chosen in its place. */
	public void deleteTheme(Theme t) {
		if( t.isUserTheme() && Theme.deleteUserTheme(t.getName()) ) {
			fillGrid();
			selectTheme(Theme.GOLD);
			pack();
		}
	}

	/** The applications' tabs in this dialog. */
	public List<SettingsPage> getPages() {
		return pages;
	}

	/** Shows a message (a JOptionPane message type); tests replace it, as there's no one to close it. */
	protected void tell(String title, String message, int type) {
		JOptionPane.showMessageDialog(this, message, title, type);
	}

	/** Shows a tab: one of {@link #getPages()}. */
	public void showPage(SettingsPage p) {
		if( tabs != null && pages.contains(p) ) {
			tabs.setSelectedComponent(p.getComponent());
		}
	}

	// ---------------------------------------------------------------- the dialog's parts

	private void build() {
		JPanel content = new JPanel(new BorderLayout(0, 12));
		content.setBorder(new EmptyBorder(16, 18, 12, 18));
		setContentPane(content);

		JPanel sections = new JPanel();
		sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
		if( pages.isEmpty() ) {
			content.add(sections, BorderLayout.CENTER);
		} else {
			// Appearance, then the applications' tabs; the gradient shows through, as in the designer
			setTitle("Settings");
			Object contentOpaque = UIManager.get("TabbedPane.contentOpaque");
			UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
			tabs = new JTabbedPane();
			UIManager.put("TabbedPane.contentOpaque", contentOpaque);
			tabs.setOpaque(false);
			sections.setOpaque(false);
			sections.setBorder(new EmptyBorder(10, 10, 10, 10));
			tabs.addTab("Appearance", sections);
			for(SettingsPage p : pages) {
				tabs.addTab(p.getTitle(), p.getComponent());
			}
			content.add(tabs, BorderLayout.CENTER);
		}

		sections.add(heading("Theme"));
		sections.add(themeGrid());
		JPanel themeButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		themeButtons.setAlignmentX(LEFT_ALIGNMENT);
		themeButtons.setBorder(new EmptyBorder(8, 0, 0, 0));
		JButton designButton = new JButton("New theme...");
		designButton.setToolTipText("Make a new theme, starting from the one chosen here");
		designButton.addActionListener(e->actionDesign());
		deleteButton.setToolTipText("Delete the chosen theme (only your own themes can be deleted)");
		deleteButton.addActionListener(e->actionDelete());
		editButton.setToolTipText("Change the chosen theme (only your own themes can be changed)");
		editButton.addActionListener(e->actionEdit());
		themeButtons.add(designButton);
		themeButtons.add(Box.createHorizontalStrut(8));
		themeButtons.add(editButton);
		themeButtons.add(Box.createHorizontalStrut(8));
		themeButtons.add(deleteButton);
		sections.add(themeButtons);
		themeName.setAlignmentX(LEFT_ALIGNMENT);
		themeName.setBorder(new EmptyBorder(4, 0, 0, 0));
		sections.add(themeName);
		sections.add(Box.createVerticalStrut(14));
		sections.add(heading("Font"));
		sections.add(fontSection());
		sections.add(Box.createVerticalStrut(14));
		sections.add(heading("Preview"));
		sections.add(previewSection());

		content.add(buttons(), BorderLayout.SOUTH);

		getRootPane().registerKeyboardAction(e->cancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		GradientLook.apply(this);
	}

	private JComponent themeGrid() {
		fillGrid();
		JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		wrap.setAlignmentX(LEFT_ALIGNMENT);
		wrap.add(grid);
		return wrap;
	}

	/** A card for each theme: the built-in ones, the user's, and a custom one if it is in effect. */
	private void fillGrid() {
		grid.removeAll();
		cards.clear();
		List<Theme> themes = Theme.all();
		if( chosen.getTheme().isCustom() || original.getTheme().isCustom() ) {
			themes.add(chosen.getTheme().isCustom() ? chosen.getTheme() : original.getTheme());
		}
		for(Theme t : themes) {
			ThemeCard card = new ThemeCard(t.getName(), t, ()->selectTheme(t));
			cards.add(card);
			grid.add(card);
		}
		grid.revalidate();
		grid.repaint();
	}

	private JComponent fontSection() {
		JPanel p = new JPanel(new BorderLayout(10, 4));
		p.setAlignmentX(LEFT_ALIGNMENT);
		fontSample.setOpaque(true);
		fontSample.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(0, 0, 0, 60)), new EmptyBorder(8, 10, 8, 10)));
		p.add(fontSample, BorderLayout.CENTER);
		fontName.setFont(fontName.getFont().deriveFont(Font.ITALIC));
		p.add(fontName, BorderLayout.SOUTH);

		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		JButton change = new JButton("Change...");
		change.addActionListener(e->actionChangeFont());
		standardFontButton.addActionListener(e->selectFont(null));
		change.setAlignmentX(LEFT_ALIGNMENT);
		standardFontButton.setAlignmentX(LEFT_ALIGNMENT);
		buttons.add(change);
		buttons.add(Box.createVerticalStrut(4));
		buttons.add(standardFontButton);
		p.add(buttons, BorderLayout.EAST);
		return p;
	}

	private JComponent previewSection() {
		preview.setAlignmentX(LEFT_ALIGNMENT);
		return preview;
	}

	private JComponent buttons() {
		JPanel p = new JPanel(new BorderLayout());
		resetButton = new JButton("Reset to defaults");
		resetButton.addActionListener(e->reset());
		if( tabs != null ) {
			// the appearance's defaults: shown with the Appearance tab only
			tabs.addChangeListener(e->resetButton.setVisible(tabs.getSelectedIndex() == 0));
		}
		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		left.add(resetButton);
		p.add(left, BorderLayout.WEST);
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e->cancel());
		JButton okButton = new JButton("OK");
		okButton.addActionListener(e->ok());
		right.add(cancelButton);
		right.add(okButton);
		p.add(right, BorderLayout.EAST);
		getRootPane().setDefaultButton(okButton);
		return p;
	}

	private static JLabel heading(String text) {
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize2D() + 1f));
		l.setBorder(new EmptyBorder(0, 0, 6, 0));
		l.setAlignmentX(LEFT_ALIGNMENT);
		return l;
	}

	/** Shows a choice in every part of the dialog. */
	private void show(Appearance a) {
		Theme t = a.getTheme();
		Font f = a.getFont() != null ? a.getFont() : new JLabel().getFont();

		// the sample is in the size chosen, small or large, so a change of size shows
		fontSample.setFont(f);
		fontSample.setBackground(t.getField());
		fontSample.setForeground(t.getFieldText());
		fontName.setText(a.getFont() == null
				? "The standard font"
				: f.getFamily() + ", " + styleName(f) + ", " + f.getSize() + " pt");
		standardFontButton.setEnabled(a.getFont() != null);

		for(ThemeCard c : cards) {
			c.setSelected(c.theme.equals(t));
		}
		String about = t.getDescription() != null ? t.getDescription()
				: t.isCustom() ? "Made from one color"
				: t.isUserTheme() ? "One of your themes" : null;
		themeName.setText(t.getName() + (about == null ? "" : ": " + about));
		editButton.setEnabled(t.isUserTheme());
		deleteButton.setEnabled(t.isUserTheme());

		// the preview, painted in the chosen theme (not the one in effect)
		preview.show(t, a.getFont());
		if( isDisplayable() ) {
			pack();
		}
		repaint();
	}

	private static String styleName(Font f) {
		if( f.isBold() && f.isItalic() ) {
			return "bold italic";
		}
		return f.isBold() ? "bold" : f.isItalic() ? "italic" : "plain";
	}

	/**
	 * A theme to click on: a small picture of it (its gradient, text, a field and a selection), its
	 * name underneath, a ring when it is the one chosen. Without a theme (Custom... before a color
	 * is picked) it shows a spectrum.
	 */
	static class ThemeCard extends JPanel {

		private static final long serialVersionUID = 1L;
		static final int W = 84;
		static final int H = 56;

		Theme theme;
		private boolean selected;
		private final JLabel label;

		ThemeCard(String name, Theme theme, Runnable onClick) {
			super(new BorderLayout(0, 3));
			this.theme = theme;
			setOpaque(false);
			JComponent tile = new JComponent() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics g) {
					paintTile((Graphics2D) g.create(), getWidth(), getHeight());
				}
			};
			tile.setPreferredSize(new Dimension(W, H));
			add(tile, BorderLayout.CENTER);
			label = new JLabel(name, SwingConstants.CENTER);
			add(label, BorderLayout.SOUTH);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			getAccessibleContext().setAccessibleName(name + " theme");
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					onClick.run();
				}
			});
		}

		void setSelected(boolean selected) {
			this.selected = selected;
			label.setFont(label.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN));
			repaint();
		}

		boolean isSelected() {
			return selected;
		}

		private void paintTile(Graphics2D g, int w, int h) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int inset = 3;
			int tw = w - inset * 2;
			int th = h - inset * 2;
			java.awt.Shape clip = new java.awt.geom.RoundRectangle2D.Float(inset, inset, tw, th, 10, 10);
			if( theme != null ) {
				g.setPaint(new GradientPaint(inset, inset, theme.getStart(), inset + tw, inset + th, theme.getEnd()));
				g.fill(clip);
				java.awt.Shape old = g.getClip();
				g.clip(clip);
				// a line of text on the gradient
				g.setColor(theme.getText());
				g.setFont(getFont().deriveFont(Font.BOLD, 13f));
				g.drawString("Aa", inset + 7, inset + 18);
				// a field, and a selected row
				g.setColor(theme.getField());
				g.fillRect(inset + 36, inset + 7, tw - 43, 12);
				g.setColor(theme.getFieldText());
				g.fillRect(inset + 39, inset + 12, 18, 2);
				g.setColor(theme.getSelection());
				g.fillRect(inset, inset + th - 16, tw, 10);
				g.setColor(theme.getSelectionText());
				g.fillRect(inset + 7, inset + th - 12, 24, 2);
				g.setClip(old);
			} else {
				float[] hues = {0f, 0.17f, 0.33f, 0.5f, 0.67f, 0.83f};
				Color[] colors = new Color[hues.length];
				for(int i = 0; i < hues.length; i++) {
					colors[i] = Color.getHSBColor(hues[i], 0.45f, 0.95f);
				}
				g.setPaint(new java.awt.LinearGradientPaint(inset, inset, inset + tw, inset + th,
						new float[] {0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f}, colors));
				g.fill(clip);
			}
			if( selected ) {
				g.setStroke(new BasicStroke(3f));
				g.setColor(new Color(0, 0, 0, 170));
				g.drawRoundRect(inset - 1, inset - 1, tw + 1, th + 1, 12, 12);
				g.setStroke(new BasicStroke(1f));
				g.setColor(new Color(255, 255, 255, 200));
				g.drawRoundRect(inset + 1, inset + 1, tw - 3, th - 3, 9, 9);
			} else {
				g.setStroke(new BasicStroke(1f));
				g.setColor(new Color(0, 0, 0, 70));
				g.drawRoundRect(inset, inset, tw - 1, th - 1, 10, 10);
			}
			g.dispose();
		}
	}

	/**
	 * Launch the dialog on its own, with the gradient look.
	 */
	public static void main(String[] args) {
		GradientLook.install();
		java.awt.EventQueue.invokeLater(()->{
			showDialog(null, null);
			System.exit(0);
		});
	}
}
