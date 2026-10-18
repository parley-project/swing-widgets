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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The applications' theme extensions ({@link ThemeExtension}), registered when an application
 * starts: the theme designer shows a page for each.
 */
public final class ThemeExtensions {

	private static final List<ThemeExtension> EXTENSIONS = new CopyOnWriteArrayList<>();

	private ThemeExtensions() {
	}

	/**
	 * Adds an extension; one with the same id is replaced.
	 *
	 * @param extension the extension
	 */
	public static void register(ThemeExtension extension) {
		Objects.requireNonNull(extension, "extension");
		String id = extension.getId();
		if( id == null || !id.matches("[A-Za-z0-9][A-Za-z0-9.-]*") ) {
			throw new IllegalArgumentException("\"" + id + "\" can't be an extension's id: letters, digits, dots and dashes");
		}
		unregister(id);
		EXTENSIONS.add(extension);
	}

	/** Removes the extension with this id, if there is one. */
	public static void unregister(String id) {
		EXTENSIONS.removeIf(e->e.getId().equals(id));
	}

	/** The registered extensions, in the order they were registered. */
	public static List<ThemeExtension> all() {
		return new ArrayList<>(EXTENSIONS);
	}
}
