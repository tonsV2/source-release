package dk.fitfit.gradle

import dk.fitfit.gradle.Version.Fraction.MINOR
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File

class SourceReleasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("release", ReleaseTask::class.java)
    }
}

open class ReleaseTask : DefaultTask() {
    val versionPropertyFile = "build.gradle"
    val bumpStrategy = MINOR
    val releaseBranch = "release"

    private lateinit var git: Git
    private lateinit var version: Version
    private lateinit var currentBranch: String

    @TaskAction
    fun release() {
        val gitWorkingDirectory = project.layout.projectDirectory.asFile
        git = Git.open(gitWorkingDirectory)

        assertCleanWorkingDirectory()
        runTests()
        saveCurrentBranch()
        bumpVersion()
        commit()
        tag()
        mergeCurrentIntoRelease()
        pushCurrentBranch()
        checkoutPreviousBranch()
    }

    private fun assertCleanWorkingDirectory() {
        val clean = git.status().call().isClean
        if (!clean) {
            throw IllegalStateException("Working directory isn't clean")
        }
    }

    private fun runTests() {
        val testTaskName = "test"
        if (project.tasks.any { it.name == testTaskName }) {
            project.tasks.getByName(testTaskName).doLast {}
        }
    }

    private fun saveCurrentBranch() {
        currentBranch = git.repository.branch
    }

    private fun bumpVersion() {
        val file = File(versionPropertyFile)
        val versionPropertyFileContent = file.readText()
        val regex = "version = '(.*)'".toRegex()

        val matchResult = regex.find(versionPropertyFileContent)
        val versionString = matchResult?.destructured?.component1()

        version = if (versionString != null) {
            Version.of(versionString).bump(bumpStrategy)
        } else {
            throw IllegalStateException("Unable to extract version")
        }

        // Replace version in file
        val replaceFirst = versionPropertyFileContent.replaceFirst(regex, "version = \'$version\'")
        file.writeText(replaceFirst)
    }

    private fun commit() {
        with(git.commit()) {
            message = "Bump version to $version"
            setAll(true)
            call()
        }
    }

    private fun tag() {
        with(git.tag()) {
            name = "v$version"
            call()
        }
    }

    private fun mergeCurrentIntoRelease(): String? {
        val currentBranchRef = git.repository.findRef(currentBranch)
        git.checkout().setName(releaseBranch).call()

        val mergeCommand = git.merge()
        mergeCommand.include(currentBranchRef)
        mergeCommand.call()
        return currentBranch
    }

    private fun pushCurrentBranch() {
        git.push().setPushTags().call()
    }

    private fun checkoutPreviousBranch() {
        git.checkout().setName(currentBranch).call()
    }
}
