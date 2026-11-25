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
package us.bringardner.swing.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import us.bringardner.swing.datetime.test.SwingTestUtil;
import us.bringardner.swing.dialog.MessageDialog.Response;

/**
 * showDialogThreadSafe from a thread that isn't the event thread waits for the answer and returns it. (It returned
 * at once with null, so what asked "Overwrite it?" went on whatever the answer was.)
 */
public class TestMessageDialogThreadSafe {

	/** a dialog that is answered when the test says so, with the answer it is told */
	private static final class Answered extends MessageDialog {
		private static final long serialVersionUID = 1L;
		final CountDownLatch shown = new CountDownLatch(1);
		final CountDownLatch answer = new CountDownLatch(1);
		volatile Response response = Response.Yes;
		volatile boolean onEventThread;

		@Override
		public Response showDialog() {
			onEventThread = SwingUtilities.isEventDispatchThread();
			shown.countDown();
			try {
				answer.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return response;
		}
	}

	private ExecutorService asker;

	@BeforeEach
	public void setUp() {
		SwingTestUtil.assumeDisplay();
		asker = Executors.newSingleThreadExecutor();
	}

	@AfterEach
	public void tearDown() {
		if( asker != null ) {
			asker.shutdownNow();
		}
	}

	private Future<Response> ask(Answered d) {
		return asker.submit(d::showDialogThreadSafe);
	}

	@Test
	public void itWaitsForTheAnswerAndReturnsIt() throws Exception {
		for(Response expected : new Response[] {Response.No, Response.Yes}) {
			Answered d = new Answered();
			d.response = expected;
			try {
				Future<Response> f = ask(d);
				assertTrue(d.shown.await(5, TimeUnit.SECONDS), "the dialog is shown");
				assertTrue(d.onEventThread, "on the event thread");
				try {
					f.get(300, TimeUnit.MILLISECONDS);
					throw new AssertionError("it returned before the dialog was answered");
				} catch (TimeoutException e) {
					// waiting
				}
				d.answer.countDown();
				assertEquals(expected, f.get(5, TimeUnit.SECONDS));
			} finally {
				d.answer.countDown();
				d.dispose();
			}
		}
	}

	@Test
	public void anAnswerIsNotKeptForTheNextDialog() throws Exception {
		Answered first = new Answered();
		first.response = Response.Yes;
		first.answer.countDown();
		Answered second = new Answered();
		second.response = Response.No;
		second.answer.countDown();
		try {
			assertEquals(Response.Yes, ask(first).get(5, TimeUnit.SECONDS));
			assertEquals(Response.No, ask(second).get(5, TimeUnit.SECONDS));
		} finally {
			first.dispose();
			second.dispose();
		}
	}

	@Test
	public void aWaitingThreadThatIsInterruptedIsAnsweredNo() throws Exception {
		Answered d = new Answered();
		AtomicBoolean interrupted = new AtomicBoolean();
		try {
			Future<Response> f = asker.submit(()->{
				Response r = d.showDialogThreadSafe();
				interrupted.set(Thread.currentThread().isInterrupted());
				return r;
			});
			assertTrue(d.shown.await(5, TimeUnit.SECONDS));
			asker.shutdownNow();
			assertEquals(Response.No, f.get(5, TimeUnit.SECONDS));
			assertTrue(interrupted.get(), "the interrupt is kept");
			assertFalse(f.isCancelled());
		} finally {
			d.answer.countDown();
			d.dispose();
		}
	}
}
