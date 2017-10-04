[![Licence](https://img.shields.io/badge/Licence-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Gradle](https://img.shields.io/badge/Gradle-Plugin-green.svg)](https://plugins.gradle.org/plugin/eu.davidea.grabver)

<img src="./art/grabver.png">

An easy Gradle plugin that follows [semver.org](http://semver.org/) rules to
automatically generate the _Patch_ version, _Build_ number and _Code_ version, while _Major_,
_Minor_ and _Pre-Release_ suffix remain under our control.

I saw plenty of plugins that require a long configuration and continuous adjustment just to update
those numbers, if so, better without any plugin then! With this plugin we are required to manage
only 2 variables.

Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>Android Studio
Automatic Incremental Gradle Versioning</a>. Customized into library with PreRelease, Auto-Reset and Sub-Modules features.</p>

> Easy to apply, it _works with any project type with sub modules too._

## Rules
_major_: User defined value for breaking changes.<br>
_minor_: User defined value for new features, but backwards compatible.<br>
_patch_: Auto generated value for backwards compatible bug fixes only.<br>
_preRelease_: Optional, user defined value for pre-releases suffix.<br>
Auto-skip versioning when 'clean', 'test' or 'grabverSkip' tasks are enqueued.

**build** - Increases at each build.<br>
**code** - Increases at each release.<br>
**patch** - Increases at each release, but it auto resets back to 0 when _Minor_ or _Major_ version changes or if _preRelease_ is set.<br>
**minor** - User defined value, it must be coherent(=0) if you increase _Major_ version.

## Installation
Configure script dependencies in the project _build.gradle_ file:
``` groovy
buildscript {
    repositories {
        // Please Use jcenter for dependecies: mavenCentral is not enough
        jcenter()
        // and (for now)
        maven { url "http://dl.bintray.com/davideas/maven" }
        // or Gradle Plugins Repository
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath 'eu.davidea:grabver:0.6.0'
        // or with Gradle Plugins Repository
        classpath "gradle.plugin.eu.davidea:grabver:0.6.0"
    }
}
```

## Usage
### 1. Version configuration
Apply the plugin in the _module_ you desire, it will create a properties file under that module!
``` groovy
apply plugin: 'eu.davidea.grabver'

versioning {
    // must be numbers
    major = 1
    minor = 0
    // optional (any string)
    preRelease = 'RC1'
}
```

### 2. Grab your new version
``` groovy 
versioning.major
versioning.minor
versioning.patch
versioning.build
versioning.preRelease
versioning.code            // needed for all Android projects
versioning.name            // output: "major.minor.patch[-preRelease]"
versioning.fullVersionName // output: "major.minor.patch[-preRelease] #build built on yyyy.mm.dd"
versioning.builtOn         // output: " built on yyyy.mm.dd"
versioning.date            // or .getDate([format]) - default "yyyy.mm.dd"
```

### 3. Run it
- Via command line:
```
// To increase build only:
// - Code and Patch remain unchanged
gradle [build | assembleDebug]

// To increase build, patch and code:
// - Code and Patch are increased because of release
// - But Patch can be resetted if Major or Minor is changed or if preRelease is set
gradle [grabverRelease | assembleRelease]
```
- In Android Studio:
  - via menu _build > Build APK_ (assembleDebug) | _Generate signed APK_ (assembleRelease).
  - by _running the App_ (assembleDebug | assembleRelease, depending by the build variant).

**Note:** File `version.properties` is auto-generated, but once it's created, you can modify its content
as of your convenience. Just remember to add it to your Version Control System.

# Contributions
Everybody is welcome to improve existing solution.

**Note:** Unit tests work fine if you open the project with IntelliJ Idea, while with Android Studio
they don't. Alternatively, you can simulate a real build script by running `gradle install`
and `gradle --build-file build-test.gradle [grabverRelease]` OR testing with modules `gradle build [grabverRelease]`.

# License

    Copyright 2017 Davidea Solutions Sprl

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.