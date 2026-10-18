/**
 *	Copyright 2026 Tony Bringardner
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *	the specific language governing permissions and limitations under the License.
 */
package us.bringardner.swing.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;

import us.bringardner.swing.gradient.Theme;

/**
 * Buttons in the theme's colors, as the JavaFX gradient look draws them: a rounded gradient from
 * the theme's dark color to its light one, outlined in the dark color, with the theme's button
 * text; lighter under the mouse, reversed while pressed, and half faded when disabled. The default
 * button (Return) is bold. The colors are the theme in effect when painted, so a theme chosen
 * later shows at once.
 * <p>
 * The gradient look ({@code GradientLook.install()}) makes this the UI of every {@link JButton} with
 * text. A button with only an icon (a toolbar button), one the look and feel makes for its own
 * controls, or one with an Aqua button type ({@code JButton.buttonType}) keeps the look and feel's
 * own UI, as does one whose {@link #THEMED} client property is {@code Boolean.FALSE} when its UI is
 * made ({@link #isThemed(JComponent)}).
 */
public class GradientButtonUI extends BasicButtonUI {

	/** A client property: {@code Boolean.FALSE} on a button keeps the look and feel's own UI. */
	public static final String THEMED = "Gradient.themed";

	private static final int RADIUS = 10;

	/**
	 * The UI for a button: this one for a button with text, the look and feel's own for the others.
	 *
	 * @param c the button
	 * @return the UI
	 */
	public static ComponentUI createUI(JComponent c) {
		watch(c);
		if( !isThemed(c) ) {
			ComponentUI own = lookUI(c);
			if( own != null ) {
				return own;
			}
		}
		return new GradientButtonUI();
	}

	/**
	 * True if a button gets the theme's colors: a button with text (or nothing yet), not one with
	 * only an icon, not one the look and feel makes for its own controls (a combo box's arrow, say),
	 * not one with an Aqua button type, and not one whose {@link #THEMED} property is false.
	 */
	public static boolean isThemed(JComponent c) {
		if( !(c instanceof AbstractButton) ) {
			return false;
		}
		AbstractButton b = (AbstractButton) c;
		if( Boolean.FALSE.equals(b.getClientProperty(THEMED)) || b.getClientProperty("JButton.buttonType") != null ) {
			return false;
		}
		String name = b.getClass().getName();
		if( name.startsWith("javax.swing.plaf.") || name.startsWith("com.apple.") || name.startsWith("sun.") ) {
			return false;
		}
		boolean text = b.getText() != null && !b.getText().isEmpty();
		return text || b.getIcon() == null;
	}

	/** The client property that marks a button already watched by {@link #watch(JComponent)}. */
	private static final String WATCHED = "Gradient.themedWatch";

	/**
	 * Makes the UI again when a button's text or icon changes it from themed to not, or back: a
	 * button is often made with no text and given an icon afterwards (new JButton(""), setIcon).
	 */
	private static void watch(JComponent c) {
		if( c.getClientProperty(WATCHED) != null ) {
			return;
		}
		c.putClientProperty(WATCHED, Boolean.TRUE);
		c.addPropertyChangeListener(e->{
			String name = e.getPropertyName();
			if( "text".equals(name) || AbstractButton.ICON_CHANGED_PROPERTY.equals(name) || THEMED.equals(name) ) {
				boolean themed = c.getUI() instanceof GradientButtonUI;
				if( themed != isThemed(c) && UIManager.get("ButtonUI") != null
						&& GradientButtonUI.class.getName().equals(String.valueOf(UIManager.get("ButtonUI"))) ) {
					c.updateUI();
				}
			}
		});
	}

	/** The look and feel's own UI for a button, or null if it has none. */
	private static ComponentUI lookUI(JComponent c) {
		try {
			// the look and feel's table: the gradient look puts this class in the developer's
			return UIManager.getLookAndFeelDefaults().getUI(c);
		} catch (RuntimeException e) {
			return null;
		}
	}

