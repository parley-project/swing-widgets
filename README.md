# swing-widgets

Reusable Swing components: date and time pickers (date, day and time panels, an analog clock,
modal date/time dialogs and a date/time combo box; Swing has no date or time picker of its own)
a "recent items" menu, a way to update a progress monitor from any thread, and a set of
gradient-styled buttons, dialogs, fields and look-and-feel pieces. Each group of components has its own package under
`us.bringardner.swing`.

- **Java 11** or later
- **No dependencies**
- Apache License 2.0

The date and time classes used to be the `us.bringardner.core.swing` package of BjlCore (briefly
`us.bringardner.parley.core.swing` in parley-core). Moving over means adding this dependency and
replacing that package with `us.bringardner.swing.datetime` in imports.

## Why this library exists

Swing leaves some everyday pieces out: it has no date or time picker, no "recent files" menu, and no
safe way to update a progress monitor from a worker thread. Each application ended up writing its
own. swing-widgets is where those pieces live once, for any Swing application.

It's a library of its own, with no dependencies, for two reasons:

- **The non-UI libraries stay free of Swing.** These classes used to be in BjlCore (later
  parley-core), so every Parley protocol library, and every server or command-line tool using one,
  carried Swing along. Now only applications with a Swing UI need it.
- **It isn't tied to Parley.** Any Swing application can use it without pulling in Parley.

[fx-widgets](https://github.com/tony-bringardner/fx-widgets) is the JavaFX counterpart, but not a
copy: JavaFX already has much of what's here (CSS styling, `Alert`, prompt text, `Task` progress),
so it only fills JavaFX's own gaps. The recent list behind both menus (`RecentItems`) is in this
library because it has no UI and no dependencies, so both toolkits can reach it.

**Use swing-widgets** when your application's UI is Swing. Swing runs on any desktop JDK from Java
11, with nothing extra to install.

## Getting started

```xml
<dependency>
    <groupId>us.bringardner</groupId>
    <artifactId>swing-widgets</artifactId>
    <version>1.0.0</version>
</dependency>
```

## What's inside

| Package | Class | Purpose |
|---|---|---|
| `us.bringardner.swing.datetime` | `DatePanel`, `DayPanel`, `TimePanel`, `Clock` | Date and time picker panels. |
| | `DateDialog`, `TimeDialog`, `DateAndTimeDialog` | Modal dialogs built from the panels. |
| | `DateTimeCombo` | A date/time spinner with a button that opens `DateAndTimeDialog`. |
| `us.bringardner.swing.menu` | `RecentItemsMenu` | A "Recent ..." menu of any kind of item, saved with `java.util.prefs.Preferences`. |
| | `RecentItems` | The list behind that menu, without any UI: newest first, a maximum, saved with `Preferences`. fx-widgets' JavaFX menu uses it too, so the two share a list. |
| `us.bringardner.swing.progress` | `ProgressMonitorUpdater` | Lets any thread update a Swing `ProgressMonitor`; the changes are made on the event dispatch thread. |
| `us.bringardner.swing.gradient` | `GradientPanel`, `GradientButton` | A panel painted with a two-color gradient, and a rounded gradient button. |
| | `GradientColors` | The colors the gradient widgets use (a gold gradient by default); set them before creating widgets. |
| `us.bringardner.swing.dialog` | `MessageDialog` | Gradient-styled message, warning, error and input dialogs (`showMessageDialog`, `showErrorDialog`, ...). |
| | `FontDialog`, `SettingsDialog` | Choose a font; a settings dialog with a font chooser. |
| `us.bringardner.swing.field` | `TextFieldPanel`, `PasswordPanel` | A text field with a prompt shown while it's empty; a password field with a show/hide eye. |
| `us.bringardner.swing.ui` | `ScrollBarUI`, `TableHeaderUI` | Gradient look for scroll bars and table headers. |
| | `GlassPane` | A glass pane that blocks input and shows a spinning globe while the application is busy. |

## Date and time pickers

The date and time components work together:

```java
Date picked = new DateAndTimeDialog().showDialog(new Date(), "Start time");

DateTimeCombo combo = new DateTimeCombo(new Date());   // spinner plus "^" button
panel.add(combo);
...
Date value = combo.getDate();
```

`DateDialog`, `TimeDialog` and `DateAndTimeDialog` are modal. `showDialog` returns the chosen
value, or the date you passed in if the user cancels (`isCanceled()` tells you which).
`TimePanel` shows 12 or 24 hour time (set the default with `-DMilitaryTime=true`), can hide the
analog `Clock`, and can show seconds and milliseconds. The clock's hands can be dragged.

The parts of `TimePanel`, `DatePanel`, `DayPanel` and `DateTimeCombo` have names
(`Component.getName()`), such as `hourSpinner`, `todayButton`, `btnBrowse` and `day1` to `day31`,
so tests and GUI testing tools can find them.

## Recent items menu

`RecentItemsMenu<T>` keeps a newest-first list of items in a `Preferences` node. You give it a
`Codec` that turns an item into one line of text and back:

```java
RecentItemsMenu<String> recent = new RecentItemsMenu<>("Recent Files",
        Preferences.userNodeForPackage(MyApp.class),
        new RecentItemsMenu.Codec<String>() {
            public String encode(String path) { return path; }
            public String decode(String line) { return line; }
        });
recent.addActionListener(e -> open((String) e.getSource()));
fileMenu.add(recent);
...
recent.addItem(path);   // after opening a file
```

The menu has an item to set the maximum number of entries (10 by default), one to clear the list,
and one item per entry. Choosing an entry moves it to the top and calls the menu's
ActionListeners. Subclasses can override `getLabel` (the text shown), `isStale` (drop entries that
no longer exist), `openItem` (what the event's source is, or null to cancel) and `merge` (keep
state from the entry an item replaces).

## Building and testing

```bash
mvn verify
```

This compiles the library, runs the tests and writes a [JaCoCo](https://www.jacoco.org/)
coverage report to `target/site/jacoco/index.html`.

The tests that paint or open dialogs need a display. They are skipped automatically when there
is none (for example on a CI server), and briefly open windows when there is.

## Releasing

Set the new version in `pom.xml`, then run `./release.sh`. It checks for GitHub credentials,
runs `mvn clean deploy` to GitHub Packages, commits any changes, tags `v<version>` and pushes.
Use `./release.sh -n` for a dry run. See the comments at the top of the script for details.

## License

Copyright 1998-2026 Tony Bringardner. Licensed under the [Apache License, Version 2.0](LICENSE).
