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
 * ~version~V000.01.40-V000.01.39-V000.01.27-V000.01.26-V000.01.V21_5-V000.01.21_3-V000.01.18-V000.01.17-V000.01.05-V000.01.00-
 */
package us.bringardner.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;

import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientPanel;



public class MessageDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public enum Response {Yes,No}

	public static final ImageIcon warningIcon = new ImageIcon(MessageDialog.class.getResource("/us/bringardner/swing/icons/Warning100x100.png"));
	public static final ImageIcon ErrorIcon   = new ImageIcon(MessageDialog.class.getResource("/us/bringardner/swing/icons/Error100x100.png"));
	public static final ImageIcon MessageIcon   = new ImageIcon(MessageDialog.class.getResource("/us/bringardner/swing/icons/Globe100x100.png"));



	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		showWarringDialog("ssage", "title");
	}

	private final JPanel contentPanel = new GradientPanel(3);
	private ImageIcon icon = MessageIcon;	
	Color startColor = new Color(242, 206, 113);
	Color endColor = new Color(150, 123, 40);
	
	private boolean canceled=false;
	private String message="";
	private String yesButtonText="Yes";
	private String noButtonText="No";
	private GradientButton yesButton;
	private GradientButton noButton;
	private JLabel iconLabel;
	private JLabel mesageLabel;
	private JTextField textField;
	private boolean getInput = false;
	private boolean showAppliesToAll = false;
	private JCheckBox applyToAllCheckBox;

	
	public boolean isShowAppliesToAll() {
		return showAppliesToAll;
	}
	public void setShowAppliesToAll(boolean showAppliesToAll) {
		this.showAppliesToAll = showAppliesToAll;
	}
	public boolean applyToAll() {
		return applyToAllCheckBox.isSelected();
	}
	public void setApppyToAll(boolean b) {
		applyToAllCheckBox.setSelected(b);
	}
	public String getUserInput() {
		return textField.getText();
	}
	public void setUserInput(String text) {
		textField.setText(text);
	}
	public boolean isGetInput() {
		return getInput;
	}
	public void setGetInput(boolean getInput) {
		this.getInput = getInput;
	}
	public String getYesButtonText() {
		return yesButtonText;
	}
	public void setYesButtonText(String yesButtonText) {
		this.yesButtonText = yesButtonText;
	}
	public String getNoButtonText() {
		return noButtonText;
	}
	public void setNoButtonText(String noButtonText) {
		this.noButtonText = noButtonText;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ImageIcon getIcon() {
		return icon;
	}
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	public Color getStartColor() {
		return startColor;
	}
	public void setStartColor(Color startColor) {
		this.startColor = startColor;
	}
	public Color getEndColor() {
		return endColor;
	}
	public void setEndColor(Color endColor) {
		this.endColor = endColor;
	}

	public void setShowYesButton(boolean b) {
		yesButton.setVisible(b);
	}
	
	public void setShowNoButton(boolean b) {
		noButton.setVisible(b);
	}
	
	/**
	 * Create the dialog.
	 */
	public MessageDialog() {
		setBounds(100, 100, 450, 264);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setBackground(startColor);
		contentPanel.setForeground(endColor);

		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		iconLabel = new JLabel();
		iconLabel.setIcon(icon);
		iconLabel.setBounds(312, 20, 100, 107);
		contentPanel.add(iconLabel);
		{
			mesageLabel = new JLabel( getMessage());			
			mesageLabel.setHorizontalAlignment(SwingConstants.CENTER);
			mesageLabel.setBounds(39, 6, 279, 134);
			contentPanel.add(mesageLabel);
		}
		
		textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canceled = false;
				dispose();
			}
		});
		textField.setBounds(83, 141, 283, 26);
		contentPanel.add(textField);
		textField.setColumns(10);
		
		applyToAllCheckBox = new JCheckBox("Apply to all");
		applyToAllCheckBox.setBounds(159, 168, 128, 23);
		contentPanel.add(applyToAllCheckBox);

		{
			JPanel buttonPanel = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
			buttonPanel.setOpaque(false);
			buttonPanel.setBackground(contentPanel.getBackground());
			buttonPanel.setForeground(contentPanel.getForeground());
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			{
				yesButton = new GradientButton("OK");
				yesButton.setText(yesButtonText);
				yesButton.addActionListener((e)->{
					canceled = false;
					dispose();
				});
				buttonPanel.add(yesButton);
			}
			{
				noButton = new GradientButton("Cancel");
				noButton.setText(noButtonText);
				noButton.addActionListener((e)->{
					canceled = true;
					dispose();
				});
				{
					Component horizontalStrut = Box.createHorizontalStrut(20);
					buttonPanel.add(horizontalStrut);
				}
				buttonPanel.add(noButton);
			}
		}
	}

	public Response showErrorDialog(String message) {
		setMessage(message);
		return showErrorDialog();
	}

	public Response showErrorDialog() {
		setIcon(ErrorIcon);
		return showDialog();
	}

	public Response showWarningDialog(String message) {
		setMessage(message);
		return showWarningDialog();
	}

	public Response showWarningDialog() {
		setIcon(warningIcon);
		return showDialog();
	}

	LinkedBlockingQueue<Response> queue = new LinkedBlockingQueue<>();
	
	public Response showDialogThreadSafe() {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(()->{
				queue.add(showDialog());
			});
			
			return queue.poll();
		} else {
			return showDialog();
		}
	}
	
	public Response showDialog() {
		String tmp = getMessage();
		if( !tmp.startsWith("<html>")) {
			tmp = "<html>"+tmp+"</html>";
		}
		textField.setVisible(getInput);
		applyToAllCheckBox.setVisible(showAppliesToAll);
		mesageLabel.setText(tmp);
		iconLabel.setIcon(getIcon());
		yesButton.setText(getYesButtonText());
		noButton.setText(getNoButtonText());
		
		setModal(true);
		setLocationRelativeTo(null);
		setVisible(true);

		if( canceled ) {
			return Response.No;
		} else {
			return Response.Yes;
		}
	}
	
	public static String showInputDialog(String message, String title,String defaultText) {
		MessageDialog d = new MessageDialog();
		d.setGetInput(true);
		d.setAlwaysOnTop(true);
		d.setMessage(message);
		d.setTitle(title);
		d.setNoButtonText("Cancel");
		d.setYesButtonText("Okay");
		if( defaultText != null ) {
			d.setUserInput(defaultText);
		}
		
		if(d.showDialog()==Response.No) {
			return null;
		} else {
			return d.getUserInput();	
		}		
	}
	
	public static void showWarringDialog(String message, String title) {
		MessageDialog d = new MessageDialog();
		d.setAlwaysOnTop(true);
		d.setMessage(message);
		d.setTitle(title);
		d.setShowNoButton(false);
		d.setYesButtonText("Okay");
		d.showWarningDialog();
		
	}
	
	public static void showErrorDialog(String message, String title) {
		MessageDialog d = new MessageDialog();
		d.setAlwaysOnTop(true);
		d.setMessage(message);
		d.setTitle(title);
		d.setShowNoButton(false);
		d.setYesButtonText("Okay");		
		d.showErrorDialog();
		
	}
	public static void showMessageDialog(String message, String title) {
		MessageDialog d = new MessageDialog();
		d.setAlwaysOnTop(true);
		d.setMessage(message);
		d.setTitle(title);
		d.setShowNoButton(false);
		d.setYesButtonText("Okay");		
		d.showDialog();
		
	}
}
