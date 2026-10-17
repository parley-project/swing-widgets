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
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import us.bringardner.swing.gradient.Appearance;
import us.bringardner.swing.gradient.Appearance.Scheme;
import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientLook;
import us.bringardner.swing.gradient.GradientPanel;

/**
 * The appearance settings: the font, and the colors of the gradient look, chosen from named
 * schemes or made from any one color. A preview shows the choice before it is used. OK puts it
 * into effect in every open window and, given a preferences node, saves it; Cancel changes
 * nothing.
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

	private final JLabel fontSample = new JLabel("<html><div style='width:260px'>" + SAMPLE_TEXT + "</div></html>");
	private final JLabel fontName = new JLabel();
	private final JButton standardFontButton = new JButton("Use the standard font");
	private final List<Swatch> swatches = new ArrayList<>();
	private Swatch customSwatch;
	private final GradientPanel preview = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
	private final GradientButton previewButton = new GradientButton("Open");
	private JScrollPane previewScroll;
	private final JTable previewTable = new JTable(new DefaultTableModel(new Object[][] {
		{"Reports", "Folder", ""}, {"budget.xlsx", "File", "48 KB"}, {"notes.txt", "File", "2 KB"}},
			new Object[] {"Name", "Type", "Size"}));

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
		if( original.getScheme() == null ) {
			customColor = original.getEnd();
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

	/** Chooses one of the named schemes. */
	public void selectScheme(Scheme scheme) {
		chosen = chosen.withScheme(scheme);
		show(chosen);
	}

	/** Chooses a scheme made from one color (what Custom... does once a color is picked). */
	public void selectColor(Color color) {
		customColor = color;
		chosen = chosen.withScheme(Scheme.fromColor("Custom", color));
		show(chosen);
	}

	/** Chooses a font, or null for the look and feel's standard font. */
	public void selectFont(Font font) {
		chosen = chosen.withFont(font);
		show(chosen);
	}

	/** Back to the gold scheme and the standard font (in effect only after OK). */
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
		Font current = chosen.getFont() != null ? chosen.getFont() : fontSample.getFont();
		Font f = d.showDialog(current);
		if( f != null ) {
			selectFont(f);
		}
	}

	protected void actionCustomColor() {
		Color start = customColor != null ? customColor : chosen.getEnd();
		Color c = JColorChooser.showDialog(this, "Choose a color for the scheme", start);
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

		sections.add(heading("Font"));
		sections.add(fontSection());
		sections.add(Box.createVerticalStrut(16));
		sections.add(heading("Colors"));
		sections.add(note("Choose a color scheme. Custom makes one from any color you pick."));
		sections.add(Box.createVerticalStrut(6));
		sections.add(swatchRow());
		sections.add(Box.createVerticalStrut(16));
		sections.add(heading("Preview"));
		sections.add(previewSection());

		content.add(buttons(), BorderLayout.SOUTH);

		getRootPane().registerKeyboardAction(e->cancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		GradientLook.apply(this);
	}

	private JComponent fontSection() {
		JPanel p = new JPanel(new BorderLayout(10, 4));
		p.setAlignmentX(LEFT_ALIGNMENT);
		fontSample.setOpaque(true);
		fontSample.setBackground(Color.white);
		fontSample.setForeground(Color.black);
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

	private JComponent swatchRow() {
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		row.setAlignmentX(LEFT_ALIGNMENT);
		for(Scheme s : Appearance.SCHEMES) {
			Swatch sw = new Swatch(s.getName(), s.getStart(), s.getEnd(), ()->selectScheme(s));
			swatches.add(sw);
			row.add(sw);
		}
		customSwatch = new Swatch("Custom...", null, null, this::actionCustomColor);
		customSwatch.setToolTipText("Pick any color: the scheme is made from light and dark shades of it");
		row.add(customSwatch);
		return row;
	}

	private JComponent previewSection() {
		preview.setLayout(new BorderLayout(0, 8));
		preview.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(0, 0, 0, 60)), new EmptyBorder(10, 10, 10, 10)));
		preview.setAlignmentX(LEFT_ALIGNMENT);
		previewTable.setEnabled(false);
		previewTable.setShowGrid(true);
		previewTable.setRowHeight(previewTable.getRowHeight() + 2);
		JScrollPane sp = new JScrollPane(previewTable, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		previewScroll = sp;
		GradientLook.seeThrough(sp);
		sp.setColumnHeaderView(previewTable.getTableHeader());
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
		l.setBorder(new EmptyBorder(0, 0, 4, 0));
		l.setAlignmentX(LEFT_ALIGNMENT);
		return l;
	}

	private static JLabel note(String text) {
		JLabel l = new JLabel(text);
		l.setAlignmentX(LEFT_ALIGNMENT);
		return l;
	}

	/** Shows a choice in every part of the dialog. */
	private void show(Appearance a) {
		Font f = a.getFont() != null ? a.getFont() : new JLabel().getFont();
		fontSample.setFont(f.deriveFont(Math.max(f.getSize2D(), 13f)));
		fontName.setText(a.getFont() == null
				? "The standard font"
				: f.getFamily() + ", " + styleName(f) + ", " + f.getSize() + " pt");
		standardFontButton.setEnabled(a.getFont() != null);

		Scheme scheme = a.getScheme();
		for(Swatch s : swatches) {
			s.setSelected(scheme != null && s.matches(a.getStart(), a.getEnd()));
		}
		if( scheme == null ) {
			customSwatch.setColors(a.getStart(), a.getEnd());
		} else {
			customSwatch.setColors(null, null);
		}
		customSwatch.setSelected(scheme == null);

		// the preview: GradientPanel paints from its foreground (dark) to its background (light)
		preview.setBackground(a.getStart());
		preview.setForeground(a.getEnd());
		previewButton.setBackground(a.getStart());
		previewButton.setForeground(a.getEnd());
		previewTable.setGridColor(a.getEnd());
		previewTable.getTableHeader().setBackground(a.getEnd());
		previewTable.getTableHeader().setForeground(Color.white);
		previewTable.setFont(f);
		previewTable.getTableHeader().setFont(f.deriveFont(Font.BOLD));
		previewTable.setRowHeight(previewTable.getFontMetrics(f).getHeight() + 4);
		// the table shows all its rows at any font size
		int header = previewTable.getFontMetrics(f.deriveFont(Font.BOLD)).getHeight() + 8;
		previewScroll.setPreferredSize(new Dimension(420, header + previewTable.getRowHeight() * previewTable.getRowCount() + 4));
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
	 * A color scheme to click on: a rounded tile painted with its gradient, its name underneath, a
	 * ring when it is the one chosen. Without colors (Custom... before a color is picked) it shows
	 * a spectrum.
	 */
	static class Swatch extends JPanel {

		private static final long serialVersionUID = 1L;
		static final int W = 64;
		static final int H = 44;

		private Color start;
		private Color end;
		private boolean selected;
		private final JLabel label;

		Swatch(String name, Color start, Color end, Runnable onClick) {
			super(new BorderLayout(0, 3));
			this.start = start;
			this.end = end;
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
			getAccessibleContext().setAccessibleName(name + " color scheme");
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					onClick.run();
				}
			});
		}

		void setColors(Color start, Color end) {
			this.start = start;
			this.end = end;
			repaint();
		}

		boolean matches(Color start, Color end) {
			return this.start != null && this.start.equals(start) && this.end.equals(end);
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
			int inset = 3;
			int tw = w - inset * 2;
			int th = h - inset * 2;
			if( start != null ) {
				g.setPaint(new GradientPaint(inset, inset, start, inset + tw, inset + th, end));
			} else {
				float[] hues = {0f, 0.17f, 0.33f, 0.5f, 0.67f, 0.83f};
				g.setPaint(new java.awt.LinearGradientPaint(inset, inset, inset + tw, inset + th,
						new float[] {0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f}, colors(hues)));
			}
			g.fillRoundRect(inset, inset, tw, th, 10, 10);
			if( selected ) {
				g.setStroke(new BasicStroke(3f));
				g.setColor(new Color(0, 0, 0, 170));
				g.drawRoundRect(inset - 1, inset - 1, tw + 1, th + 1, 12, 12);
			} else {
				g.setStroke(new BasicStroke(1f));
				g.setColor(new Color(0, 0, 0, 70));
				g.drawRoundRect(inset, inset, tw - 1, th - 1, 10, 10);
			}
			g.dispose();
		}

		private static Color[] colors(float[] hues) {
			Color[] ret = new Color[hues.length];
			for(int i = 0; i < hues.length; i++) {
				ret[i] = Color.getHSBColor(hues[i], 0.45f, 0.95f);
			}
			return ret;
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
