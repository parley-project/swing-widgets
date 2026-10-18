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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientLook;
import us.bringardner.swing.gradient.GradientPanel;
import us.bringardner.swing.gradient.Theme;

/**
 * A small window's worth of a theme, painted in a theme that need not be the one in effect: its
 * gradient, a label and a field, a table with a selected row and a header, and a gradient button.
 * The appearance settings and the theme designer show their choice in one.
 */
public class ThemePreview extends GradientPanel {

	private static final long serialVersionUID = 1L;

	private final JLabel label = new JLabel("Folder:");
	private final JTextField field = new JTextField("Documents/Reports", 16);
	private final GradientButton button = new GradientButton("Open");
	private final JTable table = new JTable(new DefaultTableModel(new Object[][] {
		{"Reports", "Folder", ""}, {"budget.xlsx", "File", "48 KB"}, {"notes.txt", "File", "2 KB"}},
			new Object[] {"Name", "Type", "Size"}) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	});
	private final JScrollPane scroll;
	private final JLabel header = new JLabel();
	private int width = 440;

	public ThemePreview() {
		super(DIAGONAL_RIGHT);
		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(0, 0, 0, 60)), new EmptyBorder(10, 10, 10, 10)));

		JPanel north = new JPanel(new BorderLayout(8, 0));
		north.setOpaque(false);
		north.add(label, BorderLayout.WEST);
		field.setEditable(false);
		field.setFocusable(false);
		north.add(field, BorderLayout.CENTER);
		add(north, BorderLayout.NORTH);

		table.setFocusable(false);
		table.setShowGrid(true);
		table.setRowSelectionInterval(1, 1);
		scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		GradientLook.seeThrough(scroll);
		scroll.setColumnHeaderView(table.getTableHeader());
		scroll.getColumnHeader().setOpaque(false);
		header.setOpaque(true);
		header.setHorizontalAlignment(SwingConstants.CENTER);
		table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				header.setText(value == null ? "" : value.toString());
				return header;
			}
		});
		add(scroll, BorderLayout.CENTER);

		JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		south.setOpaque(false);
		button.setPreferredSize(new Dimension(90, 30));
		south.add(button);
		add(south, BorderLayout.SOUTH);
		show(Theme.current(), null);
	}

	/** The width the table asks for. */
	public void setPreviewWidth(int width) {
		this.width = width;
	}

	/**
	 * Paints the preview in a theme and font.
	 *
	 * @param t the theme
	 * @param font the font, or null for the look and feel's
	 */
	public void show(Theme t, Font font) {
		Font f = font != null ? font : new JLabel().getFont();
		setBackground(t.getStart());
		setForeground(t.getEnd());
		label.setForeground(t.getText());
		label.setFont(f);
		field.setBackground(t.getField());
		field.setForeground(t.getFieldText());
		field.setFont(f);
		button.setBackground(t.getStart());
		button.setForeground(t.getEnd());
		button.setTextColor(t.getButtonText());
		table.setForeground(t.getText());
		table.setGridColor(t.getAccent());
		table.setSelectionBackground(t.getSelection());
		table.setSelectionForeground(t.getSelectionText());
		table.setFont(f);
		table.setRowHeight(table.getFontMetrics(f).getHeight() + 4);
		header.setBackground(t.getHeader());
		header.setForeground(t.getHeaderText());
		header.setFont(f.deriveFont(Font.BOLD));
		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, t.getAccent()));
		// the table shows all its rows at any font size
		int headerHeight = table.getFontMetrics(f.deriveFont(Font.BOLD)).getHeight() + 8;
		scroll.setPreferredSize(new Dimension(width, headerHeight + table.getRowHeight() * table.getRowCount() + 4));
		table.getTableHeader().repaint();
		revalidate();
		repaint();
	}
}
