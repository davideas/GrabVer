package eu.davidea.gradle

import nu.studer.java.util.OrderedProperties
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * @author Davide Steduto
 * @since 19/05/2017
 */
@SuppressWarnings(["GroovyAssignabilityCheck"])
class GrabVerTest {

    private Project project
    private static final PLUGIN_ID = 'eu.davidea.grabver'

    @Before
    void setup() {
        project = ProjectBuilder.builder().withProjectDir(new File(".")).build()
        simulateProperties(1, 1, 1, "", 20, 3)
    }

    // ==================== Skip versioning ====================

    @Test
    void skip_noRunTasks() throws Exception {
        applyPlugin([], { major = 1; minor = 1 })
        printResults("[Skip] No run tasks")
        assertVersion(1, 1, 1, 20, 3)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
    }

    @Test
    void skip_cleanOnly() throws Exception {
        applyPlugin(["clean"], { major = 1; minor = 1 })
        printResults("[Skip] Clean only")
        assertVersion(1, 1, 1, 20, 3)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
    }

    // ==================== Debug builds ====================

    @Test
    void debug_cleanBuild() throws Exception {
        applyPlugin(["clean", "build"], { major = 1; minor = 1 })
        printResults("[Debug] Clean build")
        assertVersion(1, 1, 1, 21, 3)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
        Assert.assertEquals("name", "1.1.1", project.versioning.name)
    }

    @Test
    void debug_assembleDebug() throws Exception {
        applyPlugin(["assembleDebug"], { major = 1; minor = 1 })
        printResults("[Debug] assembleDebug")
        assertVersion(1, 1, 1, 21, 3)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
    }

    @Test
    void debug_minorChange() throws Exception {
        applyPlugin(["assembleDebug"], { major = 1; minor = 2 })
        printResults("[Debug] Minor version change")
        assertVersion(1, 2, 0, 21, 3)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
        Assert.assertTrue("hasUserChanges", project.versioning.hasUserChanges())
    }

    @Test
    void debug_war() throws Exception {
        applyPlugin(["war"], { major = 1; minor = 1 })
        printResults("[Debug] War task")
        assertVersion(1, 1, 1, 21, 3)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
    }

    @Test
    void debug_customPatch() throws Exception {
        applyPlugin(["build"], { major = 1; minor = 1; patch = 9 })
        printResults("[Debug] Custom patch")
        assertVersion(1, 1, 9, 21, 3)
        Assert.assertTrue("hasUserChanges", project.versioning.hasUserChanges())
    }

    @Test
    void debug_preRelease() throws Exception {
        applyPlugin(["war"], { major = 1; minor = 1; preRelease = "RC2" })
        printResults("[Debug] PreRelease")
        assertVersion(1, 1, 0, 21, 3)
        Assert.assertEquals("name", "1.1.0-RC2", project.versioning.name)
    }

    // ==================== Release builds ====================

