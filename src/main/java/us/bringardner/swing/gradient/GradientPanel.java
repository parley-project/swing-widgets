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
 * ~version~V000.01.17-V000.01.05-V000.01.04-V000.01.01-V000.01.00-
 */
package us.bringardner.swing.gradient;




import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Created by IntelliJ IDEA.
 */

public class GradientPanel extends JPanel  {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new GradientPanel(DIAGONAL_RIGHT));

		frame.setSize(700, 400);
		frame.setLocationRelativeTo(null);

		//frame.pack();
		SwingUtilities.invokeLater(()->frame.setVisible(true));

	}

	// ------------------------------ FIELDS ------------------------------

	public final static int HORIZONTAL = 0;
	public final static int VERTICAL = 1;
	public final static int DIAGONAL_LEFT = 2;
	public final static int DIAGONAL_RIGHT = 3;

	private int direction = HORIZONTAL;
	private boolean cyclic;
	private int maxLength;

	// --------------------------- CONSTRUCTORS ---------------------------

	public GradientPanel( int direction ){
		super( new BorderLayout() );
		setOpaque( false );
		this.direction = direction;		        
	}

	public int getDirection(){
		return direction;
	}

	public void setDirection( int direction ) {
		this.direction = direction;
	}

	public boolean isCyclic() {
		return cyclic;
	}

	public void setCyclic( boolean cyclic ) {
		this.cyclic = cyclic;
	}

	public void setMaxLength( int maxLength ) {
		this.maxLength = maxLength;
	}


	public void paintComponent( Graphics g ) {
		if( isOpaque() )
		{
			super.paintComponent( g );
			return;
		}

		int width = getWidth();
		int height = getHeight();

		// Create the gradient paint
		GradientPaint paint = null;

		Color sc = getForeground();
		Color ec = getBackground();

		switch( direction )
		{
		case HORIZONTAL :
		{
			paint = new GradientPaint( 0, height / 2, sc, width, height / 2, ec, cyclic );
			break;
		}
		case VERTICAL :
		{
			paint = new GradientPaint( width / 2, 0, sc, width / 2, maxLength > 0 ? maxLength : height, ec, cyclic );
			break;
		}
		case DIAGONAL_LEFT :
		{
			paint = new GradientPaint( 0, 0, sc, width, height, ec, cyclic );
			break;
		}
		case DIAGONAL_RIGHT :
		{
			paint = new GradientPaint( width, 0, sc, 0, height, ec, cyclic );
			break;
		}
		}

		if( paint == null )
		{
			throw new RuntimeException( "Invalid direction specified in GradientPanel" );
		}

		// we need to cast to Graphics2D for this operation
		Graphics2D g2d = ( Graphics2D )g;

		// save the old paint
		Paint oldPaint = g2d.getPaint();

		// set the paint to use for this operation
		g2d.setPaint( paint );

		// fill the background using the paint
		g2d.fillRect( 0, 0, width, height );

		// restore the original paint
		g2d.setPaint( oldPaint );

		super.paintComponent( g );
	}
}