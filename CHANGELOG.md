# Changelog

## swing-widgets 1.0.0 (unreleased)

The Swing date and time components are now in their own library, split out of BjlCore / parley-core
so the Parley protocol libraries no longer bring in Swing. The code is the same as BjlCore 1.3.1;
only names changed.

### Changed (needs a code change)

- Maven coordinates: add `us.bringardner:swing-widgets`. parley-core no longer has these classes.
- Package: `us.bringardner.core.swing` (BjlCore) and `us.bringardner.parley.core.swing`
  (parley-core) are now `us.bringardner.swing.datetime`.
- Module name (`Automatic-Module-Name`): `us.bringardner.swing`.

### Added

- `us.bringardner.swing.menu.RecentItemsMenu`, a "Recent ..." menu of any kind of item, saved
  with `java.util.prefs.Preferences`. It is the generic part of parley-files' `RecentFileMenu`
  (BjlFileSystem's before that), which is now built on it.
- `us.bringardner.swing.progress.ProgressMonitorUpdater`, which lets any thread update a Swing
  `ProgressMonitor`. It is the code of parley-files' `ProgressMonitorProgress`, which now extends it.
- Gradient-styled widgets from BjlFileSystemViewer (now parley-files-viewer), which uses them from
  here: `us.bringardner.swing.gradient` (`GradientPanel`, `GradientButton`, and `GradientColors`, which
  replaces the colors they read from the viewer), `us.bringardner.swing.dialog` (`MessageDialog`,
  `FontDialog`, `SettingsDialog`), `us.bringardner.swing.field` (`TextFieldPanel`, `PasswordPanel`)
  and `us.bringardner.swing.ui` (`ScrollBarUI`, `GlassPane`, and `TableHeaderUI`, formerly
  `BjlTableHeaderUI`). Their icons are in `/us/bringardner/swing/icons`.

For earlier changes to these classes see the BjlCore changelog.
