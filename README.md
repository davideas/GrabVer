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
Automatic Incremental Gradle Versioning</a>. Customized into library with PreRelease, Auto-Reset and Sub-Modules features.</p>

> Easy to apply, it _works with **any** project type with sub modules too._

## Rules
###### User values
**major**: Required, user defined value for breaking changes.<br>
**minor**: Required, user defined value for new features, but backwards compatible. If you increase _Major_ version, this value must be coherent(=0).<br>
**patch**: Optional, user defined value (or auto-generated value) for backwards compatible bug fixes only.<br>
**preRelease**: Optional, user defined value for pre-releases suffix.<br>
**incrementOn**: Optional, custom task name to trigger the increase of the version (default: `assembleRelease`, `bundleRelease`, `grabverRelease`).<br>
**saveOn**: Optional, custom task name for which you want to save the versioning file (default: `build`, `assembleDebug`, `assembleRelease`, `bundleDebug`, `bundleRelease`, `grabverRelease`, `jar`, `war`, `explodedWar`).

###### Calculation
**patch** - If not specified by user, increases at each release, but it auto resets back to 0 when _Minor_ or _Major_ version changes or if _preRelease_ is set.<br>
**build** - Increases at each build.<br>
**code** - Increases at each release.

## Installation
Configure script dependencies in the project _build.gradle_ file:
``` gradle
buildscript {
    repositories {
        // Using Bintray repository:
        maven { url "http://dl.bintray.com/davideas/maven" }
        // or Gradle plugins repository:
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        // Using Bintray repository:
        classpath 'eu.davidea:grabver:2.0.0'
        // or with Gradle plugins repository
        classpath "gradle.plugin.eu.davidea:grabver:2.0.0"
    }
}
```

## Usage
### 1. Version configuration
Apply the plugin in the _module_ you desire, it will create a properties file under that module!
``` gradle
plugins {
    ...
    id: 'eu.davidea.grabver'
}

versioning {
    // Required (number)
    major 1
    minor 0
    // Optional, force custom patch (number)
    patch 7
    // Optional (any string)
    preRelease "RC1"
    // Optional, custom task name to trigger the increase of the version
    increaseOn [or incrementOn] "<task-name>"
    // Optional, custom task name for which you want to save the versioning file
    saveOn "<task-name>"
}
```

### 2. Grab your new version
``` gradle 
versioning.major
versioning.minor
versioning.patch
versioning.build
versioning.preRelease
versioning.code        // needed for all Android projects
versioning.name        // output: "major.minor.patch[-preRelease]"
versioning.fullName    // output: "major.minor.patch[-preRelease] #build built on yyyy.mm.dd"
versioning.builtOn     // output: " built on yyyy.mm.dd"
versioning.date        // or versioning.getDate([format]) - default "yyyy.mm.dd"
```
> **Note:** To trigger the evaluation, the user must grab one of the above attribute.

### 3. Run it
- Via command line:
```
/**
 * To increment build number only:
 * - Code and Patch remain unchanged
 */
gradle [build | jar | war | explodedWar | assembleDebug | bundleDebug]

/**
 * To increment build, patch and code:
 * Code and Patch are incremented because of releases:
 * - Code is incremented if exists a release task.
 *   Example: "bundleRelease", "grabverRelease", ":module:assembleRelease"
 * - But Patch may be resetted if Major or Minor is changed or if preRelease is set
 */
gradle [assembleRelease | bundleRelease | grabverRelease]
```
- In Android Studio:
  - via menu _build > Build Bundle / APK_ (bundleDebug | assembleDebug)
  - via menu _build > Generate Signed Bundle / APK_ (bundleRelease | assembleRelease).
  - by _running the App_ (assembleDebug | assembleRelease, depending by the build variant).

From [version 2](https://github.com/davideas/GrabVer/releases/tag/2.0.0), the plugin evaluates the run tasks
in silent mode to automatically skip the new version evaluation and to not print unnecessary logs _if_ no save task
was detected. A single warning line is however produced.<br>
Single tasks performed such as _clean, test, flyway_ and **all** third parties invented plugin
tasks out there will not trigger the versioning evaluation. Example:

|Command|Outcome|
|---|---|
|`gradle clean test`|Evaluation skipped|
|`gradle javadoc`|Evaluation skipped|
|`gradle flywayClean`|Evaluation skipped|
|`gradle clean build`|Evaluation triggered and new values saved|
|`gradle clean war grabverRelease`|Evaluation triggered, versioning increased and new values saved|

> **Note:** File `version.properties` is auto-generated, but once it's created, you can modify its content
as of your convenience. Just remember to add it to your Version Control System (from time to time).

# Contributions
Everybody is welcome to improve existing solution.

> **Note:** Unit tests work fine if you open the project with IntelliJ Idea, while with Android Studio
they don't. Alternatively, you can simulate a real build script by running `gradle install`
and `gradle -b build-test.gradle [grabverRelease]` OR testing with modules `gradle build [grabverRelease]`.

# License

    Copyright 2017-2019 Davidea Solutions Sprl

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.