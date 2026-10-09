/**
 * <PRE>
 * 
 * Copyright Tony Bringarder 1998, 2025 <A href="http://bringardner.com/tony">Tony Bringardner</A>
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
 *
 *
 * ~version~V000.01.26-V000.01.21-V000.01.20-V000.01.19-
 */
package us.bringardner.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ColorUIResource;

import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientColors;
import us.bringardner.swing.gradient.GradientPanel;
import us.bringardner.swing.ui.ScrollBarUI;


public class FontDialog extends JDialog {

	public class FontRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("rawtypes")
		@Override
		public Component getListCellRendererComponent(JList list, Object value,int index, boolean isSelected, boolean cellHasFocus) {
			JLabel ret = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			ret.setFont(new Font(names[index], Font.PLAIN, 12));
			
			if( isSelected ) {
				ret.setOpaque(false);	
			}
			
			return ret;
		}
	}



	private static final long serialVersionUID = 1L;
	private GradientPanel contentPane;
	private JList<String> fontList;
	private GradientPanel buttonPanel=new GradientPanel(GradientPanel.DIAGONAL_LEFT);
	protected boolean canceled;
	private JLabel previewLabel;
	private GradientPanel previewPanel= new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
	private GradientPanel centerPanel=new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
	private JScrollPane scrollPane_1;
	private JScrollPane scrollPane_2;
	private JList<String> styleList;
	private JList<Integer> sizeList;
	private static String [] styles = {"Plain","Bold","Italic","Bold Italic"};
	private String names [] ;
	private JScrollPane scrollPane_3;

	/**
	 * Launch the application.
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
		UIDefaults defaults = javax.swing.UIManager.getDefaults();
		
		UIManager.put("ScrollBar.background",new ColorUIResource(GradientColors.getStart()));		
		UIManager.put("ScrollBar.thumb",new ColorUIResource(GradientColors.getStart()));
		defaults.put("ScrollBar.track",new ColorUIResource(GradientColors.getStart()));
		UIManager.put("ScrollBar.trackForeground", new ColorUIResource(Color.black));
		
		UIManager.put("Panel.background", new Color(110, 110, 110, 255));
		defaults.put("ScrollBarUI", ScrollBarUI.class.getCanonicalName());
		
		FontDialog d = new FontDialog();
		Font font = new Font(Font.MONOSPACED,Font.ITALIC,20);
		
		SwingUtilities.invokeLater(()->{
			Font f = d.showDialog(font);
			System.out.println("ret="+f);
		});

	}

	/**
	 * Create the frame.
	 */
	public FontDialog() {
		setBounds(100, 100, 475, 455);
		contentPane = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setForeground(GradientColors.getEnd());
		contentPane.setBackground(GradientColors.getStart());

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		Font [] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		Map<String,Font> map = new TreeMap<>();

		for (int idx = 0; idx < fonts.length; idx++) {
			map.put(fonts[idx].getFamily(), fonts[idx]);
		}

		names = new String[map.size()];
		int pos = 0;
		for(String name : map.keySet()) {
			names[pos++] = name;
		}




		buttonPanel = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
		buttonPanel.setForeground(GradientColors.getEnd());
		buttonPanel.setBackground(GradientColors.getStart());

		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		GradientButton okButton = new GradientButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionOk();
			}
		});
		GradientButton cancelButton = new GradientButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canceled = true;
				dispose();
			}
		});
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		previewPanel = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
		previewPanel.setForeground(GradientColors.getEnd());
		previewPanel.setBackground(GradientColors.getStart());


		previewPanel.setPreferredSize(new Dimension(600, 100));
		contentPane.add(previewPanel, BorderLayout.NORTH);
		
		previewPanel.setLayout(new BorderLayout(0, 0));

		scrollPane_3 = new JScrollPane();
		previewPanel.add(scrollPane_3, BorderLayout.CENTER);

		previewLabel = new JLabel("The quick brown fox jumped over the lazy dog.");
		previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane_3.setViewportView(previewLabel);
		scrollPane_3.setOpaque(false);
		scrollPane_3.getViewport().setOpaque(false);

		contentPane.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setForeground(GradientColors.getEnd());
		centerPanel.setBackground(GradientColors.getStart());

		centerPanel.setLayout(new GridLayout(0, 3, 0, 0));

		JScrollPane scrollPane = new JScrollPane();
		centerPanel.add(scrollPane);
		fontList = new JList<String>();
		fontList.setCellRenderer(new FontRenderer());
		fontList.setModel(new AbstractListModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public int getSize() {
				return names.length;
			}

			@Override
			public String getElementAt(int index) {
				return names[index];
			}

		});
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		fontList.setOpaque(false);
		fontList.setBackground(new Color(255, 255, 255, 0));



		fontList.setModel(new AbstractListModel<String>() {
			private static final long serialVersionUID = 1L;

			String[] values = names;
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setViewportView(fontList);

		scrollPane_1 = new JScrollPane();
		centerPanel.add(scrollPane_1);

		styleList = new JList<String>();
		scrollPane_1.setViewportView(styleList);
		scrollPane_1.setOpaque(false);
		scrollPane_1.getViewport().setOpaque(false);
		styleList.setOpaque(false);
		styleList.setBackground(new Color(255, 255, 255, 0));
		styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		styleList.setModel(new AbstractListModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public int getSize() {
				return styles.length;
			}

			@Override
			public String getElementAt(int index) {
				return styles[index];
			}

		});

		scrollPane_2 = new JScrollPane();

		sizeList = new JList<Integer>();
		scrollPane_2.setViewportView(sizeList);
		centerPanel.add(scrollPane_2);
		scrollPane_2.setOpaque(false);
		scrollPane_2.getViewport().setOpaque(false);
		sizeList.setOpaque(false);
		sizeList.setBackground(new Color(255, 255, 255, 0));
		sizeList.setModel(new AbstractListModel<Integer>() {

			private static final long serialVersionUID = 1L;

			@Override
			public int getSize() {
				return 95;
			}

			@Override
			public Integer getElementAt(int index) {
				return index+5;
			}

		});
		fontList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updatePreview();
			}
		});

		styleList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updatePreview();
			}
		});

		sizeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updatePreview();
			}
		});

		
		styleList.setSelectedIndex(0);
		sizeList.setSelectedIndex(7);
	}

	protected void updatePreview() {
		String fString = fontList.getSelectedValue();
		int style  = styleList.getSelectedIndex();
		int sz = sizeList.getSelectedIndex()+5;

		Font font = new Font(fString, style, sz);
		previewLabel.setFont(font);
		
	}

	protected void actionOk() {
		canceled = false;
		dispose();

	}

	public Font showDialog(Font current) {
		setModal(true);
		Font ret = null;
		if( current != null ) {
			fontList.setSelectedValue(current.getName(), true);
			styleList.setSelectedValue(styles[current.getStyle()], true);
			sizeList.setSelectedValue(current.getSize(), true);
		}

		setLocationRelativeTo(null);
		setVisible(true);
		if( !canceled ) {
			ret = previewLabel.getFont();
		}

		return ret;
	}

}
