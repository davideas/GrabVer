# GrabVer - Gradle Automatic Build Versioning Plugin
An plugin to generate the build number, version code and patch automatically.
Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>Android Studio
Automatic Incremental Gradle Versioning</a>. Customized into library with Suffix and Auto-Reset features.</p>
> Easy to apply, it _works with any project type._

## Concept
_Major_: User defined breaking changes.<br/>
_Minor_: User defined new features, but backwards compatible.<br/>
_Patch_: Auto generated backwards compatible bug fixes only.<br/>
_Suffix_: User defined value for versionName.

**Build** - increases at each build.<br/>
**Code** - increases at each release.<br/>
**Patch** - increases at each release, but it auto-resets back to 0 when _Minor_ or _Major_ version increments.</p>

 
## Installation
```
// 1. Run "gradlew install"
// 2. Configure dependencies
```
``` groovy
buildscript {
    repositories {
        // Publishing is in progress! Please, use local repository for now
        mavenLocal()
	}
    dependencies {
        classpath 'eu.davidea:grabver:1.0.0'
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
    suffix = 'RC2'
}
```

### 2. Grab your new version
``` groovy 
versioning.major
versioning.minor
versioning.patch
versioning.build
versioning.suffix
versioning.versionCode [.code]
versioning.versionName
versioning.getDate([format])
```

### 3. Run it
```
// To increase build only
gradle build
// To increase build, code and patch
// Patch is reset if major or minor is changed
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