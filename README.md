# GrabVer - Gradle Automatic Build Versioning Plugin
Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>https://andreborud.com/android-studio-automatic-incremental-gradle-versioning</a></p>
Customized into library with Suffix and Auto-Reset features. Works with any project type.

### Concept
**Major**: User defined breaking changes<br/>
**Minor**: User defined new features, but backwards compatible<br/>
_Patch_: Auto generated backwards compatible bug fixes only<br/>
**Suffix**: User defined value for versionName

**Build** - increases at each build<br/>
**Code** - increases at each release<br/>
**Patch** - increases at each release, but it auto-resets back to 0 when _Minor_ or _Major_ version increments.</p>

 
## Installation
``` groovy
buildscript {
	repositories {
	    // Not published yet! So use local repository
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
    // Optional (any string)
    suffix = 'RC2'
}
```

### 2. Grab new version
``` groovy 
versioning.versionCode
versioning.versionName
```

# Contributions
Everybody is welcome to improve existing solution.

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