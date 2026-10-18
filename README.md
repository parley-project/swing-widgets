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

[fx-widgets](https://github.com/parley-project/fx-widgets) is the JavaFX counterpart, but not a
copy: JavaFX already has much of what's here (CSS styling, `Alert`, prompt text, `Task` progress),
so it only fills JavaFX's own gaps. The recent list behind both menus (`RecentItems`) is in this
library because it has no UI and no dependencies, so both toolkits can reach it.

**Use swing-widgets** when your application's UI is Swing. Swing runs on any desktop JDK from Java
11, with nothing extra to install.

## Getting started

```xml
<dependency>
    <groupId>us.bringardner</groupId>
    <artifactId>bringardner-swing-widgets</artifactId>
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
| | `Theme`, `Appearance` | The themes (Gold, Dark, Light, Copper, Sage, Ocean, Slate, Plum, or one made from any color); the theme and font a user chose. |
| | `GradientLook` | The whole gradient look in one call, as parley-files-viewer has it: gradient panels and scroll bars, see-through trees and tables, gold tree and sort arrows. |
| `us.bringardner.swing.dialog` | `MessageDialog` | Gradient-styled message, warning, error and input dialogs (`showMessageDialog`, `showErrorDialog`, ...). |
| | `FontDialog`, `SettingsDialog` | Choose a font; the appearance settings (theme and font, with a preview), saved in preferences. |
| | `ThemeDesigner`, `ThemePreview` | Design a new theme from an existing one, or edit one of your own; a small window's worth of a theme. |
| `us.bringardner.swing.field` | `TextFieldPanel`, `PasswordPanel` | A text field with a prompt shown while it's empty; a password field with a show/hide eye. |
| `us.bringardner.swing.ui` | `ScrollBarUI`, `TableHeaderUI` | Gradient look for scroll bars and table headers. |
| | `GlassPane` | A glass pane that blocks input and shows a spinning globe while the application is busy. |

## JavaFX alternatives

What to use in a JavaFX application instead of each component here. Most are built into JavaFX; what
isn't is in [fx-widgets](https://github.com/parley-project/fx-widgets) or, where noted, not yet
available.

| swing-widgets | In JavaFX |
|---|---|
| `DatePanel`, `DayPanel`, `DateDialog` | `DatePicker` (built in) |
| `TimePanel`, `Clock`, `TimeDialog` | None: JavaFX has no time picker, and fx-widgets doesn't have one yet |
| `DateAndTimeDialog`, `DateTimeCombo` | None yet; `DatePicker` covers only the date |
| `RecentItemsMenu` | fx-widgets' `RecentItemsMenu`, which shares its list with this one |
| `RecentItems` | The same class: both menus use it |
| `ProgressMonitorUpdater` | A `Task`: call `updateProgress` and `updateMessage` from any thread, and bind a `ProgressBar` to its `progressProperty()` |
| `GradientLook` | fx-widgets' `GradientLook`: a stylesheet with the same look |
| `GradientPanel`, `GradientButton`, `GradientColors` | CSS: `-fx-background-color: linear-gradient(...)` and `-fx-background-radius` |
| `MessageDialog` | `Alert` and `TextInputDialog` |
| `FontDialog` | None built in (ControlsFX, a separate library, has `FontSelectorDialog`) |
| `SettingsDialog` | A `Dialog` with your own content |
| `TextFieldPanel` | `TextField.setPromptText(...)` |
| `PasswordPanel` | `PasswordField`; there's no built-in show/hide toggle |
| `ScrollBarUI`, `TableHeaderUI` | CSS (`.scroll-bar`, `.column-header`) |
| `GlassPane` | A `StackPane` layer over the window's content, holding a `ProgressIndicator` |

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

## The gradient look

parley-files-viewer's look (a diagonal gradient from light to dark gold behind everything, with trees
and tables you see it through) for any Swing application:

```java
public static void main(String[] args) {
    GradientLook.install();                  // before any window is made
    EventQueue.invokeLater(() -> {
        JFrame frame = new JFrame("My app");
        ...                                  // build the window as usual
        GradientLook.apply(frame);           // then put it on the gradient
        frame.setVisible(true);
    });
}
```

`install()` sets UIManager defaults, so it works with whatever look and feel is in use (Aqua on
macOS, Metal elsewhere); set `GradientColors` first for other colors. `apply(window)` puts a frame's
or dialog's content on a `GradientPanel` and makes its layout panels, trees, tables and lists
see-through, leaving controls and text areas alone. For finer control, `seeThrough(scrollPane)` and
`seeThroughPanels(container)` do those parts on their own. A library's own windows use `follow(window)` instead: it applies
the look only if the application installed it. For JavaFX, use
fx-widgets' `GradientLook`, which has the same colors.

### Letting users choose the font and colors

`SettingsDialog` is the appearance settings: a theme chosen from the built-in ones (Gold, Dark,
Light, Copper, Sage, Ocean, Slate, Plum) or made from any one color, and the font, with a preview.
OK puts the choice into effect in every open window and saves it; Cancel changes nothing. Install
the look with the saved choice at start-up:

```java
Preferences prefs = Preferences.userNodeForPackage(MyApp.class);
GradientLook.install(prefs);                       // instead of install()
...
SettingsDialog.showDialog(frame, prefs);           // from a Preferences menu item
```

`Appearance` is the choice itself, to load, save or apply without the dialog; `Theme` has each
theme's colors (text, fields, selections, headers and so on) for code that draws its own parts.
Gradient panels and buttons follow the current theme unless colors are set on them.

### Designing themes

In the appearance settings, **Design a theme...** opens `ThemeDesigner`: choose a theme to start
from (or a color), change any of its eleven colors, watch the preview and the readability checks
(the contrast of each kind of text against what is behind it), and save it under a new name. The
theme started from is never changed. **Edit theme...** changes one of your own themes in place
(its colors, and its name if you like), and **Delete theme** deletes one. The built-in themes can't
be edited or deleted.

Your themes are `.theme` files in `~/.bringardner/themes`, shared by every application built on
swing-widgets: a theme designed in one is offered in all of them. The system property
`bringardner.themes.dir` names another folder (the tests use one under `target`). The built-in
themes are the same kind of file, in `us/bringardner/swing/themes`, offered in the order of
`themes.list` there; the first is the default. A theme file is a properties file:

```properties
name=Dark
description=The Parley palette: ink backgrounds, warm off-white text, gold accents
dark=true
start=#3A3428
end=#0E0D0B
text=#E8E2D0
field=#17150F
fieldText=#E8E2D0
selection=#6B5620
selectionText=#F5EFDD
header=#372C0F
headerText=#D0AA42
accent=#D0AA42
buttonText=#F5EFDD
```

`Theme.read`/`write` read and write one, `Theme.userThemes()`, `saveUserTheme` and
`deleteUserTheme` manage your folder, `Theme.all()` is the built-in themes and then yours, and
`Theme.contrast` is the WCAG contrast ratio the designer checks with.

## Building and testing

```bash
mvn verify
```

This compiles the library, runs the tests and writes a [JaCoCo](https://www.jacoco.org/)
coverage report to `target/site/jacoco/index.html`.

The tests that paint or open dialogs need a display. They are skipped automatically when there
is none (for example on a CI server), and briefly open windows when there is.

## Releasing

Set the new version in `pom.xml`, then `mvn -Prelease clean deploy` publishes to Maven Central
(it needs a GPG key, and a Central Portal user token in `~/.m2/settings.xml` under the server id
`central`). Then tag the release (`v<version>`) and push.

## License

Copyright 1998-2026 Tony Bringardner. Licensed under the [Apache License, Version 2.0](LICENSE).
