[![Licence](https://img.shields.io/badge/Licence-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
 
# GrabVer - Gradle Automatic Build Versioning Plugin
An easy to apply Gradle plugin that follows [semver.org](http://semver.org/) rules to
automatically generate the _Patch_ version, _Build_ number, _Code_ version, while _Major_,
_Minor_ and _Pre-Release_ suffix remain under our control.

I saw plenty of plugins that require a long configuration and continuous adjustment just to update
those numbers, if so, better without any plugin then! With this plugin we are required to manage
only 3 variables.

Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>Android Studio
Automatic Incremental Gradle Versioning</a>. Customized into library with PreRelease and Auto-Reset features.</p>

> Easy to apply, it _works with any project type._

## Rules
_major_: User defined value for breaking changes.<br/>
_minor_: User defined value for new features, but backwards compatible.<br/>
_patch_: Auto generated value for backwards compatible bug fixes only.<br/>
_preRelease_: User defined optional value for pre-releases suffix.

**build** - increases at each build.<br/>
**code** - increases at each release.<br/>
**patch** - increases at each release, but it auto resets back to 0 when _Minor_ or _Major_ version changes or if pre-release is set.<br/>
**minor** - user value, but it auto resets back to 0 when _Major_ version changes.</p>

## Installation
```
// 1. Temporary run "gradle install" to create jar in local repository
// 2. Configure dependencies
```
``` groovy
buildscript {
    repositories {
        // Publishing is in progress! Please, use local repository for now
        mavenLocal()
	}
    dependencies {
        classpath 'eu.davidea:grabver:0.2.0'
    }
}
```

## Usage
### 1. Version configuration
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
versioning.versionCode // or .code
versioning.versionName // output: "major.minor.patch[-preRelease]"
versioning.getDate([format]) // default "yyyy.mm.dd"
```

### 3. Run it
```
// To increase build only
gradle build
// To increase build, code and patch
// Minor is reset if major is changed
// Patch is reset if major or minor is changed or if pre-release
gradle assembleRelease
```
File `version.properties` is auto-generated, but once it's created, you can modify its content
as of your convenience.

# Contributions
Everybody is welcome to improve existing solution.

**Note:** Unit tests work fine if you open the project with IntelliJ Idea, while with Android Studio
they don't. Alternatively, you can simulate a real build script by running
`gradle install` and `gradle --build-file build-test.gradle [test_release]`.

# License

    Copyright 2017 Davide Steduto

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.