# swing-widgets

Reusable Swing components. So far that is a set of date and time pickers: date, day and time
panels, an analog clock, modal date/time dialogs and a date/time combo box (Swing has no date or
time picker of its own). Each group of components has its own package under
`us.bringardner.swing`.

- **Java 11** or later
- **No dependencies**
- Apache License 2.0

The date and time classes used to be the `us.bringardner.core.swing` package of BjlCore (briefly
`us.bringardner.parley.core.swing` in parley-core). Moving over means adding this dependency and
replacing that package with `us.bringardner.swing.datetime` in imports.

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

## Using the components

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
