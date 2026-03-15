[![Gradle](https://img.shields.io/badge/Gradle-Plugin-green.svg)](https://plugins.gradle.org/plugin/eu.davidea.grabver)
[![Gradle](https://img.shields.io/badge/Android-√-darkgreen.svg)](https://developer.android.com)
[![Gradle](https://img.shields.io/badge/Spring_Boot-√-darkgreen.svg)](https://spring.io/projects/spring-boot)
[![Licence](https://img.shields.io/badge/Licence-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

![Logo](./art/grabver.png)

An easy Gradle plugin that follows [semver.org](http://semver.org/) rules to
automatically generate the _Patch_ version, _Build_ number and _Code_ version, while _Major_,
_Minor_ and _Pre-Release_ suffix remain under our control.

I saw plenty of plugins that require a long configuration and continuous adjustment just to update
those numbers, if so, better without any plugin then! With this plugin we are required to manage
only 2 variables.

Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>Android Studio
Automatic Incremental Gradle Versioning</a>. Customized into a plugin with PreRelease, Auto-Reset and Sub-Modules features.</p>

> :speech_balloon: Easy to apply, it _works with **any** project type with sub modules too._

## Rules
###### User values
**major**: Required, user defined value for breaking changes.<br>
**minor**: Required, user defined value for new features, but backwards compatible. If you increase _Major_ version, this value must be coherent(=0).<br>
**patch**: Optional, user defined value (or auto-generated value) for backwards compatible bug fixes only.<br>
**preRelease**: Optional, user defined value for pre-releases suffix.<br>
**incrementBuild**: Optional, set to `false` to disable build number auto-increment during development (default: `true`).<br>
**incrementOn**: Optional, custom task name to trigger the increase of the version and save. By default, any `assemble*Release`, `bundle*Release` (including flavors) and `grabverRelease` are already detected.

###### Calculation
**patch** - If not specified by user, increases at each release, but it auto resets back to 0 when _Minor_ or _Major_ version changes or if _preRelease_ is set.<br>
**build** - Increases at each build.<br>
**code** - Increases at each release.

## Installation
Configure **pluginManagement** in _settings.gradle_ file: 
``` gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id "eu.davidea.grabver" version "2.1.0"
    }
}
```
Configure **build script** in each _build.gradle_ file:
``` gradle
plugins {
    id "eu.davidea.grabver"
}
```

## Usage
### 1. Version configuration
Apply the plugin in the _module_ you desire, it will create a `version.properties` file under that module!
``` gradle
plugins {
    ...
    id 'eu.davidea.grabver'
}

versioning {
    // Required (number)
    major 1
    minor 0
    // Optional, force custom patch (number)
    patch 7
    // Optional (any string)
    preRelease "RC1"
    // Optional, disable build number increment during development (default: true)
    incrementBuild false
    // Optional, custom task name to trigger the increase of the version and save
    incrementOn "<task-name>"
}
```
> :speech_balloon: **Note:** The file `version.properties` is auto-generated, but once it's created, you can modify its content
as of your convenience. Just remember to add it to your Version Control System (from time to time).

### 2. Grab your new version
``` gradle 
versioning.major
versioning.minor
versioning.patch
versioning.build
versioning.preRelease
versioning.code        // auto-incremental integer, needed for all Android Apps
versioning.name        // output: "major.minor.patch[-preRelease]"
versioning.fullName    // output: "major.minor.patch[-preRelease] #buildNr built on yyyy.MM.dd"
versioning.builtOn     // output: " built on yyyy.MM.dd"
versioning.date        // or versioning.getDate([format]) - default "yyyy.MM.dd"

// Example: grab the version name
version = versioning.name
```
> :warning: **Note:** To trigger the evaluation, user must grab one of `major` `minor` `patch` `build` `code` `name` `fullName` attribute!

### 3. Run it
Via command line:
```
/**
 * Debug: increments build number only.
 * Code and Patch remain unchanged.
 */
gradle [build | jar | war | explodedWar]

/**
 * Release: increments build, patch and code.
 * - Code is incremented if a release task is invoked.
 * - Patch may be reset if Major or Minor changed or if preRelease is set.
 */
gradle [grabverRelease]
```

#### Android
The plugin recognizes Android tasks including product flavors:
```
gradle [assembleDebug | bundleDebug | assembleFlavorDebug | bundleFlavorDebug]
gradle [assembleRelease | bundleRelease | assembleFlavorRelease | bundleFlavorRelease]
```
In Android Studio:
- _Build > Generate Bundle / APK_ (assembleDebug | bundleDebug)
- _Build > Generate Signed Bundle / APK_ (assembleRelease | bundleRelease)
- _Run the App_ (assembleDebug | assembleRelease, depending on the build variant)

#### Silent mode
The plugin evaluates the run tasks in silent mode to automatically skip the version evaluation
and to not print unnecessary logs _if_ no relevant task was detected. A single log line is however produced.<br>
Single tasks such as _clean, test, flyway_ and **all** third-party plugin tasks will not trigger
the versioning evaluation.
Default tasks defined via `defaultTasks` are also evaluated together with command line tasks. Example:

|Command|Outcome|
|---|---|
|`gradle clean test`|Evaluation skipped|
|`gradle javadoc`|Evaluation skipped|
|`gradle flywayClean`|Evaluation skipped|
|`gradle clean build`|Evaluation triggered and new values saved|
|`gradle clean war grabverRelease`|Evaluation triggered, versioning increased and new values saved|
| `defaultTasks('build')` + `gradle`|Evaluation triggered via default tasks|

### 4. Control Build Number Increment
By default, the build number increments on every build. To avoid constant `version.properties` changes during
development (useful for Git workflows), disable it:

```gradle
versioning {
    major 1
    minor 2
    incrementBuild false
}
```
**Scenarios (when flag=false):**
- Debug builds → Build number unchanged → File _not_ saved
- Release builds → Code/patch/build increment → File saved
- Manual version changes (major/minor/patch/preRelease) → File saved

> :bulb: **Tip:** Toggle `incrementBuild` based on your workflow needs.

# Changelog
See [CHANGELOG.md](CHANGELOG.md) for all notable changes.

# Contributions
Everybody is welcome to improve existing solution. Many features and bug fixes already come from
user requests - don't hesitate to [open an issue](https://github.com/davideas/GrabVer/issues),
even if it may take some time to address.

> :speech_balloon: **Note:** Unit tests work fine if you open the project with IntelliJ Idea, while with Android Studio
they don't. Alternatively, you can simulate a real build script by running `gradlew publishToMavenLocal` and
`gradlew -b build-test.gradle build [<release task>]` OR testing within modules_a/b `gradlew build [<release task>]`.<br>
Additional instructions inside `build-test.gradle`.

# License

    Copyright 2017-2026 Davidea Solutions Srl

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
