package us.bringardner.swing.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;

public class TableHeaderUI extends BasicTableHeaderUI {



	/**
	 * Returns a new instance of {@code BasicTableHeaderUI}.
	 *
	 * @param h a component.
	 * @return a new instance of {@code BasicTableHeaderUI}
	 */
	public static ComponentUI createUI(JComponent h) {
		return new TableHeaderUI();
	}


	public TableHeaderUI() {
		super();
	}
	
	class MyComp {
		JLabel iconLabel = new JLabel();
		JLabel textLabel = new JLabel();
		JPanel panel = new JPanel();
		
		MyComp() {
			textLabel.setOpaque(true);
			iconLabel.setOpaque(true);
			textLabel.setVisible(true);
			iconLabel.setVisible(true);
			
			panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,2));
			panel.add(textLabel);
			panel.add(iconLabel);
			panel.setBackground(Color.ORANGE);
			panel.setSize(100, 20);
			
		}
	}
	Map<Integer,MyComp> map = new HashMap<>();
	
	@Override
	public void paint(Graphics g, JComponent c) {

		if (header.getColumnModel().getColumnCount() <= 0) {
			return;
		}
		
		for(int idx = 0,sz=header.getColumnModel().getColumnCount();idx < sz; idx++ ) {
			MyComp ret = map.get(idx);
			if( ret == null ) {
				ret = new MyComp();
				map.put(idx, ret);
			}
			ret.textLabel.setText(""+idx);
			ret.panel.setLocation(idx*100+20, 10);
			ret.panel.paint(g);
		}
		//System.out.println("c="+c.getClass());
		
	}

}
