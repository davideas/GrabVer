/*
 * Copyright 2017-2024 Davidea Solutions Sprl
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

import static eu.davidea.gradle.ConsoleColors.*

/**
 * @author Davide Steduto
 * @since 19/05/2017
 */
@SuppressWarnings("GroovyAssignabilityCheck")
class VersioningExtension {
    // Public values from user
    int major = 1
    int minor
    int patch = -1
    String preRelease
    String incrementOn
    String saveOn
    // Private values from properties
    private String propPreRelease
    private int propMajor
    private int propMinor
    private int propPatch
    private int build
    private int code
    // Only GrabVer can access these properties
    protected boolean evaluated = false
    protected boolean isRelease = false
    // Plugin reference
    protected GrabVer grabver

    protected void evaluateVersion() {
        // Evaluate once new version
        if (!evaluated) {
            evaluated = true
            // Read once user extension configuration
            if (!grabver.readUserConfiguration()) {
                return
            }
            String version = "version: ${bold("${major}.${minor}${patch < 0 ? "" : ".${patch}"}${isPreRelease() ? "-${preRelease}" : ""}")}"
            String tasks = (incrementOn != null ? ", incrementOn: ${bold(incrementOn)}" : "") + (saveOn != null ? ", saveOn: ${bold(saveOn)}" : "")
            println("INFO - Evaluating user values: {${version}${tasks}}")
            // Auto reset Patch in case they differ or preRelease is set
            if (major != propMajor || minor != propMinor || isPreRelease()) {
                if (!grabver.firstRun && propMajor != 0 && major > propMajor && minor != 0) {
                    println(styler(RED, "ERROR - Expected minor to be 0 if major has increased"))
                    throw new IllegalArgumentException("Inconsistent minor value: major has changed but minor is not 0")
                }
                if (!grabver.firstRun && propMinor != 0 && minor > propMinor && patch > 0) {
                    println(styler(RED, "ERROR - Expected patch to be 0 if minor has increased"))
                    throw new IllegalArgumentException("Inconsistent patch value: minor has changed but patch is not 0")
                }
                if (patch < 0) {
                    grabver.printDebug("Auto resetting patch version")
                    patch = 0
                }
            } else if (isRelease && patch < 0) {
                grabver.printDebug("Auto incrementing patch version")
                // Auto-increment Patch if Major or Minor do not differ from user
                patch = propPatch + 1
            }
            // Always auto-increment build number
            grabver.printDebug("Auto incrementing build number")
            build++
            // Auto-increment Code only in case of release
            if (isRelease) {
                grabver.printDebug("Auto incrementing code version")
                code += 1
            }
            propPreRelease = preRelease
            grabver.printDebug("Evaluation complete: ${bold(toString())}")
        }
    }

    protected void loadProperties(OrderedProperties versionProps, boolean silent) {
        // Load current values from properties file
        propMajor = Integer.valueOf(versionProps.getProperty(VersionType.MAJOR.toString(), "1"))
        propMinor = Integer.valueOf(versionProps.getProperty(VersionType.MINOR.toString(), "0"))
        propPatch = Integer.valueOf(versionProps.getProperty(VersionType.PATCH.toString(), "0"))
        propPreRelease = versionProps.getProperty(VersionType.PRE_RELEASE.toString(), "")
        build = Integer.valueOf(versionProps.getProperty(VersionType.BUILD.toString(), "0"))
        code = Integer.valueOf(versionProps.getProperty(VersionType.CODE.toString(), "1"))
        if (!silent) {
            println("INFO - Current version: " + bold(toStringCurrent()))
        }
    }

    private isPreRelease() {
        return preRelease != null && !preRelease.trim().empty
    }

    int getMajor() {
        evaluateVersion()
        // Keep user value
        return major
    }

    int getMinor() {
        evaluateVersion()
        // Keep user value
        return minor
    }

    int getPatch() {
        evaluateVersion()
        return patch < 0 ? propPatch : patch
    }

    int getBuild() {
        evaluateVersion()
        return build
    }

    int getCode() {
        evaluateVersion()
        return code
    }

    /**
     * @return "<code>major.minor.patch[-preRelease]</code>"
     */
    String getName() {
        evaluateVersion()
        return (major + "." + minor + "." + (patch < 0 ? propPatch : patch) + (isPreRelease() ? "-" + preRelease : ""))
    }

    /**
     * @return "<code>major.minor.patch[-preRelease] #buildNr built on yyyy.MM.dd</code>"
     */
    String getFullName() {
        return getName() + " #" + build + getBuiltOn()
    }

    static String getBuiltOn() {
        return " built on " + getDate()
    }

    static String getDate() {
        return getDate('yyyy.MM.dd')
    }

    static String getDate(String format) {
        Date date = new Date()
        return date.format(format)
    }

    String toStringCurrent() {
        return propMajor + "." + propMinor + "." + propPatch +
                (propPreRelease != null && !propPreRelease.isEmpty() ? "-" + propPreRelease : "") +
                " #" + build +
                " code=" + code
    }

    String toString() {
        return major + "." + minor + "." + (patch < 0 ? propPatch : patch) +
                (isPreRelease() ? "-" + preRelease : "") +
                " #" + build +
                " code=" + code
    }

}
