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
 * ~version~V000.01.19-
 */
package us.bringardner.swing.field;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


public class PasswordPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField textField = new JTextField();
	private JPasswordField passwordField = new JPasswordField();
	private JLabel showLabel;
	private JLayeredPane layeredPane;

	/**
	 * Create the panel.
	 */
	public PasswordPanel() {
		setToolTipText("Password");
		setBackground(new Color(255,255,255,0));
		setLayout(new BorderLayout(0, 0));
		
		layeredPane = new JLayeredPane();
		add(layeredPane, BorderLayout.CENTER);
		
		
		textField = new JTextField();
		layeredPane.setLayer(textField, 0);
		textField.setBounds(6, 4, 303, 26);
		layeredPane.add(textField);
		textField.setToolTipText("Password");
		textField.setColumns(15);
		
		passwordField = new JPasswordField();
		layeredPane.setLayer(passwordField, 1);
		passwordField.setBounds(6, 4, 303, 26);
		layeredPane.add(passwordField);
		passwordField.setColumns(15);
		passwordField.setToolTipText("Password");
		
		hideLabel = new JLabel("");
		hideLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				actionHidePassword();
			}
		});
		hideLabel.setIcon(new ImageIcon(PasswordPanel.class.getResource("/us/bringardner/swing/icons/OpenEyeBw_40_17.png")));
		layeredPane.setLayer(hideLabel, 0);
		hideLabel.setBounds(259, 9, 50, 16);
		layeredPane.add(hideLabel);
		
		showLabel = new JLabel("");
		showLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				actionShowPassword();
			}
		});
		showLabel.setIcon(new ImageIcon(PasswordPanel.class.getResource("/us/bringardner/swing/icons/ClosedEyeBw_40_17.png")));
		layeredPane.setLayer(showLabel, 2);
		showLabel.setBounds(259, 9, 49, 16);
		layeredPane.add(showLabel);
		//actionHidePassword();

	}

	private boolean init = true;
	private JLabel hideLabel;
	
	@Override
	public void paint(Graphics g) {
		if( init ) {
			init = false;
		}
		super.paint(g);
	}
	
	public PasswordPanel(String val) {
		this();
		if( val !=null ) {
			setPassword(val);
		}
	}

	public void calculateBounds() {
		Font font = getFont();
		if( font == null ) {
			font = textField.getFont();
		}
		FontMetrics fm = getFontMetrics(font);
		int cols = textField.getColumns();		
		byte[] data = new byte[cols];
		for (int idx = 0; idx < data.length; idx++) {
			data[idx] = 'X';
		}
		int stringWidth = fm.bytesWidth(data,0,data.length);
		int stringHieight = fm.getHeight();
		passwordField.setSize(stringWidth, stringHieight);
		textField.setSize(stringWidth, stringHieight);
		int w = (int)((stringWidth+30)*1.1);
		int h = (int)((stringHieight+30)*1.1);
		setSize(w, h);
		int yOffset = 2;
		passwordField.setLocation(0, yOffset);
		textField.setLocation(0, yOffset);
	}
	
	protected void actionShowPassword() {
		textField.setText(new String(passwordField.getPassword()));
		textField.setVisible(true);
		passwordField.setVisible(false);
		hideLabel.setVisible(true);		
		showLabel.setVisible(false);
		layeredPane.setLayer(showLabel, 0);
		layeredPane.setLayer(hideLabel, 2);
	}
	
	protected void actionHidePassword() {
		passwordField.setText(textField.getText());
		passwordField.setVisible(true);
		textField.setVisible(false);		
		hideLabel.setVisible(false);		
		showLabel.setVisible(true);
		layeredPane.setLayer(showLabel, 2);
		layeredPane.setLayer(hideLabel, 0);
	}
	
	public String getPassword() {
		String ret = textField.getText();
		if( passwordField.isVisible()) {
			ret = new String(passwordField.getPassword());
		}
		
		return ret;
	}
	
	public void setPassword(String password) {
		textField.setText(password);
		passwordField.setText(password);
	}
}
