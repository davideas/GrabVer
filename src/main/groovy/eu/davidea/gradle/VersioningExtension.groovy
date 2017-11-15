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

import nu.studer.java.util.OrderedProperties

class VersioningExtension {
    // Public values from user
    int major
    int minor
    int patch = -1
    String preRelease
    String dependsOn
    // Private values from properties
    private String propPreRelease
    private int propMajor
    private int propMinor
    private int propPatch
    private int build
    private int code
    // Only GrabVer can access these properties
    protected boolean evaluated = false
    protected int increment = 0

    private void evaluateVersions() {
        if (!evaluated) {
            // Always auto-increment Build
            println("INFO - Auto incrementing build number")
            build++
            // Auto-increment Code only in case of release
            if (increment > 0 ) println("INFO - Auto incrementing code version")
            code += increment
            // Auto reset Patch in case they differ or preRelease is set
            if (major != propMajor || minor != propMinor || isPreRelease()) {
                if (propMajor != 0 && major > propMajor && minor != 0) {
                    println("ERROR - Expected minor to be 0 if major has increased")
                    throw new IllegalArgumentException("Inconsistent minor value: major has changed but minor is not 0")
                }
                if (propMinor != 0 && minor > propMinor && patch > 0) {
                    println("ERROR - Expected patch to be 0 if minor has increased")
                    throw new IllegalArgumentException("Inconsistent patch value: minor has changed but patch is not 0")
                }
                if (patch < 0) {
                    println("INFO - Auto resetting patch version")
                    patch = 0
                }
            } else if (increment > 0 && patch < 0) {
                println("INFO - Auto incrementing patch version")
                // Auto-increment Patch if Major or Minor do not differ from user
                patch = propPatch
                patch += increment
            }
            propPreRelease = preRelease
            evaluated = true
        }
    }

    protected void loadVersions(OrderedProperties versionProps) {
        // Load current values from properties file
        propMajor = Integer.valueOf(versionProps.getProperty(VersionType.MAJOR.toString(), "0"))
        propMinor = Integer.valueOf(versionProps.getProperty(VersionType.MINOR.toString(), "0"))
        propPatch = Integer.valueOf(versionProps.getProperty(VersionType.PATCH.toString(), "0"))
        propPreRelease = versionProps.getProperty(VersionType.PRE_RELEASE.toString(), "")
        build = Integer.valueOf(versionProps.getProperty(VersionType.BUILD.toString(), "0"))
        code = Integer.valueOf(versionProps.getProperty(VersionType.CODE.toString(), "0"))
        println("INFO - Current versioning: " + toStringCurrent())
    }

    private isPreRelease() {
        return preRelease != null && !preRelease.trim().empty
    }

    boolean hasDependingTask() {
        return dependsOn != null && !dependsOn.isEmpty()
    }

    int getMajor() {
        // Keep user value
        return major
    }

    int getMinor() {
        // Keep user value
        return minor
    }

    int getPatch() {
        evaluateVersions()
        return patch < 0 ? propPatch : patch
    }

    int getBuild() {
        evaluateVersions()
        return build
    }

    int getCode() {
        evaluateVersions()
        return code
    }

    /**
     * @return will output {@code major.minor.patch[-preRelease]}
     */
    String getName() {
        evaluateVersions()
        return (major + "." + minor + "." + (patch < 0 ? propPatch : patch) + (isPreRelease() ? "-" + preRelease : ""))
    }

    /**
     * @return will output {@code major.minor.patch[-preRelease] #build built on date}
     */
    String getFullVersionName() {
        return (getName() + " #" + build + getBuiltOn())
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