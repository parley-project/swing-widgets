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
 * The colors the gradient widgets (GradientButton, the dialogs, the scroll bar) are drawn with.
 * The defaults are a gold gradient. An application can set its own before it creates any
 * widgets; widgets read the colors when they are created.
 */
public final class GradientColors {

	public static final Color DEFAULT_START = new Color(242, 206, 113);
	public static final Color DEFAULT_END   = new Color(150, 123, 40);
	public static final Color TRANSPARENT   = new Color(255, 255, 255, 0);

	private static volatile Color start = DEFAULT_START;
	private static volatile Color end = DEFAULT_END;

	private GradientColors() {
	}

	/** @return the light color a gradient starts with */
	public static Color getStart() {
		return start;
	}

	public static void setStart(Color color) {
		start = Objects.requireNonNull(color, "color");
	}

	/** @return the dark color a gradient ends with */
	public static Color getEnd() {
		return end;
	}

	public static void setEnd(Color color) {
		end = Objects.requireNonNull(color, "color");
	}
}