    @Test
    void release_bundleRelease_majorChange() throws Exception {
        applyPlugin(["bundleRelease"], { major = 2; minor = 0 })
        printResults("[Release] Major version change")
        assertVersion(2, 0, 0, 21, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
        Assert.assertEquals("name", "2.0.0", project.versioning.name)
    }

    @Test
    void release_assembleRelease_minorChange() throws Exception {
        applyPlugin(["assembleRelease"], { major = 1; minor = 2 })
        printResults("[Release] Minor version change")
        assertVersion(1, 2, 0, 21, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
    }

    @Test
    void release_grabverRelease_autoIncrementPatch() throws Exception {
        applyPlugin(["grabverRelease"], { major = 1; minor = 1 })
        printResults("[Release] Auto increment patch")
        assertVersion(1, 1, 2, 21, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
        Assert.assertEquals("name", "1.1.2", project.versioning.name)
    }

    // ==================== Flavor tasks ====================

    @Test
    void release_assembleFreeRelease() throws Exception {
        applyPlugin(["assembleFreeRelease"], { major = 1; minor = 1 })
        printResults("[Release] assembleFreeRelease")
        assertVersion(1, 1, 2, 21, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
    }

    @Test
    void release_bundleProRelease() throws Exception {
        applyPlugin(["bundleProRelease"], { major = 1; minor = 1 })
        printResults("[Release] bundleProRelease")
        assertVersion(1, 1, 2, 21, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
    }

    @Test
    void debug_assembleFreeDebug() throws Exception {
        applyPlugin(["assembleFreeDebug"], { major = 1; minor = 1 })
        printResults("[Debug] assembleFreeDebug")
        assertVersion(1, 1, 1, 21, 3)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
    }

    // ==================== incrementBuild flag ====================

    @Test
    void debug_incrementBuildDisabled_buildUnchanged() throws Exception {
        applyPlugin(["build"], { major = 1; minor = 1; incrementBuild = false })
        printResults("[Debug] incrementBuild=false")
        assertVersion(1, 1, 1, 20, 3)
        Assert.assertFalse("hasUserChanges", project.versioning.hasUserChanges())
    }

    @Test
    void release_incrementBuildDisabled_patchAndCodeStillIncrement() throws Exception {
        applyPlugin(["grabverRelease"], { major = 1; minor = 1; incrementBuild = false })
        printResults("[Release] incrementBuild=false")
        assertVersion(1, 1, 2, 20, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
    }

    // ==================== incrementOn ====================

    @Test
    void release_incrementOn_customTask() throws Exception {
        applyPlugin(["deploy"], { major = 1; minor = 1; incrementOn = "deploy" })
        printResults("[Release] incrementOn=deploy")
        assertVersion(1, 1, 2, 21, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
    }

    // ==================== saveOn (deprecated) ====================

    @Test
    void deprecated_saveOn_stillTriggersSave() throws Exception {
        applyPlugin(["clean"], { major = 1; minor = 1; saveOn = "clean" })
        printResults("[Deprecated] saveOn=clean")
        assertVersion(1, 1, 1, 21, 3)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
    }

    @Test
    void deprecated_saveOn_withIncrementOn() throws Exception {
        applyPlugin(["clean"], { major = 1; minor = 1; saveOn = "clean"; incrementOn = "clean" })
        printResults("[Deprecated] saveOn+incrementOn=clean")
        assertVersion(1, 1, 2, 21, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
    }

    // ==================== PreRelease from properties ====================

    @Test
    void debug_preReleaseChange() throws Exception {
        simulateProperties(1, 1, 1, "RC1", 20, 3)
        applyPlugin(["build"], { major = 1; minor = 1; preRelease = "RC2" })
        printResults("[Debug] PreRelease change RC1→RC2")
        assertVersion(1, 1, 0, 21, 3)
        Assert.assertEquals("name", "1.1.0-RC2", project.versioning.name)
        Assert.assertTrue("hasUserChanges", project.versioning.hasUserChanges())
    }

    @Test
    void release_preReleaseRemoved() throws Exception {
        simulateProperties(1, 1, 0, "RC1", 20, 3)
        applyPlugin(["grabverRelease"], { major = 1; minor = 1 })
        printResults("[Release] PreRelease removed")
        assertVersion(1, 1, 1, 21, 4)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
        Assert.assertEquals("name", "1.1.1", project.versioning.name)
        Assert.assertTrue("hasUserChanges", project.versioning.hasUserChanges())
    }

    // ==================== Major zero ====================

    @Test
    void debug_majorZero() throws Exception {
        simulateProperties(0, 1, 0, "", 0, 0)
        applyPlugin(["build"], { major = 0; minor = 1 })
        printResults("[Debug] Major zero")
        // Also ensure debug build gets code=1 but saves 0 if it was zero
        assertVersion(0, 1, 0, 1, 1)
        Assert.assertFalse("isRelease", project.versioning.isRelease)
        Assert.assertEquals("name", "0.1.0", project.versioning.name)
        Assert.assertEquals("code", 0, project.versioning.rawCode)
    }

    @Test
    void release_majorZero() throws Exception {
        simulateProperties(0, 1, 0, "", 3, 0)
        applyPlugin(["grabverRelease"], { major = 0; minor = 1 })
        printResults("[Release] Major zero")
        // Also ensure release build gets and saves code=1 if it was zero
        assertVersion(0, 1, 1, 4, 1)
        Assert.assertTrue("isRelease", project.versioning.isRelease)
        Assert.assertEquals("name", "0.1.1", project.versioning.name)
        Assert.assertEquals("code", 1, project.versioning.rawCode)
    }

    // ==================== Validation errors ====================

    @Test(expected = IllegalArgumentException)
    void error_majorChange_minorNotZero() throws Exception {
        applyPlugin(["assembleDebug"], { major = 2; minor = 2 })
        printResults("[Error] Major changed but minor != 0")
    }

    @Test(expected = IllegalArgumentException)
    void error_minorChange_patchNotZero() throws Exception {
        applyPlugin(["assembleDebug"], { major = 1; minor = 2; patch = 1 })
        printResults("[Error] Minor changed but patch != 0")
    }

    // ==================== Helpers ====================

    private void applyPlugin(List<String> tasks, Closure config) {
        project.gradle.startParameter.setTaskNames(tasks)
        project.pluginManager.apply PLUGIN_ID
        project.versioning(config)
    }

    private void assertVersion(int major, int minor, int patch, int build, int code) {
        Assert.assertEquals("major", major, project.versioning.major)
        Assert.assertEquals("minor", minor, project.versioning.minor)
        Assert.assertEquals("patch", patch, project.versioning.patch)
        Assert.assertEquals("build", build, project.versioning.build)
        Assert.assertEquals("code", code, project.versioning.code)
    }

    private void printResults(String title) {
        println()
        println("TEST - " + title)
        println("TEST - code=$project.versioning.code")
        println("TEST - name=$project.versioning.name")
        println("TEST - fullName: $project.versioning.fullName")
        println("TEST - New versioning: $project.versioning")
    }

    private static void simulateProperties(int major, int minor, int patch, String preRelease, int build, int code) {
        File versionFile = getFile('version.properties')
        OrderedProperties versionProps = new OrderedProperties()
        FileInputStream fis = new FileInputStream(versionFile)
        versionProps.load(fis)
        fis.close()

        versionProps.setProperty(VersionType.MAJOR.toString(), String.valueOf(major))
        versionProps.setProperty(VersionType.MINOR.toString(), String.valueOf(minor))
        versionProps.setProperty(VersionType.PATCH.toString(), String.valueOf(patch))
        versionProps.setProperty(VersionType.PRE_RELEASE.toString(), preRelease)
        versionProps.setProperty(VersionType.BUILD.toString(), String.valueOf(build))
        versionProps.setProperty(VersionType.CODE.toString(), String.valueOf(code))
        Writer writer = versionFile.newWriter()
        versionProps.store(writer, null)
        writer.close()
    }

    private static File getFile(String fileName) {
        File versionPropsFile = new File(fileName)
        if (!versionPropsFile.canRead()) {
            println("====== Could not find properties file '" + fileName + "', generating new one!")
            versionPropsFile = new File(fileName)
            versionPropsFile.createNewFile()
        }
        return versionPropsFile
    }
}
