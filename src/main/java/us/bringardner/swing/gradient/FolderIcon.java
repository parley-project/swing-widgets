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
package us.bringardner.swing.gradient;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import javax.swing.Icon;

/**
 * A folder in the current theme's colors: the theme's light color with an outline in its accent
 * (a dark theme's is the accent, darkened, with a lighter outline), like a manila folder. It takes
 * the colors as it is painted, so a theme chosen later shows at once.
 */
public class FolderIcon implements Icon {

	/** The kinds of folder. */
	public enum Kind {
		/** A folder. */
		CLOSED,
		/** An open folder (the one shown, in a tree). */
		OPEN,
		/** A folder that can't be read: crossed out in red. */
		NOT_READABLE
	}

	/** The colors of a theme's folders: its fill, its tab, its outline. */
	public static Color[] colors(Theme t) {
		if( t.isDark() ) {
			return new Color[] {mix(t.getAccent(), t.getStart(), 0.35), t.getAccent(), mix(t.getAccent(), Color.WHITE, 0.3)};
		}
		return new Color[] {t.getStart(), mix(t.getStart(), t.getAccent(), 0.3), t.getAccent()};
	}

	private final int size;
	private final Kind kind;

	/**
	 * @param size its width and height in pixels
	 * @param kind which folder
	 */
	public FolderIcon(int size, Kind kind) {
		this.size = size;
		this.kind = kind;
	}

	public Kind getKind() {
		return kind;
	}

	@Override
	public int getIconWidth() {
		return size;
	}

	@Override
	public int getIconHeight() {
		return size;
	}

	@Override
	public void paintIcon(Component c, Graphics graphics, int x, int y) {
		Color[] col = colors(Theme.current());
		Color fill = col[0];
		Color tab = col[1];
		Color edge = col[2];
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.translate(x, y);
			// drawn on a 16-unit square
			double s = size / 16.0;
			g.scale(s, s);
			g.setStroke(new BasicStroke(0.9f));

			Path2D tabShape = new Path2D.Float();
			tabShape.moveTo(1, 4);
			tabShape.lineTo(1, 2.5);
			tabShape.lineTo(6, 2.5);
			tabShape.lineTo(7.5, 4);
			tabShape.closePath();
			g.setColor(tab);
			g.fill(tabShape);
			g.setColor(edge);
			g.draw(tabShape);

			Path2D body = new Path2D.Float();
			body.moveTo(1, 4);
			body.lineTo(15, 4);
			body.lineTo(15, 14);
			body.lineTo(1, 14);
			body.closePath();
			if( kind == Kind.OPEN ) {
				// the back, darker, and the front flap open toward the viewer
				g.setColor(tab);
				g.fill(body);
				g.setColor(edge);
				g.draw(body);
				Path2D flap = new Path2D.Float();
				flap.moveTo(3, 7);
				flap.lineTo(15.5, 7);
				flap.lineTo(13.5, 14);
				flap.lineTo(1, 14);
				flap.closePath();
				g.setColor(fill);
				g.fill(flap);
				g.setColor(edge);
				g.draw(flap);
			} else {
				g.setColor(fill);
				g.fill(body);
				g.setColor(edge);
				g.draw(body);
				g.setColor(mix(fill, Color.WHITE, 0.35));
				g.draw(new Line2D.Float(1.6f, 5.2f, 14.4f, 5.2f));
			}
			if( kind == Kind.NOT_READABLE ) {
				g.setColor(new Color(0xD02020));
				g.setStroke(new BasicStroke(2f));
				g.draw(new Line2D.Float(2.5f, 9.5f, 13.5f, 9.5f));
			}
		} finally {
			g.dispose();
		}
	}

	static Color mix(Color a, Color b, double t) {
		return new Color((int) Math.round(a.getRed() * (1 - t) + b.getRed() * t),
				(int) Math.round(a.getGreen() * (1 - t) + b.getGreen() * t),
				(int) Math.round(a.getBlue() * (1 - t) + b.getBlue() * t));
	}
}
