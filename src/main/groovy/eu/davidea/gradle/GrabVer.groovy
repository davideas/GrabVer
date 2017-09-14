/*
 * Copyright 2017 Davidea Solutions Sprl
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
 * Major: User defined breaking changes<br>
 * Minor: User defined new features, but backwards compatible<br>
 * Patch: Auto generated backwards compatible bug fixes only<br>
 * PreRelease: User defined value for versionName
 *
 * <p><b>Build</b> - increases at each build<br>
 * <b>Code</b> - increases at each release<br>
 * <b>Patch</b> - increases at each release, but it auto-resets back to 0 when Minor or Major version increments.</p>
 * <p>Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>https://andreborud.com/android-studio-automatic-incremental-gradle-versioning</a></p>
 * Customized into library with Suffix and Auto-Reset features.
 *
 * @since 19/05/2017
 * @author Davide Steduto
 */
class GrabVer implements Plugin<Project> {

    void apply(Project project) {
        println("====== STARTED GrabVer v0.4.1")
        println("INFO - ProjectName=" + project.name)

        project.task('grabverRelease') {
            // Dummy task to trigger release versioning (also used in unit test)
        }

        // Create new empty versioning instance
        VersioningExtension versioning = project.extensions.create("versioning", VersioningExtension)

        def runTasks = project.gradle.startParameter.taskNames
        println("INFO - runTasks=" + runTasks)

        // Module versioning
        String module = project.name
        String filename = 'version.properties'
        if (!project.rootProject.name.equalsIgnoreCase(module)) {
            println("INFO - Versioning Module '" + module + "'")
            filename = module + "\\" + filename
        } else {
            println("INFO - Versioning Root Project '" + module + "'")
        }
        filename = project.rootDir.toString() + "\\" + filename
        println("INFO - Versioning Filename: '" + filename + "'")

        // Load properties file
        File versionFile = getFile(filename)
        Properties versionProps = loadProperties(project, versionFile)

        if ('clean' in runTasks || 'test' in runTasks) {
            println("INFO - Skipping on Task clean & test")
            versioning.evaluated = true
        } else {
            // Increment depends on release
            if ('assemble' in runTasks || 'assembleRelease' in runTasks || 'grabverRelease' in runTasks) {
                println("INFO - Running with 'release' task: 'Code' version will auto increment")
                versioning.increment = 1
            } else {
                println("INFO - Running with normal build: 'Code' version unchanged")
            }
        }

        project.afterEvaluate {
            if (!('clean' in runTasks)) {
                // Save new values
                println("INFO - Saving Versioning: " + versioning)
                versionProps.setProperty(VersionType.MAJOR.toString(), String.valueOf(versioning.major))
                versionProps.setProperty(VersionType.MINOR.toString(), String.valueOf(versioning.minor))
                versionProps.setProperty(VersionType.PATCH.toString(), String.valueOf(versioning.patch))
                versionProps.setProperty(VersionType.PRE_RELEASE.toString(), versioning.preRelease)
                versionProps.setProperty(VersionType.BUILD.toString(), String.valueOf(versioning.build))
                versionProps.setProperty(VersionType.CODE.toString(), String.valueOf(versioning.code))
                Writer writer = versionFile.newWriter()
                versionProps.store(writer, null)
                writer.close()
            }
            println("====== ENDED GrabVer\n")
        }
    }

    private static File getFile(String fileName) {
        File versionFile = new File(fileName)
        if (!versionFile.canRead()) {
            println("WARN - Could not find properties file '" + fileName + "'")
            println("WARN - Auto-generating content properties with default values!")
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