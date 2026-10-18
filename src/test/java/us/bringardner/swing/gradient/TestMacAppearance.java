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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** MacAppearance does nothing, quietly, where it can't do anything. */
public class TestMacAppearance {

	@Test
	public void nothingOffMacOs() {
		Assumptions.assumeFalse(MacAppearance.isMac());
		assertFalse(MacAppearance.follow(Theme.DARK));
		assertFalse(MacAppearance.follow(Theme.GOLD));
		assertFalse(MacAppearance.set(null));
	}

	@Test
	public void theDarkAppearance() {
		assertEquals("NSAppearanceNameDarkAqua", MacAppearance.DARK);
	}
}
