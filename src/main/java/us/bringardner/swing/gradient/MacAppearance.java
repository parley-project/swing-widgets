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

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

/**
 * Light or dark macOS windows (title bars, native controls) to go with the theme, at any time.
 * <p>
 * AWT reads its {@code apple.awt.application.appearance} property only when it starts, so a theme
 * changed later left the title bars as they were until the application was restarted. This sets
 * the application's appearance ({@code NSApp.appearance}) directly, through the Objective-C runtime
 * and JNA: dark for a dark theme, the system's own for a light one. It works the same for Swing and
 * JavaFX windows, which share the one NSApplication.
 * <p>
 * JNA is an optional dependency: an application without it on the class path, or on another
 * platform, simply keeps the appearance it started with ({@link #follow} returns false).
 */
public final class MacAppearance {

	/** The dark appearance's name (NSAppearanceNameDarkAqua). */
	public static final String DARK = "NSAppearanceNameDarkAqua";

	private MacAppearance() {
	}

	static boolean isMac() {
		return System.getProperty("os.name", "").toLowerCase().startsWith("mac");
	}

	/**
	 * Dark windows for a dark theme, the system's appearance for a light one.
	 *
	 * @param theme the theme now in effect
	 * @return true if the appearance was set, false if it can't be here (not macOS, no JNA, or
	 *         neither AWT nor JavaFX has started yet)
	 */
	public static boolean follow(Theme theme) {
		return set(theme.isDark() ? DARK : null);
	}

	/**
	 * Sets the application's appearance.
	 *
	 * @param name an NSAppearanceName such as {@link #DARK}, or null for the system's own
	 * @return true if it was set, false if it can't be here (not macOS, no JNA)
	 */
	public static boolean set(String name) {
		if( !isMac() ) {
			return false;
		}
		try {
			return Objc.setAppearance(name);
		} catch (LinkageError | RuntimeException e) {
			// no JNA, or no Objective-C runtime: the appearance stays as it started
			return false;
		}
	}

	/** The calls into the Objective-C runtime; loaded only when needed, so JNA stays optional. */
	private static final class Objc {

		private static final NativeLibrary OBJC = NativeLibrary.getInstance("objc");
		private static final Function MSG_SEND = OBJC.getFunction("objc_msgSend");
		private static final Function GET_CLASS = OBJC.getFunction("objc_getClass");
		private static final Function SEL = OBJC.getFunction("sel_registerName");

		static boolean setAppearance(String name) {
			// the application AWT or JavaFX made; none yet means neither has started (making one
			// here would be the wrong kind for them), and AWT's start-up property does the job then
			Pointer app = NativeLibrary.getInstance("AppKit").getGlobalVariableAddress("NSApp").getPointer(0);
			if( app == null ) {
				return false;
			}
			Pointer appearance = null;
			if( name != null ) {
				Pointer nsName = send(cls("NSString"), "stringWithUTF8String:", name);
				appearance = send(cls("NSAppearance"), "appearanceNamed:", nsName);
				if( appearance == null ) {
					return false;
				}
			}
			// AppKit only on its own thread: the main thread does it, without waiting for it (AWT's
			// and JavaFX's threads mustn't wait on AppKit's)
			MSG_SEND.invoke(Void.class, new Object[] {app, sel("performSelectorOnMainThread:withObject:waitUntilDone:"),
					sel("setAppearance:"), appearance, Boolean.FALSE});
			return true;
		}

		private static Pointer cls(String name) {
			return (Pointer) GET_CLASS.invoke(Pointer.class, new Object[] {name});
		}

		private static Pointer sel(String name) {
			return (Pointer) SEL.invoke(Pointer.class, new Object[] {name});
		}

		private static Pointer send(Pointer receiver, String selector, Object... args) {
			Object[] all = new Object[args.length + 2];
			all[0] = receiver;
			all[1] = sel(selector);
			System.arraycopy(args, 0, all, 2, args.length);
			return (Pointer) MSG_SEND.invoke(Pointer.class, all);
		}
	}
}
