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
package us.bringardner.swing.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.Timer;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import us.bringardner.swing.datetime.test.RunOnEdt;
import us.bringardner.swing.dialog.FontDialog;

/** The font chooser opens on the font given, and gives back what was chosen. */
@ExtendWith(RunOnEdt.class)
public class TestFontDialog {

	/** The chooser, with OK and Cancel pressed by the test once it is showing. */
	static class Chooser extends FontDialog {
		private static final long serialVersionUID = 1L;

		Font choose(Font current, boolean ok) {
			Timer t = new Timer(200, e->{
				if( ok ) {
					actionOk();
				} else {
					canceled = true;
					dispose();
				}
			});
			t.setRepeats(false);
			t.start();
			return showDialog(current);
		}
	}

	@BeforeEach
	public void display() {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
	}

	@Test
	public void opensOnTheFontGivenByFamily() {
		String family = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0];
		Font f = new Chooser().choose(new Font(family, Font.BOLD, 17), true);
		assertEquals(family, f.getFamily());
		assertEquals(Font.BOLD, f.getStyle());
		assertEquals(17, f.getSize());
	}

	@Test
	public void aFontNotInTheListKeepsItsFamily() {
		// like macOS's hidden system font, which isn't among the families
		Font hidden = new Font("No Such Family", Font.PLAIN, 13);
		Font f = new Chooser().choose(hidden, true);
		assertEquals(hidden.getFamily(), f.getFamily());
		assertEquals(13, f.getSize());
	}

	@Test
	public void cancelGivesNothing() {
		assertNull(new Chooser().choose(new Font(Font.DIALOG, Font.PLAIN, 12), false));
	}
}
