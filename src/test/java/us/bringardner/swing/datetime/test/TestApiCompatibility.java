package us.bringardner.swing.datetime.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

import us.bringardner.swing.datetime.DateTimeCombo;

/**
 * BJL-53: public API that bjl_core 1.0.0 had, 1.1.0 removed and other BJL projects still use.
 * Moved here from parley-core with the Swing components.
 */
@SuppressWarnings("deprecation")
public class TestApiCompatibility {

	@Test
	public void dateTimeComboSetdate() {
		DateTimeCombo combo = new DateTimeCombo();
		Date d = new Date(1_700_000_000_000L);
		combo.setdate(d);
		assertEquals(d, combo.getDate());
	}
}
