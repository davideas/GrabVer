/*
 * Copyright 2017 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Major: User defined breaking changes<br/>
 * Minor: User defined new features, but backwards compatible<br/>
 * Patch: Auto generated backwards compatible bug fixes only<br/>
 * PreRelease: User defined value for versionName
 *
 * <p><b>Build</b> - increases at each build<br/>
 * <b>Code</b> - increases at each release<br/>
 * <b>Patch</b> - increases at each release, but it auto-resets back to 0 when Minor or Major version increments.</p>
 * <p>Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>https://andreborud.com/android-studio-automatic-incremental-gradle-versioning</a></p>
 * Customized into library with Suffix and Auto-Reset features.
 *
 * @since 19/05/2017
 * @author Davide Steduto
 */
// TODO: version.properties for each module
class GrabVer implements Plugin<Project> {

    void apply(Project project) {
        println("===== START GrabVer v0.2.0")

        VersioningExtension versioning = project.extensions.create("versioning", VersioningExtension)

        // Load properties file
        File versionFile = getFile('version.properties')
        Properties versionProps = loadProperties(project, versionFile)

        project.task('test_release') {
            // Dummy task to use with testing
        }
        // Increment depends on release
        def runTasks = project.gradle.startParameter.taskNames
        if ('assemble' in runTasks || 'release' in runTasks || 'assembleRelease' in runTasks || 'test_release' in runTasks) {
            println("INFO - Running with 'release' task: Code will auto increment")
            versioning.increment = 1
        } else {
            println("INFO - Running with normal build: Code remains unchanged")
        }

        project.afterEvaluate {
            // Save new values
            println("INFO - Saving Versioning: " + versioning)
            versionProps.setProperty(VersionType.MAJOR.toString(), String.valueOf(versioning.major))
            versionProps.setProperty(VersionType.MINOR.toString(), String.valueOf(versioning.minor))
            versionProps.setProperty(VersionType.PATCH.toString(), String.valueOf(versioning.patch))
            versionProps.setProperty(VersionType.BUILD.toString(), String.valueOf(versioning.build))
            versionProps.setProperty(VersionType.CODE.toString(), String.valueOf(versioning.code))
            Writer writer = versionFile.newWriter()
            versionProps.store(writer, null)
            writer.close()
            println("===== END GrabVer\n")
        }
    }

    private static File getFile(String fileName) {
        File versionFile = new File(fileName)
        if (!versionFile.canRead()) {
            println("WARNING - Could not find properties file '" + fileName + "'")
            println("WARNING - Auto-generating content properties with default values!")
            versionFile = new File(fileName)
            versionFile.createNewFile()
        }
        return versionFile
    }

    private static Properties loadProperties(Project project, File versionFile) {
        FileInputStream fis = new FileInputStream(versionFile)
        Properties versionProps = new Properties()
        versionProps.load(fis)
        project.versioning.loadVersions(versionProps)
        fis.close()

        return versionProps
    }

}