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
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import static eu.davidea.gradle.ConsoleColors.*

/**
 * <u>User values</u>:<br>
 * <b>major</b>: User defined breaking changes.<br>
 * <b>minor</b>: User defined new features, but backwards compatible.<br>
 * <b>patch</b>: Optional, user defined value or Auto generated backwards compatible bug fixes only.<br>
 * <b>preRelease</b>: Optional, user defined value for versionName.<br>
 * <b>incrementOn</b>: Optional, custom task name to trigger the increase of the version
 * (default: <code>assembleRelease, bundleRelease, grabverRelease</code>).<br>
 * <b>saveOn</b>: Optional, custom task name for which you want to save the versioning file
 * (default: <code>build, assembleDebug, assembleRelease, bundleDebug, bundleRelease, grabverRelease, jar,
 * war, explodedWar</code>).
 *
 * <br><br><u>Calculation</u>:
 * <p><b>build</b> - increases at each build.<br>
 * <b>code</b> - increases at each release.<br>
 * <b>patch</b> - if not specified, auto-increases at each release, but it auto-resets back to 0 when Minor or Major
 * version increments or if PreRelease is set.</p>
 * <br>Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>https://andreborud.com/android-studio-automatic-incremental-gradle-versioning</a>
 * <br>Customized into library with Suffix and Auto-Reset features.
 * <br><br>
 *
 * @author Davide Steduto
 * @since 19/05/2017
 */
class GrabVer implements Plugin<Project> {

    private static String GRABVER_VERSION = "2.0.0"
    private static String[] RELEASE_TASKS = ["assembleRelease", "bundleRelease", "grabverRelease"]
    private static String[] SAVE_TASKS = ["build", "assembleDebug", "assembleRelease", "bundleDebug", "bundleRelease", "grabverRelease", "jar", "war", "explodedWar"]

    // Extension reference
    private VersioningExtension versioning
    // Internal references
    private Project project
    private File versionFile
    private OrderedProperties versionProps
    protected boolean isFirstRun = false

    void apply(Project project) {
        project.task('grabverRelease') {
            // Dummy task to force trigger release versioning (also used in unit test)
        }

        // Create new empty versioning instance
        this.versioning = project.extensions.create("versioning", VersioningExtension)
        this.versioning.grabver = this
        this.project = project

        // Evaluate success state on monitored tasks
        Set<Task> succeededTasks = new HashSet<>()
        project.gradle.taskGraph.afterTask { Task task ->
            if ((task.project.name == project.rootProject.name || task.project.name == project.name) &&
                    (task.name == versioning.saveOn || SAVE_TASKS.contains(task.name))) {
                if (task.state.failure) {
                    println("ERROR - ${project.name}:${task.name} ${styler(RED, 'FAILED')}")
                } else if (!succeededTasks.contains(task)) {
                    succeededTasks.add(task)
                }
            }
        }

        // Gradle build complete
        project.gradle.buildFinished() { BuildResult result ->
            println("") // Print empty line
            if (isFirstRun || !succeededTasks.isEmpty()) {
                println(bold("> Module: ${project.name}"))
                for (Task task in succeededTasks) {
                    String state = task.state.skipMessage != null
                            ? styler(YELLOW, task.state.skipMessage)
                            : styler(GREEN, 'EXECUTED')
                    println("Task: ${task.name} ${state}")
                }
                // Save new versioning
                saveFile()
            } else if (result.failure != null) {
                println(styler(RED, project.name.toUpperCase() + ' - ' + result.failure.getLocalizedMessage()))
            }
        }
    }

