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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JTextField;

public class TextFieldPanel extends JLayeredPane {

	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private JLabel promptLabel;
	
	
	/**
	 * Create the panel.  
	 */
	public TextFieldPanel() {
		textField = new JTextField();
		textField.setBounds(0, 2, 243, 26);
		setLayer(textField, 1);
		textField.setColumns(15);		
		add(textField);
		
		promptLabel = new JLabel("Prompt");
		promptLabel.setBounds(10, 7, 45, 16);
		setLayer(promptLabel, 2);
		promptLabel.setForeground(new Color(166,166,166));
		add(promptLabel);
		
		
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				TextFieldPanel.this.updateUI();
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				TextFieldPanel.this.updateUI();
			}
		});
		
	}
	
	public TextFieldPanel(String displayName, String val) {
		this();
		setPrompt(displayName);
		if( val !=null ) {
			setText(val);
		}
	}
	
	

	@Override
	public void paint(Graphics g) {
		
		if( textField.getText().trim().isEmpty()) {
			promptLabel.setVisible(true);
		} else {
			promptLabel.setVisible(false);			
		}
		super.paint(g);
	}


	public void setColumns(int columns) {
		textField.setColumns(columns);
	}
	
	public int getColumns() {
		return  textField.getColumns();
	}
	
	public String getPrompt() {
		return promptLabel.getText();
	}

	public void setPrompt(String prompt) {
		this.promptLabel.setText(prompt);
		updateUI();
	}
	
	public void setText(String text) {
		textField.setText(text);
		updateUI();
	}
	
	public String getText() {
		return textField.getText();
	}
}
