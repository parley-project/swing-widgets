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

For earlier changes to these classes see the BjlCore changelog.
