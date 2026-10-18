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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import us.bringardner.swing.gradient.GradientLook;
import us.bringardner.swing.gradient.Theme;
import us.bringardner.swing.gradient.ThemeExtension;
import us.bringardner.swing.gradient.ThemeExtensions;

/**
 * Makes a new theme from an existing one, or changes one of the user's own themes.
 * <p>
 * To design a new theme ({@link #design}): choose the theme to start from (or a color), change any of
 * its colors, watch a preview and the readability checks, and save it under a new name. The theme
 * started from is never changed.
 * <p>
 * To edit one of the user's themes ({@link #edit}): change its colors, and its name if wanted, and
 * save the changes over it. The built-in themes can't be edited.
 * <p>
 * Applications add pages for their own parts of a theme ({@link ThemeExtension}), such as a script
 * editor's syntax colors: a new theme starts with those of the theme it is made from.
 * <p>
 * Saved themes are the user's own ({@link Theme#saveUserTheme}), offered by every application built
 * on swing-widgets.
 */
public class ThemeDesigner extends JDialog {

	private static final long serialVersionUID = 1L;

	/** What each color is for, in the order of {@link Theme#KEYS}. */
	static final String[] ROLE_NAMES = {"Gradient, light end", "Gradient, dark end", "Text on the gradient",
			"Field background", "Field text", "Selected row", "Selected text", "Table header", "Header text",
			"Lines and arrows", "Button text"};

	/** The readability checks: [text, background, minimum ratio x10]. */
	static final int[][] CHECKS = {{2, 0, 45}, {2, 1, 30}, {4, 3, 45}, {6, 5, 30}, {8, 7, 30}, {10, 1, 30}};

	static final String[] CHECK_NAMES = {"Text on the light end", "Text on the dark end", "Field text",
			"Selected text", "Header text", "Button text"};

	private final JComboBox<Object> startFrom = new JComboBox<>();
	private final JTextField name = new JTextField(18);
	private final JCheckBox dark = new JCheckBox("Dark theme: light text on dark backgrounds");
	private final Color[] colors = new Color[Theme.KEYS.size()];
	private final JButton[] swatches = new JButton[colors.length];
	private final JTextField[] hexes = new JTextField[colors.length];
	private final JLabel[] checks = new JLabel[CHECKS.length];
	private final ThemePreview preview = new ThemePreview();
	private final JLabel nameNote = new JLabel(" ");
	private final JButton saveButton = new JButton();
	private boolean updating;
	private Theme saved;
	/** The user's theme being edited, or null when designing a new one. */
	private final Theme editing;
	/** The applications' pages, and their extensions. */
	private final List<ThemeExtension> extensions = ThemeExtensions.all();
	private final List<ThemeExtension.Page> pages = new ArrayList<>();
	/** The applications' settings of the theme started from, kept as they are unless a page changes them. */
	private Map<String, String> startExtras = Map.of();

	/** The choice in "Start from" that makes the starting colors from one color the user picks. */
	static final String FROM_A_COLOR = "A color...";

	/**
	 * @param owner the window it belongs to, or null
	 * @param start the theme to start from
	 */
	public ThemeDesigner(Window owner, Theme start) {
		this(owner, start, false);
	}

	/**
	 * @param owner the window it belongs to, or null
	 * @param theme the theme to start from, or (edit) the user's theme to change
	 * @param edit true to change the theme itself, which must be one of the user's own
	 */
	public ThemeDesigner(Window owner, Theme theme, boolean edit) {
		super(owner, edit ? "Edit Theme" : "New Theme", ModalityType.APPLICATION_MODAL);
		if( edit && !theme.isUserTheme() ) {
			throw new IllegalArgumentException("\"" + theme.getName() + "\" isn't one of your themes");
		}
		editing = edit ? theme : null;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		build();
		startFrom(theme);
		if( edit ) {
			setThemeName(theme.getName());
		}
		pack();
		setResizable(false);
		setLocationRelativeTo(owner);
	}

	/**
	 * Shows the designer and waits.
	 *
	 * @param owner the window it belongs to, or null
	 * @param start the theme to start from
	 * @return the new theme, saved, or null if canceled
	 */
	public static Theme design(Window owner, Theme start) {
		ThemeDesigner d = new ThemeDesigner(owner, start);
		d.setVisible(true);
		return d.getSaved();
	}

