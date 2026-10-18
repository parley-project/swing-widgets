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

import java.awt.Color;
import java.util.Objects;

/**
 * The two colors of the gradient: those of the current {@link Theme}. Kept for the code that used
 * them before there were themes; a theme has more colors (text, fields, selections and so on), so
 * new code uses {@link Theme#current()}. Setting a color here makes a custom theme from the current
 * one with that gradient.
 */
public final class GradientColors {

	public static final Color DEFAULT_START = new Color(242, 206, 113);
	public static final Color DEFAULT_END   = new Color(150, 123, 40);
	public static final Color TRANSPARENT   = new Color(255, 255, 255, 0);

	private GradientColors() {
	}

	/** @return the light color a gradient starts with */
	public static Color getStart() {
		return Theme.current().getStart();
	}

	public static void setStart(Color color) {
		Objects.requireNonNull(color, "color");
		Theme t = Theme.current();
		if( !color.equals(t.getStart()) ) {
			Theme.setCurrent(t.withGradient(color, t.getEnd()));
		}
	}

	/** @return the dark color a gradient ends with */
	public static Color getEnd() {
		return Theme.current().getEnd();
	}

	public static void setEnd(Color color) {
		Objects.requireNonNull(color, "color");
		Theme t = Theme.current();
		if( !color.equals(t.getEnd()) ) {
			Theme.setCurrent(t.withGradient(t.getStart(), color));
		}
	}
}
