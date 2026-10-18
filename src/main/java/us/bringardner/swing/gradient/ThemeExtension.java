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

import java.util.Map;

import javax.swing.JComponent;

/**
 * An application's own part of a theme, with a page for it in the theme designer: a script
 * editor's syntax colors, say. Register one with {@link ThemeExtensions#register} when the
 * application starts, and the appearance settings' New theme... and Edit theme... show its page.
 * <p>
 * Its settings are kept in each theme file as {@code <id>.<key>=<value>} ({@link Theme#getExtras()}).
 * A theme that has none of its own, such as a built-in one, gets {@link #defaults(Theme)}; a new
 * theme starts with those of the theme it is made from, and can then be changed.
 * {@link Theme#values(ThemeExtension)} gives a theme's settings, defaults filled in.
 */
public interface ThemeExtension {

	/**
	 * The prefix of its keys in a theme file, such as {@code fsh-ide.editor}: letters, digits, dots
	 * and dashes, unique to the application.
	 */
	String getId();

	/** The title of its page in the theme designer, such as "Script editor". */
	String getTitle();

	/**
	 * Its settings for a theme that has none of its own (keys without the prefix): for a built-in
	 * theme, say, or one made before the application was. They may depend on the theme, such as
	 * dark colors for a dark one.
	 */
	Map<String, String> defaults(Theme theme);

	/** A new page for the theme designer. */
	Page createPage();

	/** The page in the theme designer where the user changes the settings. */
	interface Page {

		/** What the designer shows. */
		JComponent getComponent();

		/**
		 * Shows a theme's settings.
		 *
		 * @param values the settings (keys without the prefix), defaults filled in
		 * @param theme the theme being designed, for anything else the page wants to know about it
		 */
		void setValues(Map<String, String> values, Theme theme);

		/** The settings as the user has them now (keys without the prefix). */
		Map<String, String> getValues();

		/** Called whenever the user changes a setting. */
		void setChangeListener(Runnable listener);
	}
}
