# Changelog

## swing-widgets 1.1.0 (unreleased)

### Added

- `us.bringardner.swing.gradient.FolderIcon`: a folder in the current theme's colors (its light color
  with its accent around, like a manila folder; a dark theme's is its accent, darkened), closed,
  open or not readable, in any size. It takes the colors as it's painted, so it follows a theme
  chosen later.
- `us.bringardner.swing.gradient.MacAppearance`: macOS title bars and native controls dark for a
  dark theme, light for a light one, as soon as the theme changes. `Appearance.apply()` calls it,
  so changing to or from a dark theme in Settings no longer needs a restart on macOS. It uses JNA,
  an optional dependency: without JNA the appearance stays as the application started.
- `us.bringardner.swing.menu.StandardMenus`: one menu layout for every Parley application. File,
  Edit, View, the application's own menus, Window and Help, in that order. On macOS, About,
  Settings… (⌘,) and Quit (⌘Q) go in the application menu and the menu bar is at the top of the
  screen (`useScreenMenuBar`); elsewhere Settings… and Exit end the File menu and About is in Help.
  The Window menu lists the open windows and has Minimize (⌘M).
- `us.bringardner.swing.gradient.GradientLook`: parley-files-viewer's look in one call, so every
  Parley application can have it. `install()` sets the UIManager defaults (gradient panels and
  scroll bars, see-through trees and tables, the dark color for grid lines, the table header and
  the tree selection, and the gold tree and sort arrows); `seeThrough(scrollPane)` lets the gradient
  show through a scroll pane and its tree or table; `seeThroughPanels(container)` does the same for
  plain layout panels; `apply(window)` does it all for a frame or dialog, putting its content on a
  `GradientPanel`; `follow(window)` does that only if the application installed the look, for a
  library's own windows.
- The tree and sort arrow icons, and the tree node icon, from parley-files-viewer, in
  `us/bringardner/swing/icons`.
- `us.bringardner.swing.gradient.Theme`: a named set of colors for each part of the look: the
  gradient's two ends, text on the gradient, fields, selections, table headers, outlines and
  gradient-button text, and whether it is dark. Built in (`THEMES`): Gold (the default), Dark (the
  Parley ink and gold), Light, Copper, Sage, Ocean, Slate and Plum; `fromColor` makes one from any
  one color. `GradientColors` now reads and sets the current theme's gradient.
- `us.bringardner.swing.gradient.Appearance`: the theme and font a user chooses, with
  `load`/`save` in `java.util.prefs.Preferences` (the font keys are the ones parley-files-viewer
  already uses, so saved fonts carry over) and `apply()`, which updates the windows already open.
  `GradientLook.install(prefs)` installs the look with the saved choice.
- `GradientPanel.getGradientStart()` / `getGradientEnd()`: the colors a panel paints with.
- Themes are `.theme` files (properties: a name, a description, whether it is dark, and the eleven
  colors as `#RRGGBB`). The built-in ones are in `us/bringardner/swing/themes`, in the order of
  `themes.list`. `Theme.read` and `write` read and write one.
- The user's own themes, in `~/.bringardner/themes` (or the folder the `bringardner.themes.dir`
  system property names), shared by every application built on swing-widgets:
  `Theme.userThemes()`, `saveUserTheme`, `deleteUserTheme`, `userThemesDir()`, and `Theme.all()`
  (the built-in themes, then the user's). `Theme.named` finds the user's themes too, so a saved
  `Appearance` can name one.
- `ThemeDesigner`: designs a new theme starting from any theme (or a color), with every color
  editable by a color chooser or as hex, a preview, and readability checks; saving always makes a
  new theme. It also edits one of the user's own themes in place, including renaming it.
  `Theme.contrast` is the WCAG contrast ratio it checks with.
- `ThemePreview`: a small window's worth of any theme, used by the settings dialog and the designer.
- `ThemeExtension` and `ThemeExtensions`: an application's own part of a theme (such as a script
  editor's syntax colors), with a tab in the theme designer. A theme keeps applications' settings as
  extra `<id>.<key>` lines in its file (`Theme.getExtras()`, `withExtras`, `values`, `withValues`,
  `hasValues`); a new theme starts with its parent's, and themes without their own get the
  extension's defaults.
- `SettingsDialog`: New theme..., Edit theme... and Delete theme (the last two for the user's own
  themes), and the user's themes among the cards.

### Changed

- `SettingsDialog` works now: it was a sketch whose OK did nothing, whose colors were never kept and
  whose font change took effect even on Cancel. It shows the themes as cards (each a small picture of
  the theme) with a Custom... that makes one from a single color (instead of "Color 1" and
  "Color 2"), the font with Change... and Use the standard font, a preview, and Reset to defaults.
  OK applies and, given a preferences node, saves; Cancel changes nothing. New:
  `SettingsDialog(Preferences)`, `SettingsDialog(Window, Preferences)` and
  `showDialog(Window, Preferences)`.
- `GradientPanel` and `GradientButton` paint with the current theme unless colors are set on them,
  so they follow a theme change; they no longer copy the colors when they are created.
  `GradientButton`'s text is the theme's button text color unless `setTextColor` is used.
  `FontDialog` and `MessageDialog` follow the theme too.
- In the gold theme, selected rows and table headers have white text: light gold on dark gold was
  hard to read.

## swing-widgets 1.0.0 (2026-10-16)

The Swing date and time components are now in their own library, split out of BjlCore / parley-core
so the Parley protocol libraries no longer bring in Swing. The code is the same as BjlCore 1.3.1;
only names changed.

### Changed (needs a code change)

- Maven coordinates: add `us.bringardner:bringardner-swing-widgets`. parley-core no longer has these classes.
- Package: `us.bringardner.core.swing` (BjlCore) and `us.bringardner.parley.core.swing`
  (parley-core) are now `us.bringardner.swing.datetime`.
- Module name (`Automatic-Module-Name`): `us.bringardner.swing`.

### Added

- `us.bringardner.swing.menu.RecentItemsMenu`, a "Recent ..." menu of any kind of item, saved
  with `java.util.prefs.Preferences`. It is the generic part of parley-files' `RecentFileMenu`
  (BjlFileSystem's before that), which is now built on it.
- `us.bringardner.swing.menu.RecentItems`: the list behind `RecentItemsMenu` (newest first, the
  maximum, saving with `Preferences`), with no UI, so the JavaFX menu in fx-widgets can use it too.
  `RecentItemsMenu` is built on it and works as before; `getRecentItems()` returns it.
- `us.bringardner.swing.progress.ProgressMonitorUpdater`, which lets any thread update a Swing
  `ProgressMonitor`. It is the code of parley-files' `ProgressMonitorProgress`, which now extends it.
- Gradient-styled widgets from BjlFileSystemViewer (now parley-files-viewer), which uses them from
  here: `us.bringardner.swing.gradient` (`GradientPanel`, `GradientButton`, and `GradientColors`, which
  replaces the colors they read from the viewer), `us.bringardner.swing.dialog` (`MessageDialog`,
  `FontDialog`, `SettingsDialog`), `us.bringardner.swing.field` (`TextFieldPanel`, `PasswordPanel`)
  and `us.bringardner.swing.ui` (`ScrollBarUI`, `GlassPane`, and `TableHeaderUI`, formerly
  `BjlTableHeaderUI`). Their icons are in `/us/bringardner/swing/icons`.

For earlier changes to these classes see the BjlCore changelog.
