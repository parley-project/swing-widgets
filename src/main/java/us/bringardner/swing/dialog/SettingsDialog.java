package us.bringardner.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import javax.swing.SwingConstants;

import us.bringardner.swing.gradient.GradientButton;
import us.bringardner.swing.gradient.GradientColors;
import us.bringardner.swing.gradient.GradientPanel;

public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final GradientPanel contentPanel = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
	private JPanel buttonPane;

	private JLabel textLabel;
	private GradientButton okButton;
	private GradientButton cancelButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		/*
		UIDefaults defaults = javax.swing.UIManager.getDefaults();

		for(Object key : defaults.keySet()) {
			String name = key.toString();
			if( name.startsWith("Color")) {
					Object val = defaults.get(key);
					System.out.println(name+" = "+val.getClass()+" = "+val);
					if (val instanceof IconUIResource) {
						IconUIResource icon = (IconUIResource) val;
						System.out.println(""+icon.getIconWidth()+" x "+icon.getIconHeight());
					}


			}
		}
		 */
		//System.exit(0);

		//defaults.put("ColorChooser.background", new ColorUIResource(Color.red));
		//defaults.put("ColorChooser.foreground", new ColorUIResource(Color.blue));

		try {
			SettingsDialog dialog = new SettingsDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SettingsDialog() {
		setBounds(100, 100, 669, 387);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setBackground(GradientColors.getStart());
		contentPanel.setForeground(GradientColors.getEnd());
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JPanel centerPanel = new JPanel();
		centerPanel.setBackground(GradientColors.TRANSPARENT);
		contentPanel.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(null);

		textLabel = new JLabel("<html>The quick brown fox jumped over the lazy dog.<br><center><font color=\"green\">Edit</font></center></html>");
		textLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				actionFont();
			}
		});
		textLabel.setBorder(new TitledBorder(null, "Font", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		textLabel.setBounds(133, 14, 393, 158);
		textLabel.setOpaque(true);

		//textLabel.setBackground(GradientColors.TRANSPARENT);
		centerPanel.add(textLabel);

		JLabel startColorLabel = new JLabel("Color 1");
		startColorLabel.setBounds(240, 239, 103, 31);
		centerPanel.add(startColorLabel);
		startColorLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				actionStartColor();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				startColorLabel.setBorder(new LineBorder(startColorLabel.getBackground(), 1, true));
				contentPanel.updateUI();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				startColorLabel.setBorder(new LineBorder(startColorLabel.getForeground(), 1, true));
				contentPanel.updateUI();

			}
		});

		startColorLabel.setBorder(new LineBorder(startColorLabel.getForeground(), 1, true));

		JLabel endColorLabel = new JLabel("Color 2");
		endColorLabel.setBounds(355, 237, 111, 35);
		centerPanel.add(endColorLabel);
		endColorLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				actionEndColor();
			}
			@Override
			public void mousePressed(MouseEvent e) {
				endColorLabel.setBorder(new LineBorder(endColorLabel.getBackground(), 1, true));
				contentPanel.updateUI();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				endColorLabel.setBorder(new LineBorder(endColorLabel.getForeground(), 1, true));
				contentPanel.updateUI();

			}
		});
		endColorLabel.setBorder(new LineBorder(endColorLabel.getForeground(), 1, true));

		JLabel lblNewLabel = new JLabel("Color Theme");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(240, 211, 178, 16);
		centerPanel.add(lblNewLabel);

		buttonPane = new GradientPanel(GradientPanel.DIAGONAL_RIGHT);
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);


		okButton = new GradientButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionOk();
			}
		});
		buttonPane.add(okButton);
		cancelButton = new GradientButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionCancel();
			}
		});
		buttonPane.add(cancelButton);
		buttonPane.setBackground(GradientColors.getStart());
		buttonPane.setForeground(GradientColors.getEnd());

	}

	protected void actionCancel() {
		dispose();

	}

	protected void actionOk() {
		dispose();

	}

	protected void actionFont() {
		FontDialog dialog = new FontDialog();
		dialog.setLocationRelativeTo(this);
		Font font = dialog.showDialog(textLabel.getFont());
		if( font != null ) {
			textLabel.setFont(font);

			UIDefaults defaults = javax.swing.UIManager.getDefaults();
			for(Object key : defaults.keySet()) {
				Object value = defaults.get(key);
				if (value instanceof Font) {
					defaults.put(key, font);				
				}
			}		
		}
	}

	protected void actionEndColor() {
		Color c = JColorChooser.showDialog(this, "Color 2", contentPanel.getForeground(), true);
		if( c != null ) {
			contentPanel.setForeground(c);	
			buttonPane.setForeground(c);
			okButton.setForeground(c);
			cancelButton.setForeground(c);
		};		

	}

	protected void actionStartColor() {
		Color c = JColorChooser.showDialog(this, "Color 1", contentPanel.getBackground(), true);
		if( c != null ) {
			contentPanel.setBackground(c);
			buttonPane.setBackground(c);
			okButton.setBackground(c);
			cancelButton.setBackground(c);
		}

	}
}