    /**
     * This plugin needs configuration evaluation done during the Gradle project evaluation
     * and before it completes.
     * This plugin works without specifying any custom task, but because Gradle provides only one way
     * to read the user values (at afterEvaluation time), it is too late for this plugin to wait such event.
     * <p>To overcome at this "issue", this function is being called at the first extension invocation
     * (user must grab one of the extension attribute).</p>
     *
     * @return true to allow the saving, false to deactivate the plugin in silent mode.
     */
    protected boolean readUserConfiguration() {
        List<String> runTasks = project.gradle.startParameter.taskNames

        // Silent evaluation looking for activation/save tasks
        if (!shouldSave(runTasks, project.name, versioning.saveOn)) {
            runTasks.isEmpty()
                    ? println(styler(YELLOW, "> GrabVer - No RunTask specified. Is Gradle syncing?"))
                    : println(styler(YELLOW, "> GrabVer - No save task detected"))
            // Load existing properties file to provide last values
            loadProperties(true)
            return isFirstRun
        }

        // Silent evaluation done. If passes, at least one save task was detected.
        // Plugin info
        String rootProject = project.rootProject.name
        println(bold("> Plugin GrabVer v${GRABVER_VERSION}"))
        println("INFO - Root project ${rootProject}")

        // Load existing properties file
        loadProperties(false)
        // Display extra info
        println("INFO - runTasks=" + runTasks)
        println("INFO - Save task detected") // Real detection was done in silent mode

        // Patch and Code increment depending on release task
        if (isRelease(runTasks, project.name, versioning)) {
            versioning.isRelease = true
            println("INFO - ${styler(BLUE, "Release")} build detected => 'Code' version will auto increment")
        } else {
            println("INFO - Running ${styler(BLUE, "normal")} build => 'Code' version remains unchanged")
        }
        return true
    }

    /**
     * Loads current values from properties file.
     */
    private void loadProperties(boolean silent) {
        this.versionFile = this.getFile(silent)
        this.versionProps = new OrderedProperties()
        FileInputStream fis = new FileInputStream(versionFile)
        versionProps.load(fis)
        versioning.loadProperties(versionProps, silent)
        fis.close()
    }

    private File getFile(boolean silent) {
        String rootProject = this.project.rootProject.name
        String module = project.name
        String filename = 'version.properties'

        // Root or Module versioning
        if (rootProject == module) {
            if (!silent) {
                println("INFO - Versioning root project ${module}")
            }
        } else {
            if (!silent) {
                println("INFO - Versioning module ${module}")
            }
            filename = module + File.separator + filename
        }
        filename = project.rootDir.toString() + File.separator + filename

        File file = new File(filename)
        if (!file.canRead()) {
            this.isFirstRun = true
            println(styler(YELLOW, "WARN - Could not find properties file ${filename}"))
            println(styler(YELLOW, "WARN - Creating new properties file!"))
            file.createNewFile()
        } else if (!silent) {
            println("INFO - Versioning file ${filename}")
        }
        return file
    }

    /**
     * Saves new values to properties file.
     */
    private void saveFile() {
        versionProps.setProperty(VersionType.MAJOR.toString(), String.valueOf(versioning.major))
        versionProps.setProperty(VersionType.MINOR.toString(), String.valueOf(versioning.minor))
        versionProps.setProperty(VersionType.PATCH.toString(), String.valueOf(versioning.patch))
        versionProps.setProperty(VersionType.PRE_RELEASE.toString(), versioning.preRelease != null ? versioning.preRelease : "")
        versionProps.setProperty(VersionType.BUILD.toString(), String.valueOf(versioning.build))
        versionProps.setProperty(VersionType.CODE.toString(), String.valueOf(versioning.code))
        Writer writer = versionFile.newWriter()
        versionProps.store(writer, null)
        writer.close()
        println("Saved version: ${bold(versioning.toString())}")
    }

    private static boolean shouldSave(List<String> runTasks, String project, String saveOn) {
        for (String task in runTasks) {
            String androidProject = getAndroidProject(task, project)
            task = getAndroidTask(task)
            if (project == androidProject && (task == saveOn || SAVE_TASKS.contains(task))) {
                return true
            }
        }
        return false
    }

    private static boolean isRelease(List<String> runTasks, String project, VersioningExtension versioning) {
        for (String task in runTasks) {
            String androidProject = getAndroidProject(task, project)
            task = getAndroidTask(task)
            if (project == androidProject && (
                    task.equalsIgnoreCase(versioning.incrementOn) || RELEASE_TASKS.contains(task))) {
                return true
            }
        }
        return false
    }

    private static String getAndroidProject(String task, String project) {
        int firstIndex = task.indexOf(":")
        int lastIndex = task.lastIndexOf(":")
        return (firstIndex >= 0 && lastIndex > 0) ? task.substring(firstIndex + 1, lastIndex) : project
    }

    private static String getAndroidTask(String task) {
        int lastIndex = task.lastIndexOf(":")
        return (lastIndex > 0) ? task.substring(lastIndex + 1) : task
    }

}
