[![SemVer](https://img.shields.io/badge/SemVer-2.0.0-blue.svg)](https://semver.org/spec/v2.0.0.html)
[![Changelog](https://img.shields.io/badge/Changelog-Keep%20a%20Changelog-orange.svg)](https://keepachangelog.com/en/1.1.0/)

# Changelog
All notable changes to this project will be documented in this file.<br>
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.0.0] - Unreleased
### Planned
- Configuration Cache support.
- Removal of deprecated `saveOn` property.
- Migration from `buildFinished`/`afterTask` to Build Services API.
- Evaluate removal of `java-ordered-properties` dependency.
- Evaluate Gradle version compatibility check (minimum Gradle 8.1).
- Upgrade `com.gradle.plugin-publish` from 1.2.1 to 1.3.1.

### Compatibility
- **Users**: Gradle 8.1+ / Java 17+
- **Contributors**: Gradle 9.x wrapper / JDK 21+
- Gradle 7.x skipped: Configuration Cache is opt-in with unstable Build Services and listener APIs.

## [2.1.0] - 2026-03-15
### Compatibility
- **Users**: Gradle 7.0+ / Java 11+
- **Contributors**: Gradle 8.x wrapper / JDK 17+

### Added
- `CHANGELOG.md` file to track all notable changes.
- Optional property `incrementBuild` (default: `true`) to prevent constant modifications of `version.properties` [#26], [#29]
- Added internal function `hasUserChanges()` detection - allows to save file when user manually changes main version values.
- Gradle `defaultTasks()` included in runTasks evaluation [#33]
- Android Product Flavor support - recognizes flavor tasks like `assembleFreeRelease`, `bundleProRelease` [#25]
- Android detection - checks for `com.android.application` or `com.android.library` plugins.
- :warning: `grabverVerbose` task (renamed from `grabverDebug`) for verbose logging.
- GrabVer tasks are now registered under `versioning` group.
- Gradle wrapper for development/publishing.

### Changed
- :warning: Property `incrementOn` now also triggers save: Custom release task (like deploy/publish) defined via `incrementOn`
  is now correctly detected as save-task, ensuring version changes are always persisted.
- :warning: Task monitoring improvements:<br>
  Monitored tasks only track user-invoked tasks, filtering out internal sub-tasks (e.g., AGP's `bundleLib*`) from
  build output. `gradle build` is treated as debug even if AGP executes both `assembleDebug` and `assembleRelease`
  internally. Use `gradle assembleRelease` explicitly to trigger release versioning.
- :warning: Task Matching Logic with case-sensitive comparison, consistent with Gradle's own behavior.<br>
  Separated exact match tasks from prefix+suffix patterns. Suffix patterns (Debug/Release) match Android's
  camelCase convention (e.g., assembleFreeRelease, bundleProDebug).
- Comprehensive logging - "release" vs "debug" build clarity and better task status display
- Android-specific logging - shows 'Code' messages only for Android projects.
- Modern Gradle API - replaced deprecated `project.task()` with `project.tasks.register()`.
- Independent `settings.gradle` for test modules (composite build testing).

### Deprecated
- `saveOn` - will be removed in next major release. Use `incrementOn` instead.
  With `incrementOn` covering custom release tasks and `incrementBuild` controlling debug build behavior,
  there is no remaining scenario requiring a custom save task. Default save tasks already cover all standard builds.

### Fixed
- NPE when user doesn't access versioning values - shows a warning message.
- Version code starting from 0 now correctly produces `code=1` on the first release. Previously, a debug build
  would save `code=1` to the file, causing the first release to skip to `code=2`. The version code is now guaranteed
  to be ≥ 1 at read time (required by Android), while the file preserves the raw value [#15], [#17]

## [2.0.3] - 2024-08-01
### Compatibility
- **Users**: Gradle 7.6+ / Java 19+
- **Contributors**: Gradle 8.x wrapper / JDK 19+

### Changed
- Compiled with Gradle 8.x (Groovy 3.0) and OrderedProperties 1.0.4.
- Removed JCenter repository.
- Migrated to new plugin-publish for Gradle Plugin Portal publication.

## [2.0.2] - 2019-10-01
### Changed
- Upgraded OrderedProperties dependency for Java 11 [PR #22]


### Fixed
- Support for `module/someDir/sub-module` location [#23]


## [2.0.1] - 2019-06-25
### Added
- `grabverDebug` dummy task to print debug steps.

### Changed
- Reduced verbosity

## [2.0.0] - 2019-06-17
### Compatibility
- **Users**: Gradle 4.9+ / Java 8+

### Added
- Optional attribute `incrementOn`: accepts a custom task name to trigger the version increase based on the rules.
  Tasks supported by default: `assembleRelease`, `bundleRelease`, `grabverRelease` [#19]
- Silent mode evaluation - the plugin now evaluates the run tasks in silent mode to automatically skip the new version
  evaluation and to not print unnecessary logs if no save task was detected. A single warning line is however produced.
  Single tasks performed such as _clean, test, flyway_ and all third parties invented plugin tasks out there will 
  not trigger the versioning evaluation. Example:

  |Command|Outcome|
  |---|---|
  |`gradle clean test`|Evaluation skipped|
  |`gradle javadoc`|Evaluation skipped|
  |`gradle flywayClean`|Evaluation skipped|
  |`gradle clean build`|Evaluation triggered and new values saved|
  |`gradle clean war grabverRelease`|Evaluation triggered, versioning increased and new values saved|

- More default save tasks: `build`, `assembleDebug`, `assembleRelease`, `bundleDebug`, `bundleRelease`, `grabverRelease`, `jar`, `war`, `explodedWar`.
- Colors and bold effects in logs.
- Info log line with execution state showing which task triggered the saving.

### Changed
- First run on Gradle syncing (`gradle <no tasks>`) generates the properties file with default values `1.0.0 #1 code=1`
  even without user configuration. In all other cases the properties file is saved when it passes the silent evaluation
  and the save tasks complete with success.<br>
  **Note:** To trigger the evaluation, the user must grab at least one value [#21]
- New values are saved when all tasks have been completed and build is finished.
- When `gradle` is executed without tasks (e.g., Gradle syncing), evaluation is skipped with a warning log.
- General code and logs improvements.

### Removed (deprecated since 1.0.1)
- Removed attribute `dependsOn`.
- Removed task `grabverSkip`, became obsolete after new solution.
- Removed getter `getFullVersionName` (`fullVersionName`) in favor of `getFullName` (`versioning.fullName`).

### Fixed
- Android bundle support: the correct task to monitor is `bundleRelease`. Code value will increase when this task is in runTasks [#18]

## [1.0.1] - 2018-12-02
### Added
- Plugin now available in Gradle Plugins repository [#16]
- Added new property `saveOn`.
- To give a better idea of what this plugin property does. Example, to have patch number automatically increased and
  saved on property file when task name `jar` is specified, run command line:
  ```
  gradle jar grabverRelease
  ```
  having
  ```
  versioning {
      ....
      saveOn "jar"
  }
  ```

### Deprecated
- `dependsOn` in favor of `saveOn`.

### Fixed
- Version Code starts with 1 [#15], [#17]
- Code doesn't increase on Android `bundle` release [#14], [#18], [#20]

## [1.0.0] - 2018-08-23
### Added
- Support for Android App Bundle (`bundle*`) tasks [#14]

## [0.7.0] - 2017-11-15
### Added
- Optionally set directly the patch number: skips auto-increase [#11]
- Introduced optional field `dependsOn`: saving versioning file depends on the task-name specified here
  (default: `compileJava`, `assembleDebug`, `assembleRelease`) [#9]

### Fixed
- Replace hardcoded path separators - supports unix/win file separator [#8]
- When the generate signed APK fails, should not increase the patch and build numbers [#9]

## [0.6.0] - 2017-10-04
### Added
- `grabverSkip` task, so calculation can be skipped

### Changed
- Improved `toString`

### Fixed
- Patch not increasing in `assembleRelease` when "Build Signed APK" is chosen from the menu [#6]
- Ignoring `preRelease` empty string [#7]

## [0.5.0] - 2017-09-23
### Fixed
- "A problem occurred configuring project ':app'" caused by NPE [#3]
- OrderedProperties in `version.properties` [#5]

## [0.4.1] - 2017-09-14
### Fixed
- `preRelease` not cleared [#2]

## [0.4.0] - 2017-09-08
### Added
- `preRelease` suffix support.
- Auto-reset patch when major/minor changes.
- Auto-skip `test` task.

### Changed
- Printing the path of the versioning filename.

### Fixed
- Versioning module bug when launching the build from within the module itself.
- Removal of `skipOnTask`.

## [0.3.0] - 2017-08-06
### Initial public release
- `major`: User defined value for breaking changes.
- `minor`: User defined value for new features, but backwards compatible.
- `patch`: Auto generated value for backwards compatible bug fixes only.
- `preRelease`: Optional, user defined value for pre-releases suffix.
- `build` - increases at each build.
- `code` - increases at each release.
- `patch` - increases at each release, but it auto resets back to 0 when _minor_ or _major_ version changes or if _preRelease_ is set.
- Auto-skip versioning when `clean` or `test` tasks are enqueued.

[3.0.0]: https://github.com/davideas/GrabVer/compare/2.1.0...HEAD
[2.1.0]: https://github.com/davideas/GrabVer/compare/2.0.3...2.1.0
[2.0.3]: https://github.com/davideas/GrabVer/compare/2.0.2...2.0.3
[2.0.2]: https://github.com/davideas/GrabVer/compare/2.0.1...2.0.2
[2.0.1]: https://github.com/davideas/GrabVer/compare/2.0.0...2.0.1
[2.0.0]: https://github.com/davideas/GrabVer/compare/1.0.1...2.0.0
[1.0.1]: https://github.com/davideas/GrabVer/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/davideas/GrabVer/compare/0.7.0...1.0.0
[0.7.0]: https://github.com/davideas/GrabVer/compare/0.6.0...0.7.0
[0.6.0]: https://github.com/davideas/GrabVer/compare/0.5.0...0.6.0
[0.5.0]: https://github.com/davideas/GrabVer/compare/0.4.1...0.5.0
[0.4.1]: https://github.com/davideas/GrabVer/compare/0.4.0...0.4.1
[0.4.0]: https://github.com/davideas/GrabVer/compare/0.3.0...0.4.0
[0.3.0]: https://github.com/davideas/GrabVer/releases/tag/0.3.0

[#2]: https://github.com/davideas/GrabVer/issues/2
[#3]: https://github.com/davideas/GrabVer/issues/3
[#5]: https://github.com/davideas/GrabVer/issues/5
[#6]: https://github.com/davideas/GrabVer/issues/6
[#7]: https://github.com/davideas/GrabVer/issues/7
[#8]: https://github.com/davideas/GrabVer/issues/8
[#9]: https://github.com/davideas/GrabVer/issues/9
[#11]: https://github.com/davideas/GrabVer/issues/11
[#14]: https://github.com/davideas/GrabVer/issues/14
[#15]: https://github.com/davideas/GrabVer/issues/15
[#16]: https://github.com/davideas/GrabVer/issues/16
[#17]: https://github.com/davideas/GrabVer/issues/17
[#18]: https://github.com/davideas/GrabVer/issues/18
[#19]: https://github.com/davideas/GrabVer/issues/19
[#20]: https://github.com/davideas/GrabVer/issues/20
[#21]: https://github.com/davideas/GrabVer/issues/21
[#23]: https://github.com/davideas/GrabVer/issues/23
[#25]: https://github.com/davideas/GrabVer/issues/25
[#26]: https://github.com/davideas/GrabVer/issues/26
[#29]: https://github.com/davideas/GrabVer/issues/29
[#33]: https://github.com/davideas/GrabVer/issues/33
[PR #22]: https://github.com/davideas/GrabVer/pull/22
