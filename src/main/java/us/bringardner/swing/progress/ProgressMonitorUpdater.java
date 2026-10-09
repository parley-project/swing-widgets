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
package us.bringardner.swing.progress;

import java.util.Objects;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * Lets any thread update a Swing ProgressMonitor. A ProgressMonitor must only be changed
 * on the event dispatch thread, but the work it reports on usually runs on another one;
 * the changes here are passed to the event dispatch thread.
 * <p>
 * A library's own progress interface with the same methods can be implemented by
 * extending this class.
 */
public class ProgressMonitorUpdater {

	private final ProgressMonitor monitor;

	public ProgressMonitorUpdater(ProgressMonitor monitor) {
		this.monitor = Objects.requireNonNull(monitor, "monitor");
	}

	/** @return the monitor being updated */
	public ProgressMonitor getMonitor() {
		return monitor;
	}

	/** The value that means done. */
	public void setMaximum(int max) {
		SwingUtilities.invokeLater(()->monitor.setMaximum(max));
	}

	public int getMaximum() {
		return monitor.getMaximum();
	}

	public void setProgress(int value) {
		SwingUtilities.invokeLater(()->monitor.setProgress(value));
	}

	/** True if the user pressed Cancel; the work should end early. */
	public boolean isCanceled() {
		return monitor.isCanceled();
	}
}
