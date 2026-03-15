/*
 * Copyright 2017-2026 Davidea Solutions Srl
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
 * <b>incrementBuild</b>: Optional, set to false to disable build number auto-increment (default: true).<br>
 * <b>incrementOn</b>: Optional, custom task name to trigger the increase of the version and save
 * (default: <code>assembleRelease, bundleRelease, grabverRelease</code>).<br>
 * <b>saveOn</b>: <i>Deprecated, it will be removed in next major release.</i><br>
 * Since {@code incrementOn} now also triggers save, and {@code incrementBuild} controls whether build
 * number increments on debug builds, there is no remaining scenario where a custom save task is needed.
 *
 * <br><br><u>Calculation</u>:
 * <p><b>build</b> - increases at each build.<br>
 * <b>code</b> - increases at each release.<br>
 * <b>patch</b> - if not specified, auto-increases at each release, but it auto-resets back to 0 when Minor or Major
 * version increments or if PreRelease is set.</p>
 *
 * <br><u>Save</u>:
 * <p>The version file is saved when a save task is detected and at least one of the following is true:
 * build number was incremented, a release task was detected, or user changed major/minor/patch/preRelease.</p>
 * <br>Inspired from <a href='https://andreborud.com/android-studio-automatic-incremental-gradle-versioning/'>https://andreborud.com/android-studio-automatic-incremental-gradle-versioning</a>
 * <br>Customized into library with Suffix and Auto-Reset features.
 * <br><br>
 *
 * @author Davide Steduto
 * @since 19/05/2017
 */
class GrabVer implements Plugin<Project> {

    private static String GRABVER_VERSION = "2.1.0"
    private static String VERSIONING_FILENAME = 'version.properties'
    // Capitalized values (Debug, Release) are suffixes matched with assemble*/bundle* prefixes, others are exact task names
    private static String[] TASK_PREFIXES = ["assemble", "bundle"]
    private static String[] RELEASE_TASKS = ["Release", "grabverRelease"]
    private static String[] SAVE_TASKS = ["build", "jar", "war", "explodedWar", "grabverRelease", "Debug", "Release"]

    // Extension reference
    private VersioningExtension versioning
    // Internal references
    private File versionFile
    private OrderedProperties versionProps
    protected Project project
    protected boolean firstRun = false
    protected static boolean debug = false

    void apply(Project project) {
        project.tasks.register('grabverRelease') {
            // Dummy task to force release versioning calculation (also used in unit test)
            group = 'versioning'
            description = 'Triggers release versioning (increments patch and code)'
        }
        project.tasks.register('grabverVerbose') {
            // Dummy task to log/display debug steps
            group = 'versioning'
            description = 'Enables verbose logging for versioning'
        }

        // Create new empty versioning instance
        this.versioning = project.extensions.create("versioning", VersioningExtension)
        this.versioning.grabver = this
        this.project = project

        // Evaluate monitored tasks: only track user-invoked tasks that trigger save
        Set<Task> monitoredTasks = new HashSet<>()
        Set<String> runTaskNames = project.gradle.startParameter.taskNames
                .collect { getTaskName(it) }.toSet()
        project.gradle.taskGraph.afterTask { Task task ->
            if ((task.project.name == project.rootProject.name || task.project.name == project.name) &&
                    runTaskNames.contains(task.name) &&
                    (task.name == versioning.saveOn ||
                    task.name == versioning.incrementOn ||
                    matchesTask(task.name, SAVE_TASKS)) &&
                    !monitoredTasks.contains(task)) {
                monitoredTasks.add(task)
            }
        }

        // Gradle build complete
        project.gradle.buildFinished() { BuildResult result ->
            println("") // Print empty line
            if (firstRun || !monitoredTasks.isEmpty()) {
                println(bold("> Module: ${project.name}"))
                for (Task task in monitoredTasks) {
                    String state = task.state.failure
                            ? styler(RED, 'FAILED')
                            : task.state.skipMessage != null
                                    ? styler(YELLOW, task.state.skipMessage)
                                    : styler(GREEN, 'EXECUTED')
                    println("Task: ${task.name} ${state}")
                }
                // Save version file if any value changed: build incremented, release detected, or user changes
                if (versioning.incrementBuild || versioning.isRelease || versioning.hasUserChanges()) {
                    saveFile()
                } else {
                    println("No version changes, skipping save")
                }
            } else if (result.failure != null) {
                println(bold("> Module: ${project.name}"))
                printError(result.failure.getLocalizedMessage())
            } else if (!versioning.evaluated) {
                println(styler(GRAY, "> GrabVer - ${project.name}: Plugin applied, but no versioning property was accessed."))
            }
        }
    }

