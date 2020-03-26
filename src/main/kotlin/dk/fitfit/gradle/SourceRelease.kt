package dk.fitfit.gradle

import dk.fitfit.gradle.Version.Fraction.MINOR
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File

class SourceRelease : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("release", ReleaseTask::class.java)
    }
}

open class ReleaseTask : DefaultTask() {
    val versionPropertyFile = "build.gradle"
    val bumpStrategy = MINOR

    @TaskAction
    fun release() {
        val gitWorkingDirectory = project.layout.projectDirectory.asFile
        val git = Git.open(gitWorkingDirectory)

        val clean = git.status().call().isClean
        if (!clean) {
            throw IllegalStateException("Working directory isn't clean")
        }

        // TODO: Execute test task... Unless skip tests

        // Bump version
        val version = bumpVersion()

        // TODO: Commit
        // TODO: Tag
        // TODO: Merge current branch into /release
        // TODO: Push /release
        // TODO: Checkout previous branch
    }

    private fun bumpVersion(): Version {
        val file = File(versionPropertyFile)
        val versionPropertyFileContent = file.readText()
        val regex = "version = '(.*)'".toRegex()

        val matchResult = regex.find(versionPropertyFileContent)
        val versionString = matchResult?.destructured?.component1()

        val version = if (versionString != null) {
            Version.of(versionString).bump(bumpStrategy)
        } else {
            throw IllegalStateException("Unable to extract version")
        }

        // Replace version in file
        val replaceFirst = versionPropertyFileContent.replaceFirst(regex, "version = \'$version\'")
        file.writeText(replaceFirst)
        return version
    }
}
