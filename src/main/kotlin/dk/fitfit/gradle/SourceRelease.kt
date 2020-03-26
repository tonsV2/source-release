package dk.fitfit.gradle

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
        println("Hello World!")
    }
}