	/**
	 * Shows the designer changing one of the user's themes, and waits.
	 *
	 * @param owner the window it belongs to, or null
	 * @param theme the user's theme
	 * @return the theme as changed and saved, or null if canceled
	 */
	public static Theme edit(Window owner, Theme theme) {
		ThemeDesigner d = new ThemeDesigner(owner, theme, true);
		d.setVisible(true);
		return d.getSaved();
	}

	/** True when changing one of the user's themes rather than designing a new one. */
	public boolean isEditing() {
		return editing != null;
	}

	/** The theme saved, or null if not (yet). */
	public Theme getSaved() {
		return saved;
	}

	/** The theme as the designer has it now (unsaved). */
	public Theme getTheme() {
		String n = name.getText().trim();
		Theme t = Theme.of(n.isEmpty() ? "Untitled" : n, colors.clone(), dark.isSelected()).withExtras(startExtras);
		for(int i = 0; i < pages.size(); i++) {
			Map<String, String> v = pages.get(i).getValues();
			if( v != null ) {
				t = t.withValues(extensions.get(i), v);
			}
		}
		return editing != null && editing.getDescription() != null ? t.withDescription(editing.getDescription()) : t;
	}

	// ---------------------------------------------------------------- actions

	/** Starts from a theme: its colors, and a new name made from its name. */
	public void startFrom(Theme t) {
		updating = true;
		System.arraycopy(t.colors(), 0, colors, 0, colors.length);
		dark.setSelected(t.isDark());
		// the applications' settings come from the theme started from too
		startExtras = t.getExtras();
		for(int i = 0; i < pages.size(); i++) {
			pages.get(i).setValues(t.values(extensions.get(i)), t);
		}
		if( editing == null ) {
			selectInStartFrom(t);
		}
		if( editing == null && (name.getText().trim().isEmpty() || isSuggested(name.getText())) ) {
			name.setText(suggestName(t.getName()));
		}
		updating = false;
		refresh();
	}

	/** Starts from a theme made from one color. */
	public void startFromColor(Color c) {
		Theme t = Theme.fromColor(c);
		startFrom(t);
	}

	/** Sets one color (by its index in {@link Theme#KEYS}). */
	public void setColor(int role, Color c) {
		colors[role] = c;
		refresh();
	}

	/** Sets the new theme's name. */
	public void setThemeName(String n) {
		name.setText(n);
		refresh();
	}

	public void setDark(boolean b) {
		dark.setSelected(b);
		refresh();
	}

	/**
	 * Saves the theme, as a new one of the user's or over the one being edited, and closes; or
	 * explains why it can't.
	 *
	 * @return true if saved
	 */
	public boolean save() {
		String problem = nameProblem();
		if( problem != null ) {
			nameNote.setText(problem);
			return false;
		}
		try {
			saved = Theme.saveUserTheme(getTheme());
			if( editing != null && !editing.getName().trim().equalsIgnoreCase(saved.getName()) ) {
				// renamed: the old one goes
				Theme.deleteUserTheme(editing.getName());
			}
		} catch (IOException | RuntimeException e) {
			nameNote.setText("Can't save it: " + e.getMessage());
			return false;
		}
		dispose();
		return true;
	}

	public void cancel() {
		saved = null;
		dispose();
	}

	/** Why the name can't be used, or null if it can. */
	String nameProblem() {
		String n = name.getText().trim();
		if( n.isEmpty() ) {
			return "Give the theme a name";
		}
		if( Theme.CUSTOM.equalsIgnoreCase(n) ) {
			return "\"" + Theme.CUSTOM + "\" is kept for themes made from one color";
		}
		if( editing != null && editing.getName().trim().equalsIgnoreCase(n) ) {
			return null;
		}
		if( Theme.named(n) != null ) {
			return "There is already a theme called \"" + Theme.named(n).getName() + "\"";
		}
		return null;
	}

	/** A name for a theme started from another: "Ocean 2", "Ocean 3", ... the first that's free. */
	static String suggestName(String from) {
		String base = from.replaceAll("\\s+\\d+$", "");
		if( Theme.CUSTOM.equals(base) ) {
			base = "My theme";
		}
		for(int i = 2; ; i++) {
			String n = base + " " + i;
			if( Theme.named(n) == null ) {
				return n;
			}
		}
	}

	private boolean isSuggested(String n) {
		return n.matches(".*\\s\\d+$");
	}

	// ---------------------------------------------------------------- the dialog's parts

