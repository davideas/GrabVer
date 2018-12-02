/*
 * Copyright 2017-2019 Davidea Solutions Sprl
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

import nu.studer.java.util.OrderedProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskState

/**
 * major: User defined breaking changes.<br>
 * minor: User defined new features, but backwards compatible.<br>
 * patch: Optional, user defined value or Auto generated backwards compatible bug fixes only.<br>
 * preRelease: Optional, user defined value for versionName.<br>
 * saveOn: Optional, saving versioning file depends by the task-name specified here
 * (default: <i>compileJava, assembleDebug & assembleRelease</i>).
 *
 * <p><b>build</b> - increases at each build.<br>
 * <b>code</b> - increases at each release.<br>
 * <b>patch</b> - if not specified, auto-increases at each release, but it auto-resets back to 0 when Minor or Major version increments.</p>
 * <p>Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>https://andreborud.com/android-studio-automatic-incremental-gradle-versioning</a>.</p>
 * Customized into library with Suffix and Auto-Reset features.
 *
 * @author Davide Steduto
 * @since 19/05/2017
 */
class GrabVer implements Plugin<Project> {

    private static String[] RELEASE_TASKS = ["bundle", "grabverRelease"]
    private static String[] SKIP_TASKS = ["test", "clean", "grabverSkip"]
    private static String[] SAVE_TASKS = ["compileJava", "bundle", "assembleDebug", "assembleRelease"]

    void apply(Project project) {
        println("====== STARTED GrabVer v1.0.1")
        println("INFO - ProjectName '" + project.name + "'")

        project.task('grabverRelease') {
            // Dummy task to trigger release versioning (also used in unit test)
        }
        project.task('grabverSkip') {
            // Dummy task to skip versioning (also used in unit test)
        }

        // Create new empty versioning instance
        VersioningExtension versioning = project.extensions.create("versioning", VersioningExtension)

        // Module versioning
        String module = project.name
        String filename = 'version.properties'
        if (!project.rootProject.name.equalsIgnoreCase(module)) {
            println("INFO - Versioning module '" + module + "'")
            filename = module + File.separator + filename
        } else {
            println("INFO - Versioning root project '" + module + "'")
        }
        filename = project.rootDir.toString() + File.separator + filename
        println("INFO - Versioning filename '" + filename + "'")

        // Load properties file
        File versionFile = getFile(filename)
        OrderedProperties versionProps = loadProperties(project, versionFile)

        // Check runTasks
        List<String> runTasks = project.gradle.startParameter.taskNames
        println("INFO - runTasks=" + runTasks)

        if (shouldSkip(runTasks)) {
            println("INFO - Skipping on tasks=" + SKIP_TASKS)
            versioning.evaluated = true
        } else if (shouldIncrement(runTasks, module)) {
            // Increment depends on releases
            println("INFO - Running with release build => 'Code' version will auto increment")
            versioning.increment = 1
        } else {
            println("INFO - Running with normal build => 'Code' version unchanged")
        }

        project.gradle.taskGraph.afterTask { Task task, TaskState state ->
            if (task.project.name == project.name && (versioning.hasSaveTask(task.name) || hasDefaultSaveTask(task.name))) {
                if (state.failure) {
                    println("ERROR - " + project.name + ":" + task.name + " TaskState failed")
                } else {
                    println("INFO - " + project.name + ":" + task.name + " TaskState succeeded")
                    if (!shouldSkip(runTasks)) {
                        // Save new values
                        println("INFO - Saving versioning " + versioning)
                        versionProps.setProperty(VersionType.MAJOR.toString(), String.valueOf(versioning.major))
                        versionProps.setProperty(VersionType.MINOR.toString(), String.valueOf(versioning.minor))
                        versionProps.setProperty(VersionType.PATCH.toString(), String.valueOf(versioning.patch))
                        versionProps.setProperty(VersionType.PRE_RELEASE.toString(), versioning.preRelease != null ? versioning.preRelease : "")
                        versionProps.setProperty(VersionType.BUILD.toString(), String.valueOf(versioning.build))
                        versionProps.setProperty(VersionType.CODE.toString(), String.valueOf(versioning.code))
                        Writer writer = versionFile.newWriter()
                        versionProps.store(writer, null)
                        writer.close()
                    }
                }
                println("====== ENDED GrabVer\n")
            }
        }
    }

    private static boolean shouldSkip(List<String> runTasks) {
        for (String task : SKIP_TASKS) {
            if (runTasks.contains(task)) return true
        }
        return false
    }

    private static boolean shouldIncrement(List<String> runTasks, String module) {
        for (String task : RELEASE_TASKS) {
            if (runTasks.contains(task)) return true
        }
        if (runTasks.contains(":" + module + ":assembleRelease")) {
            return true
        }
        return false
    }

    private static boolean hasDefaultSaveTask(String taskName) {
        return SAVE_TASKS.contains(taskName)
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

    private static OrderedProperties loadProperties(Project project, File versionFile) {
        FileInputStream fis = new FileInputStream(versionFile)
        OrderedProperties versionProps = new OrderedProperties()
        versionProps.load(fis)
        project.versioning.loadVersions(versionProps)
        fis.close()

        return versionProps
    }

}
