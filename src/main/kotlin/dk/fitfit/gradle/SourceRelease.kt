package dk.fitfit.gradle

import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class SourceRelease : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("release", ReleaseTask::class.java)
    }
}

open class ReleaseTask : DefaultTask() {
    @TaskAction
    fun release() {
        val gitWorkingDirectory = project.layout.projectDirectory.asFile
        val git = Git.open(gitWorkingDirectory)

        val clean = git.status().call().isClean
        if (!clean) {
            throw IllegalStateException("Working directory isn't clean")
        }

        // TODO: Execute test task... Unless skip tests
        // TODO: Bump version
        // TODO: Commit
        // TODO: Tag
        // TODO: Merge current branch into /release
        // TODO: Push /release
        // TODO: Checkout previous branch
    }
}
