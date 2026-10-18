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
import java.awt.Component;
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
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import us.bringardner.swing.gradient.Appearance;
import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientLook;
import us.bringardner.swing.gradient.GradientPanel;
import us.bringardner.swing.gradient.Theme;

/**
 * The appearance settings: the font, and a theme chosen from the built-in ones or made from any
 * one color. A preview shows the choice before it is used. OK puts it into effect in every open
 * window and, given a preferences node, saves it; Cancel changes nothing.
 * <p>
 * An application loads the saved settings at start-up with {@code GradientLook.install(prefs)}
 * and opens this dialog with {@link #showDialog(Window, Preferences)}.
 */
public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	static final String SAMPLE_TEXT = "The quick brown fox jumps over the lazy dog";

	private final Preferences prefs;
	private final Appearance original;
	private Appearance chosen;
	private Color customColor;
	private boolean accepted;

	private final JLabel fontSample = new JLabel("<html><div style='width:280px'>" + SAMPLE_TEXT + "</div></html>");
	private final JLabel fontName = new JLabel();
	private final JButton standardFontButton = new JButton("Use the standard font");
	private final List<ThemeCard> cards = new ArrayList<>();
	private ThemeCard customCard;
	private final JLabel themeName = new JLabel();

	private final GradientPanel preview = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
	private final JLabel previewLabel = new JLabel("Folder:");
	private final JTextField previewField = new JTextField("Documents/Reports", 16);
	private final GradientButton previewButton = new GradientButton("Open");
	private final JTable previewTable = new JTable(new DefaultTableModel(new Object[][] {
		{"Reports", "Folder", ""}, {"budget.xlsx", "File", "48 KB"}, {"notes.txt", "File", "2 KB"}},
			new Object[] {"Name", "Type", "Size"}) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	});
	private JScrollPane previewScroll;

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
		if( original.getTheme().isCustom() ) {
			customColor = original.getTheme().getBaseColor();
		}
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		build();
		show(chosen);
		pack();
		setResizable(false);
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
		if( theme.isCustom() ) {
			customColor = theme.getBaseColor();
		}
		show(chosen);
	}

	/** Chooses a theme made from one color (what Custom... does once a color is picked). */
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
		customColor = null;
		show(chosen);
	}

	/** OK: puts the choice into effect, saves it if there is a preferences node, and closes. */
	public void ok() {
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

	protected void actionCustomColor() {
		Color start = customColor != null ? customColor : chosen.getEnd();
		Color c = JColorChooser.showDialog(this, "Choose a color for your theme", start);
		if( c != null ) {
			selectColor(c);
		}
	}

	// ---------------------------------------------------------------- the dialog's parts

	private void build() {
		JPanel content = new JPanel(new BorderLayout(0, 12));
		content.setBorder(new EmptyBorder(16, 18, 12, 18));
		setContentPane(content);

		JPanel sections = new JPanel();
		sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
		content.add(sections, BorderLayout.CENTER);

		sections.add(heading("Theme"));
		sections.add(themeGrid());
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
		JPanel grid = new JPanel(new GridLayout(0, 5, 10, 10));
		grid.setAlignmentX(LEFT_ALIGNMENT);
		for(Theme t : Theme.THEMES) {
			ThemeCard card = new ThemeCard(t.getName(), t, ()->selectTheme(t));
			cards.add(card);
			grid.add(card);
		}
		customCard = new ThemeCard("Custom...", null, this::actionCustomColor);
		customCard.setToolTipText("Pick any color: the theme is made from light and dark shades of it");
		grid.add(customCard);
		JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		wrap.setAlignmentX(LEFT_ALIGNMENT);
		wrap.add(grid);
		return wrap;
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
		preview.setLayout(new BorderLayout(0, 8));
		preview.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(0, 0, 0, 60)), new EmptyBorder(10, 10, 10, 10)));
		preview.setAlignmentX(LEFT_ALIGNMENT);

		JPanel north = new JPanel(new BorderLayout(8, 0));
		north.setOpaque(false);
		north.add(previewLabel, BorderLayout.WEST);
		previewField.setEditable(false);
		previewField.setFocusable(false);
		north.add(previewField, BorderLayout.CENTER);
		preview.add(north, BorderLayout.NORTH);

		previewTable.setFocusable(false);
		previewTable.setRowSelectionAllowed(true);
		previewTable.setShowGrid(true);
		previewTable.setRowSelectionInterval(1, 1);
		JScrollPane sp = new JScrollPane(previewTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		previewScroll = sp;
		GradientLook.seeThrough(sp);
		sp.setColumnHeaderView(previewTable.getTableHeader());
		sp.getColumnHeader().setOpaque(false);
		preview.add(sp, BorderLayout.CENTER);

		JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		south.setOpaque(false);
		previewButton.setPreferredSize(new Dimension(90, 30));
		south.add(previewButton);
		preview.add(south, BorderLayout.SOUTH);
		return preview;
	}

	private JComponent buttons() {
		JPanel p = new JPanel(new BorderLayout());
		JButton resetButton = new JButton("Reset to defaults");
		resetButton.addActionListener(e->reset());
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

		fontSample.setFont(f.deriveFont(Math.max(f.getSize2D(), 13f)));
		fontSample.setBackground(t.getField());
		fontSample.setForeground(t.getFieldText());
		fontName.setText(a.getFont() == null
				? "The standard font"
				: f.getFamily() + ", " + styleName(f) + ", " + f.getSize() + " pt");
		standardFontButton.setEnabled(a.getFont() != null);

		for(ThemeCard c : cards) {
			c.setSelected(!t.isCustom() && c.theme.equals(t));
		}
		customCard.theme = t.isCustom() ? t : null;
		customCard.setSelected(t.isCustom());
		customCard.repaint();
		themeName.setText(t.isCustom() ? "Your own theme, made from the color you picked"
				: t.getName() + (t == Theme.GOLD ? " (the default)" : "") + (t.isDark() ? ": light text on dark backgrounds" : ""));

		// the preview, painted in the chosen theme (not the one in effect)
		preview.setBackground(t.getStart());
		preview.setForeground(t.getEnd());
		previewLabel.setForeground(t.getText());
		previewLabel.setFont(f);
		previewField.setBackground(t.getField());
		previewField.setForeground(t.getFieldText());
		previewField.setFont(f);
		previewButton.setBackground(t.getStart());
		previewButton.setForeground(t.getEnd());
		previewButton.setTextColor(t.getButtonText());
		previewTable.setForeground(t.getText());
		previewTable.setGridColor(t.getAccent());
		previewTable.setSelectionBackground(t.getSelection());
		previewTable.setSelectionForeground(t.getSelectionText());
		previewTable.setFont(f);
		previewTable.setRowHeight(previewTable.getFontMetrics(f).getHeight() + 4);
		DefaultTableCellRenderer header = new DefaultTableCellRenderer();
		header.setOpaque(true);
		header.setBackground(t.getHeader());
		header.setForeground(t.getHeaderText());
		header.setFont(f.deriveFont(Font.BOLD));
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, t.getAccent()));
		previewTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				header.setText(value == null ? "" : value.toString());
				return header;
			}
		});
		// the table shows all its rows at any font size
		int headerHeight = previewTable.getFontMetrics(f.deriveFont(Font.BOLD)).getHeight() + 8;
		previewScroll.setPreferredSize(new Dimension(440,
				headerHeight + previewTable.getRowHeight() * previewTable.getRowCount() + 4));
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