    /**
     * Reads user configuration and determines if versioning should be evaluated.
     * <p>Called when user accesses any extension attribute (e.g., versioning.name).
     * Checks if any save tasks are present to decide whether to run in active mode
     * (with logging) or silent mode (load existing values only).</p>
     *
     * @return true if versioning should be saved (save task detected or first run), false otherwise.
     */
    protected boolean readUserConfiguration() {
        List<String> runTasks = project.gradle.startParameter.taskNames + project.defaultTasks
        debug = runTasks.contains("grabverVerbose")
        printDebug("runTasks=" + runTasks)
        printDebug("saveOn=" + versioning.saveOn)
        if (versioning.saveOn) {
            printWarn("'saveOn' is deprecated and it will be removed in next major release, use 'incrementOn' instead which now also triggers save.")
        }

        // Silent evaluation looking for activation/save tasks
        if (!shouldSave(runTasks, project.name, versioning)) {
            // Load existing properties file to provide last values
            loadProperties(true)
            runTasks.isEmpty()
                    ? println(styler(GRAY, "> GrabVer - No RunTask specified. Is Gradle syncing?"))
                    : println(styler(GRAY, "> GrabVer - No save task detected (current: ${versioning.toStringCurrent()})"))
            return firstRun
        }

        // Silent evaluation done. If passes, at least one save task was detected.
        printDebug("Save task detected")

        // Plugin info
        println(bold("> Plugin GrabVer v${GRABVER_VERSION}"))

        // Load existing properties file
        loadProperties(false)

        // Patch and Code increment depending on release task
        boolean isAndroid = project.plugins.hasPlugin("com.android.application") ||
                            project.plugins.hasPlugin("com.android.library")
        if (isRelease(runTasks, project.name, versioning)) {
            versioning.isRelease = true
            printInfo("${styler(BLUE, "release")} build detected" + (isAndroid ? " => 'Code' version will auto increment" : ""))
        } else {
            printInfo("Running ${styler(BLUE, "debug")} build" + (isAndroid ? " => 'Code' version remains unchanged" : ""))
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
        String filename = this.project.projectDir.absolutePath + File.separator + VERSIONING_FILENAME
        File file = new File(filename)
        if (!file.canRead()) {
            this.firstRun = true
            printWarn("Creating new properties file ${filename}")
            file.createNewFile()
        } else if (!silent) {
            printDebug("Versioning file ${filename}")
        }
        return file
    }

    /**
     * Saves new values to properties file.
     */
    private void saveFile() {
        if (versionProps == null) {
            printWarn("Cannot save version: no evaluation performed. Use versioning.name or similar in your build script.")
            return
        }
        versionProps.setProperty(VersionType.MAJOR.toString(), String.valueOf(versioning.major))
        versionProps.setProperty(VersionType.MINOR.toString(), String.valueOf(versioning.minor))
        versionProps.setProperty(VersionType.PATCH.toString(), String.valueOf(versioning.patch))
        versionProps.setProperty(VersionType.PRE_RELEASE.toString(), versioning.preRelease != null ? versioning.preRelease : "")
        versionProps.setProperty(VersionType.BUILD.toString(), String.valueOf(versioning.build))
        versionProps.setProperty(VersionType.CODE.toString(), String.valueOf(versioning.rawCode))
        Writer writer = versionFile.newWriter()
        versionProps.store(writer, null)
        writer.close()
        println("Saved version: ${bold(versioning.toString())}")
    }

    private static boolean shouldSave(List<String> runTasks, String project, VersioningExtension versioning) {
        for (String task in runTasks) {
            String projectName = getProjectName(task, project)
            task = getTaskName(task)
            if (project == projectName && (
                    task == versioning.saveOn ||
                    task == versioning.incrementOn ||
                    matchesTask(task, SAVE_TASKS))) {
                return true
            }
        }
        return false
    }

    private static boolean isRelease(List<String> runTasks, String project, VersioningExtension versioning) {
        for (String task in runTasks) {
            String projectName = getProjectName(task, project)
            task = getTaskName(task)
            if (project == projectName && (
                    task == versioning.incrementOn || matchesTask(task, RELEASE_TASKS))) {
                return true
            }
        }
        return false
    }

    private static boolean matchesTask(String task, String[] patterns) {
        for (String pattern in patterns) {
            // Exact match
            if (task == pattern) {
                return true
            }
            // For tasks with prefixes: match assemble*/bundle* tasks ending with Debug/Release
            if (pattern == "Debug" || pattern == "Release") {
                for (String prefix in TASK_PREFIXES) {
                    if (task.startsWith(prefix) && task.endsWith(pattern)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private static String getProjectName(String task, String project) {
        int firstIndex = task.indexOf(":")
        int lastIndex = task.lastIndexOf(":")
        return (firstIndex >= 0 && lastIndex > 0) ? task.substring(firstIndex + 1, lastIndex) : project
    }

    private static String getTaskName(String task) {
        int lastIndex = task.lastIndexOf(":")
        return (lastIndex > 0) ? task.substring(lastIndex + 1) : task
    }

    protected static void printDebug(String message) {
        if (debug) {
            println("DEBUG - ${message}")
        }
    }

    protected static void printInfo(String message) {
        println("INFO - ${message}")
    }

    protected static void printWarn(String message) {
        println(styler(YELLOW,"WARN - ${message}"))
    }

    protected static void printError(String message) {
        println(styler(RED,"ERROR - ${message}"))
    }
}