	private void build() {
		JPanel content = new JPanel(new BorderLayout(16, 12));
		content.setBorder(new EmptyBorder(16, 18, 12, 18));
		setContentPane(content);

		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		if( editing == null ) {
			left.add(row("Start from:", startFromBox()));
			left.add(Box.createVerticalStrut(8));
		}
		left.add(row("Name:", name));
		nameNote.setAlignmentX(LEFT_ALIGNMENT);
		nameNote.setFont(nameNote.getFont().deriveFont(Font.ITALIC));
		left.add(nameNote);
		left.add(Box.createVerticalStrut(8));
		dark.setAlignmentX(LEFT_ALIGNMENT);
		dark.addActionListener(e->refresh());
		left.add(dark);
		left.add(Box.createVerticalStrut(8));
		left.add(colorGrid());
		if( extensions.isEmpty() ) {
			content.add(left, BorderLayout.WEST);
		} else {
			// a tab for the look's colors, and one for each application's page; the gradient shows
			// through the tabs' content, as through the rest of the dialog
			Object contentOpaque = UIManager.get("TabbedPane.contentOpaque");
			UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
			JTabbedPane tabs = new JTabbedPane();
			UIManager.put("TabbedPane.contentOpaque", contentOpaque);
			tabs.setOpaque(false);
			left.setOpaque(false);
			dark.setOpaque(false);
			left.setBorder(new EmptyBorder(8, 8, 8, 8));
			tabs.addTab("Colors", left);
			for(ThemeExtension x : extensions) {
				ThemeExtension.Page page = x.createPage();
				page.setChangeListener(this::refresh);
				pages.add(page);
				tabs.addTab(x.getTitle(), page.getComponent());
			}
			content.add(tabs, BorderLayout.WEST);
		}

		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		right.add(heading("Preview"));
		preview.setPreviewWidth(380);
		preview.setAlignmentX(LEFT_ALIGNMENT);
		right.add(preview);
		right.add(Box.createVerticalStrut(12));
		right.add(heading("Readability"));
		JPanel checkPanel = new JPanel(new GridBagLayout());
		checkPanel.setAlignmentX(LEFT_ALIGNMENT);
		for(int i = 0; i < checks.length; i++) {
			checks[i] = new JLabel();
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = i;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(1, 0, 1, 12);
			checkPanel.add(new JLabel(CHECK_NAMES[i]), c);
			c.gridx = 1;
			checkPanel.add(checks[i], c);
		}
		// the list sits on the left
		GridBagConstraints fill = new GridBagConstraints();
		fill.gridx = 2;
		fill.weightx = 1;
		checkPanel.add(Box.createHorizontalGlue(), fill);
		right.add(checkPanel);
		JLabel note = new JLabel("<html><div style='width:360px'>The contrast between text and what is behind it: "
				+ "4.5 or more reads easily; 3 is the least for large or bold text and selected rows.</div></html>");
		note.setFont(note.getFont().deriveFont(note.getFont().getSize2D() - 1f));
		note.setAlignmentX(LEFT_ALIGNMENT);
		right.add(Box.createVerticalStrut(4));
		right.add(note);
		content.add(right, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e->cancel());
		saveButton.setText(editing == null ? "Save as new theme" : "Save changes");
		saveButton.addActionListener(e->save());
		buttons.add(cancelButton);
		buttons.add(saveButton);
		content.add(buttons, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(saveButton);
		getRootPane().registerKeyboardAction(e->cancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		name.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				refresh();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				refresh();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				refresh();
			}
		});
		GradientLook.apply(this);
	}

	private JComponent startFromBox() {
		for(Theme t : Theme.all()) {
			startFrom.addItem(t);
		}
		startFrom.addItem(FROM_A_COLOR);
		startFrom.addActionListener(e->{
			if( updating ) {
				return;
			}
			Object o = startFrom.getSelectedItem();
			if( o instanceof Theme ) {
				startFrom((Theme) o);
			} else if( FROM_A_COLOR.equals(o) ) {
				Color c = JColorChooser.showDialog(this, "Start from a color", colors[1]);
				if( c != null ) {
					startFromColor(c);
				}
			}
		});
		return startFrom;
	}

	private void selectInStartFrom(Theme t) {
		for(int i = 0; i < startFrom.getItemCount(); i++) {
			Object o = startFrom.getItemAt(i);
			if( o instanceof Theme && ((Theme) o).getName().equals(t.getName()) && ((Theme) o).sameColors(t) ) {
				startFrom.setSelectedIndex(i);
				return;
			}
		}
		startFrom.setSelectedItem(FROM_A_COLOR);
	}

	private JComponent colorGrid() {
		JPanel grid = new JPanel(new GridBagLayout()) {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getMaximumSize() {
				// on the left, at its own size, however wide the column is
				return getPreferredSize();
			}
		};
		grid.setAlignmentX(LEFT_ALIGNMENT);
		grid.setOpaque(false);
		for(int i = 0; i < colors.length; i++) {
			final int role = i;
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = i;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(2, 0, 2, 8);
			c.gridx = 0;
			grid.add(new JLabel(ROLE_NAMES[i]), c);

			swatches[i] = new JButton(new SwatchIcon(()->colors[role]));
			swatches[i].setToolTipText("Choose the " + ROLE_NAMES[i].toLowerCase());
			swatches[i].setMargin(new Insets(2, 2, 2, 2));
			swatches[i].addActionListener(e->{
				Color chosen = JColorChooser.showDialog(this, ROLE_NAMES[role], colors[role]);
				if( chosen != null ) {
					setColor(role, chosen);
				}
			});
			c.gridx = 1;
			grid.add(swatches[i], c);

			hexes[i] = new JTextField(7);
			hexes[i].setToolTipText("#RRGGBB");
			hexes[i].addActionListener(e->readHex(role));
			hexes[i].addFocusListener(new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					readHex(role);
				}
			});
			c.gridx = 2;
			grid.add(hexes[i], c);
		}
		return grid;
	}

	/** Takes a color typed in a hex field; a bad one is put back as it was. */
	void readHex(int role) {
		try {
			Color c = Color.decode(hexes[role].getText().trim());
			if( !c.equals(colors[role]) ) {
				setColor(role, c);
			}
		} catch (NumberFormatException e) {
			hexes[role].setText(Theme.hex(colors[role]));
		}
	}

	/** Shows the colors, the preview, the checks and whether the name can be saved. */
	private void refresh() {
		if( updating ) {
			return;
		}
		for(int i = 0; i < colors.length; i++) {
			hexes[i].setText(Theme.hex(colors[i]));
			swatches[i].repaint();
		}
		Theme t = getTheme();
		preview.show(t, null);
		for(int i = 0; i < CHECKS.length; i++) {
			double ratio = Theme.contrast(colors[CHECKS[i][0]], colors[CHECKS[i][1]]);
			boolean ok = ratio * 10 >= CHECKS[i][2];
			checks[i].setText(String.format("%.1f  %s", ratio, ok ? "✓ reads well" : "⚠ hard to read"));
			checks[i].setForeground(ok ? UIManager.getColor("Label.foreground") : new Color(0xB00020));
			checks[i].setFont(checks[i].getFont().deriveFont(ok ? Font.PLAIN : Font.BOLD));
		}
		String problem = nameProblem();
		nameNote.setText(problem == null ? " " : problem);
		saveButton.setEnabled(problem == null);
	}

	private static JComponent row(String label, JComponent c) {
		JPanel p = new JPanel(new BorderLayout(8, 0));
		JLabel l = new JLabel(label);
		l.setPreferredSize(new Dimension(80, l.getPreferredSize().height));
		p.add(l, BorderLayout.WEST);
		p.add(c, BorderLayout.CENTER);
		p.setAlignmentX(LEFT_ALIGNMENT);
		p.setOpaque(false);
		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
		return p;
	}

	private static JLabel heading(String text) {
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize2D() + 1f));
		l.setBorder(new EmptyBorder(0, 0, 6, 0));
		l.setAlignmentX(LEFT_ALIGNMENT);
		return l;
	}

	/** A square of a color, for a swatch button. */
	static class SwatchIcon implements Icon {
		private final java.util.function.Supplier<Color> color;

		SwatchIcon(java.util.function.Supplier<Color> color) {
			this.color = color;
		}

		@Override
		public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
			g.setColor(color.get());
			g.fillRect(x, y, getIconWidth(), getIconHeight());
			g.setColor(new Color(0, 0, 0, 120));
			g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
		}

		@Override
		public int getIconWidth() {
			return 28;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	}

	/** The checks' labels, for tests. */
	List<String> checkTexts() {
		List<String> ret = new java.util.ArrayList<>();
		for(JLabel l : checks) {
			ret.add(l.getText());
		}
		return ret;
	}

	/** Launch the designer on its own, with the gradient look. */
	public static void main(String[] args) {
		GradientLook.install();
		java.awt.EventQueue.invokeLater(()->{
			design(null, Theme.current());
			System.exit(0);
		});
	}
}
