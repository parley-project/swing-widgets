package us.bringardner.swing.ui;


import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;



public class ScrollBarUI extends BasicScrollBarUI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	 /**
     * Creates the UI.
     * @param c the component
     * @return the UI
     */
    public static ComponentUI createUI(JComponent c)    {
        return new ScrollBarUI();
    }
    
	/**
     * This method should be used for drawing a borders over a filled rectangle.
     * Draws vertical line, using the current color, between the points {@code
     * (x, y1)} and {@code (x, y2)} in graphics context's coordinate system.
     * Note: it use {@code Graphics.fillRect()} internally.
     *
     * @param g  Graphics to draw the line to.
     * @param x  the <i>x</i> coordinate.
     * @param y1 the first point's <i>y</i> coordinate.
     * @param y2 the second point's <i>y</i> coordinate.
     */
    public static void drawVLine(Graphics g, int x, int y1, int y2) {
        if (y2 < y1) {
            final int temp = y2;
            y2 = y1;
            y1 = temp;
        }
        g.fillRect(x, y1, 1, y2 - y1 + 1);
    }

    /**
     * This method should be used for drawing a borders over a filled rectangle.
     * Draws horizontal line, using the current color, between the points {@code
     * (x1, y)} and {@code (x2, y)} in graphics context's coordinate system.
     * Note: it use {@code Graphics.fillRect()} internally.
     *
     * @param g  Graphics to draw the line to.
     * @param x1 the first point's <i>x</i> coordinate.
     * @param x2 the second point's <i>x</i> coordinate.
     * @param y  the <i>y</i> coordinate.
     */
    public static void drawHLine(Graphics g, int x1, int x2, int y) {
        if (x2 < x1) {
            final int temp = x2;
            x2 = x1;
            x1 = temp;
        }
        g.fillRect(x1, y, x2 - x1 + 1, 1);
    }

    /**
     * This method should be used for drawing a borders over a filled rectangle.
     * Draws the outline of the specified rectangle. The left and right edges of
     * the rectangle are at {@code x} and {@code x + w}. The top and bottom
     * edges are at {@code y} and {@code y + h}. The rectangle is drawn using
     * the graphics context's current color. Note: it use {@code
     * Graphics.fillRect()} internally.
     *
     * @param g Graphics to draw the rectangle to.
     * @param x the <i>x</i> coordinate of the rectangle to be drawn.
     * @param y the <i>y</i> coordinate of the rectangle to be drawn.
     * @param w the w of the rectangle to be drawn.
     * @param h the h of the rectangle to be drawn.
     * @see SwingUtilities2#drawVLine(java.awt.Graphics, int, int, int)
     * @see SwingUtilities2#drawHLine(java.awt.Graphics, int, int, int)
     */
    public static void drawRect(Graphics g, int x, int y, int w, int h) {
        if (w < 0 || h < 0) {
            return;
        }

        if (h == 0 || w == 0) {
            g.fillRect(x, y, w + 1, h + 1);
        } else {
            g.fillRect(x, y, w, 1);
            g.fillRect(x + w, y, 1, h);
            g.fillRect(x + 1, y + h, w, 1);
            g.fillRect(x, y + 1, 1, h);
        }
        
    }

    boolean isHorizontal = false;
    
    @Override
    protected void layoutHScrollbar(JScrollBar sb) {
    	isHorizontal = true;
    	super.layoutHScrollbar(sb);
    }
    
	
    /**
     * Paints the thumb.
     * @param g the graphics
     * @param c the component
     * @param thumbBounds the thumb bounds
     */
    @Override
    public void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds){
    	
        if(thumbBounds.isEmpty() || !scrollbar.isEnabled())     {
            return;
        }

        int w = thumbBounds.width;
        int h = thumbBounds.height;

        g.translate(thumbBounds.x, thumbBounds.y);

        g.setColor(thumbDarkShadowColor);
        drawRect(g, 0, 0, w - 1, h - 1);
        
        Color sc = trackColor;
        Color ec = thumbColor;
        
        g.setColor(thumbColor);
        GradientPaint paint = new GradientPaint( 0, h / 2, sc, w, h / 2, ec, true );
        if( !isHorizontal) {
        	ec = trackColor;
            sc = thumbColor;
            
        	 paint = new GradientPaint( w / 2, 0, sc, w / 2, h, ec, true );	
        }
		Paint oldPaint = ((Graphics2D)g).getPaint();

		// set the paint to use for this operation
		((Graphics2D)g).setPaint( paint );

		// fill the background using the paint
		g.fillRect( 0, 0, w-1, h-1 );

		// restore the original paint
		((Graphics2D)g).setPaint( oldPaint );

        //g.fillRect(0, 0, w - 1, h - 1);

        g.setColor(thumbHighlightColor);
        drawVLine(g, 1, 1, h - 2);
        drawHLine(g, 2, w - 3, 1);

        g.setColor(thumbLightShadowColor);
        drawHLine(g, 2, w - 2, h - 2);
        drawVLine(g, w - 2, 1, h - 3);

        g.translate(-thumbBounds.x, -thumbBounds.y);
    }


}
