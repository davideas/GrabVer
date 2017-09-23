package eu.davidea.gradle

import nu.studer.java.util.OrderedProperties
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.junit.Assert

/**
 * @author Davide
 * @since 19/05/2017
 */
class GrabVerTest {

    private Project project
    private static final PLUGIN_ID = 'eu.davidea.grabver'

    @Before
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(new File(".")).build()
        simulateProperties(1, 1, 1, 20, 3)
    }

    @Test
    void testAutoIncrement_Release_NoVersionChange() throws Exception {
        List tasks = new ArrayList<>()
        tasks.add("grabverRelease")
        project.gradle.startParameter.setTaskNames(tasks)
        project.pluginManager.apply PLUGIN_ID
        project.versioning {
            major = 1
            minor = 1
        }
        printResults("[Release + NoVersionChange]")
        Assert.assertEquals("major check",1, project.versioning.major)
        Assert.assertEquals("minor check",1, project.versioning.minor)
        Assert.assertEquals("patch check",2, project.versioning.patch)
        Assert.assertEquals("build check",21, project.versioning.build)
        Assert.assertEquals("code check", 4, project.versioning.code)
    }

    @Test
    void testAutoIncrement_Release_VersionChange() throws Exception {
        List tasks = new ArrayList<>()
        tasks.add("grabverRelease")
        project.gradle.startParameter.setTaskNames(tasks)
        project.pluginManager.apply PLUGIN_ID
        project.versioning {
            major = 2
            minor = 0
        }
        printResults("[Release + VersionChange]")
        Assert.assertEquals("major check",2, project.versioning.major)
        Assert.assertEquals("minor check",0, project.versioning.minor)
        Assert.assertEquals("patch check",0, project.versioning.patch)
        Assert.assertEquals("build check",21, project.versioning.build)
        Assert.assertEquals("code check", 4, project.versioning.code)
    }

    @Test
    void testAutoIncrement_NoRelease_NoVersionChange() throws Exception {
        project.pluginManager.apply PLUGIN_ID
        project.versioning {
            major = 1
            minor = 1
        }
        printResults("[NoRelease + NoVersionChange]")
        Assert.assertEquals("major check",1, project.versioning.major)
        Assert.assertEquals("minor check",1, project.versioning.minor)
        Assert.assertEquals("patch check",1, project.versioning.patch)
        Assert.assertEquals("build check",21, project.versioning.build)
        Assert.assertEquals("code check", 3, project.versioning.code)
    }

    @Test
    void testAutoIncrement_NoRelease_MinorVersionChange() throws Exception {
        project.pluginManager.apply PLUGIN_ID
        project.versioning {
            major = 1
            minor = 2
        }
        printResults("[NoRelease + MinorVersionChange]")
        Assert.assertEquals("major check",1, project.versioning.major)
        Assert.assertEquals("minor check",2, project.versioning.minor)
        Assert.assertEquals("patch check",0, project.versioning.patch)
        Assert.assertEquals("build check",21, project.versioning.build)
        Assert.assertEquals("code check", 3, project.versioning.code)
    }

    @Test(expected = IllegalArgumentException)
    void testAutoIncrement_NoRelease_MajorVersionChange() throws Exception {
        project.pluginManager.apply PLUGIN_ID
        project.versioning {
            major = 2
            minor = 2 // expected error due to inconsistency
        }
        printResults("[NoRelease + MajorVersionChange]")
    }

    @Test
    void testAutoIncrement_NoRelease_Suffix() throws Exception {
        project.pluginManager.apply PLUGIN_ID
        project.versioning {
            major = 1
            minor = 1
            preRelease = "RC2"
        }
        printResults("[NoRelease + Suffix]")
        Assert.assertEquals("minor check",1, project.versioning.minor)
        Assert.assertEquals("patch check",0, project.versioning.patch)
        Assert.assertEquals("build check",21, project.versioning.build)
        Assert.assertEquals("code check", 3, project.versioning.code)
    }

    /**
     * This method triggers the evaluation by invoking the versionName!
     *
     * @param title the title of the test
     */
    private void printResults(String title) {
        println("TEST - " + title)
        println("TEST - code=" + project.versioning.code)
        println("TEST - name=" + project.versioning.name)
        println("TEST - fullVersionName=" + project.versioning.fullVersionName)
        println("TEST - " + project.versioning)
    }

    private static void simulateProperties(int major, int minor, int patch, int build, int code) {
        // Load properties file
        File versionFile = getFile('version.properties')
        OrderedProperties versionProps = new OrderedProperties()
        FileInputStream fis = new FileInputStream(versionFile)
        versionProps.load(fis)
        fis.close()

        println("====== Auto-generating content properties for test")
        versionProps.setProperty(VersionType.MAJOR.toString(), String.valueOf(major))
        versionProps.setProperty(VersionType.MINOR.toString(), String.valueOf(minor))
        versionProps.setProperty(VersionType.PATCH.toString(), String.valueOf(patch))
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