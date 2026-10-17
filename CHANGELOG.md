# Changelog

## swing-widgets 1.1.0 (unreleased)

### Added

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
- `us.bringardner.swing.gradient.Appearance`: the font and color scheme a user chooses, with
  named schemes (`SCHEMES`: Gold, Copper, Sage, Ocean, Slate, Plum), `Scheme.fromColor` to make a
  scheme from any one color, `load`/`save` in `java.util.prefs.Preferences` (the font keys are the
  ones parley-files-viewer already uses, so saved fonts carry over), and `apply()`, which updates the
  windows already open. `GradientLook.install(prefs)` installs the look with the saved choice.

### Changed

- `SettingsDialog` works now: it was a sketch whose OK did nothing, whose colors were never kept and
  whose font change took effect even on Cancel. It shows the font with Change... and Use the
  standard font, the color schemes as swatches with a Custom... that makes one from a single color
  (instead of "Color 1" and "Color 2"), a preview, and Reset to defaults. OK applies and, given a
  preferences node, saves; Cancel changes nothing. New: `SettingsDialog(Preferences)`,
  `SettingsDialog(Window, Preferences)` and `showDialog(Window, Preferences)`.

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