	@Override
	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		b.setOpaque(false);
		b.setRolloverEnabled(true);
		Border border = b.getBorder();
		if( border == null || border instanceof UIResource ) {
			b.setBorder(new BorderUIResource.EmptyBorderUIResource(4, 14, 4, 14));
		}
	}

	@Override
	public void update(Graphics g, JComponent c) {
		paint(g, c);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		AbstractButton b = (AbstractButton) c;
		ButtonModel m = b.getModel();
		Theme t = Theme.current();
		Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if( !b.isEnabled() ) {
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			}
			if( b.isContentAreaFilled() ) {
				paintBackground(g2, b, m, t);
			}
			if( b.isFocusPainted() && b.hasFocus() ) {
				g2.setColor(t.getAccent());
				g2.setStroke(new BasicStroke(1.5f));
				g2.draw(new RoundRectangle2D.Float(2.5f, 2.5f, b.getWidth() - 5, b.getHeight() - 5, RADIUS - 3,
						RADIUS - 3));
			}
			// the text and icon, as the basic UI lays them out, in the button's font (bold for the default)
			Font f = b.getFont();
			if( isDefault(b) && f != null ) {
				g2.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
			}
			super.paint(g2, c);
		} finally {
			g2.dispose();
		}
	}

	private static void paintBackground(Graphics2D g2, AbstractButton b, ButtonModel m, Theme t) {
		Color start = t.getStart();
		Color end = t.getEnd();
		boolean pressed = m.isArmed() && m.isPressed();
		Color edge = pressed ? start : end;
		Color from;
		Color to;
		if( pressed ) {
			from = end;
			to = mix(end, Color.white, 0.2);
		} else if( m.isRollover() && b.isEnabled() ) {
			from = mix(end, Color.white, 0.25);
			to = start;
		} else {
			from = mix(end, Color.white, 0.1);
			to = mix(start, Color.black, 0.1);
		}
		float w = b.getWidth();
		float h = b.getHeight();
		g2.setColor(edge);
		g2.fill(new RoundRectangle2D.Float(0, 0, w, h, RADIUS, RADIUS));
		g2.setPaint(new GradientPaint(0, 0, from, w, 0, to));
		g2.fill(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, RADIUS - 2, RADIUS - 2));
	}

	@Override
	protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
		// the theme's button text, enabled or not: disabled is faded by the composite instead
		Theme t = Theme.current();
		g.setColor(t.getButtonText());
		int mnemonic = b.getDisplayedMnemonicIndex();
		java.awt.FontMetrics fm = b.getFontMetrics(g.getFont());
		javax.swing.plaf.basic.BasicGraphicsUtils.drawStringUnderlineCharAt(b, (Graphics2D) g, text, mnemonic,
				textRect.x, textRect.y + fm.getAscent());
	}

	@Override
	protected void paintButtonPressed(Graphics g, AbstractButton b) {
		// the background shows pressed
	}

	@Override
	protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect,
			Rectangle iconRect) {
		// painted with the background
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension d = super.getPreferredSize(c);
		AbstractButton b = (AbstractButton) c;
		// room for bold text, whether or not it's the default button: a button becomes the default
		// after it's laid out, and nothing lays it out again
		Font f = b.getFont();
		String text = b.getText();
		if( d != null && f != null && text != null && !text.isEmpty() ) {
			int plain = b.getFontMetrics(f).stringWidth(text);
			int bold = b.getFontMetrics(f.deriveFont(f.getStyle() | Font.BOLD)).stringWidth(text);
			d = new Dimension(d.width + Math.max(0, bold - plain), d.height);
		}
		if( d != null ) {
			// no shorter than a text field, as the look and feel's buttons are
			Insets in = b.getInsets();
			int min = b.getFontMetrics(f == null ? b.getFont() : f).getHeight() + in.top + in.bottom;
			d.height = Math.max(d.height, min);
		}
		return d;
	}

	private static boolean isDefault(AbstractButton b) {
		return b instanceof JButton && ((JButton) b).isDefaultButton();
	}

	/** Between two colors: t = 0 is a, 1 is b. */
	static Color mix(Color a, Color b, double t) {
		return new Color((int) Math.round(a.getRed() * (1 - t) + b.getRed() * t),
				(int) Math.round(a.getGreen() * (1 - t) + b.getGreen() * t),
				(int) Math.round(a.getBlue() * (1 - t) + b.getBlue() * t));
	}
}
