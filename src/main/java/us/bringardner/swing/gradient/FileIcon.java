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
 * A file, to go with {@link FolderIcon}: a page with its top corner folded, in the theme's field
 * color, outlined in its accent, with a few lines of "text" (a dark theme's: a lighter outline). An
 * unreadable file is crossed out in red, as an unreadable folder is. It takes the colors as it is
 * painted, so a theme chosen later shows at once.
 */
public class FileIcon implements Icon {

	/** The colors of a theme's files: the page, its outline, its lines of text. */
	public static Color[] colors(Theme t) {
		Color page = t.getField();
		Color edge = t.isDark() ? FolderIcon.mix(t.getAccent(), Color.WHITE, 0.3) : t.getAccent();
		return new Color[] {page, edge, FolderIcon.mix(page, edge, 0.55)};
	}

	private final int size;
	private final boolean readable;

	/** @param size its width and height in pixels */
	public FileIcon(int size) {
		this(size, true);
	}

	/**
	 * @param size its width and height in pixels
	 * @param readable false to cross it out
	 */
	public FileIcon(int size, boolean readable) {
		this.size = size;
		this.readable = readable;
	}

	public boolean isReadable() {
		return readable;
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
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.translate(x, y);
			// drawn on a 16-unit square, as the folders are
			double s = size / 16.0;
			g.scale(s, s);
			g.setStroke(new BasicStroke(0.9f));

			Path2D page = new Path2D.Float();
			page.moveTo(3, 1.5);
			page.lineTo(10, 1.5);
			page.lineTo(13.5, 5);
			page.lineTo(13.5, 14.5);
			page.lineTo(3, 14.5);
			page.closePath();
			g.setColor(col[0]);
			g.fill(page);
			g.setColor(col[1]);
			g.draw(page);

			// the folded corner
			Path2D fold = new Path2D.Float();
			fold.moveTo(10, 1.5);
			fold.lineTo(10, 5);
			fold.lineTo(13.5, 5);
			g.draw(fold);

			// lines of text
			g.setColor(col[2]);
			for(float ty : new float[] {7.5f, 9.5f, 11.5f}) {
				g.draw(new Line2D.Float(5, ty, 11.5f, ty));
			}
			g.draw(new Line2D.Float(5, 5.5f, 8, 5.5f));

			if( !readable ) {
				g.setColor(new Color(0xD02020));
				g.setStroke(new BasicStroke(2f));
				g.draw(new Line2D.Float(2.5f, 9.5f, 14f, 9.5f));
			}
		} finally {
			g.dispose();
		}
	}
}
