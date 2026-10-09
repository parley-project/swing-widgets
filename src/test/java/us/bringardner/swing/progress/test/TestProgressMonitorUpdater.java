package us.bringardner.swing.progress.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import us.bringardner.swing.progress.ProgressMonitorUpdater;

/** Tests for ProgressMonitorUpdater that do not need a display. */
public class TestProgressMonitorUpdater {

	@Test
	public void changesReachTheMonitorOnTheEventThread() throws Exception {
		ProgressMonitor monitor = new ProgressMonitor(null, "Working", null, 0, 10);
		ProgressMonitorUpdater updater = new ProgressMonitorUpdater(monitor);
		assertSame(monitor, updater.getMonitor());

		// called off the event thread, as a worker would
		Thread worker = new Thread(()->updater.setMaximum(250));
		worker.start();
		worker.join();
		SwingUtilities.invokeAndWait(()->{});

		assertEquals(250, monitor.getMaximum());
		assertEquals(250, updater.getMaximum());
		assertFalse(updater.isCanceled());

		// reaching the maximum closes the monitor; no dialog was shown, so nothing to cancel
		updater.setProgress(250);
		SwingUtilities.invokeAndWait(()->{});
		assertFalse(updater.isCanceled());
	}

	@Test
	public void aMonitorIsRequired() {
		assertThrows(NullPointerException.class, () -> new ProgressMonitorUpdater(null));
	}
}
