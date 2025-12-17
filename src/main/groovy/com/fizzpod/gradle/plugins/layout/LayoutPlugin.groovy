package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Plugin
import org.gradle.api.Project

class LayoutPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create('layout', LayoutPluginExtension)

        project.tasks.register('createLayout') {
            doLast {
                extension.files.each { path, content ->
                    def file = new File(project.projectDir, path)
                    if (!file.exists()) {
                        file.parentFile.mkdirs()
                        file.createNewFile()
                        if (content != null) {
                            file.text = content.call()
                        }
                    }
                }
                extension.paths.each { path ->
                    def dir = new File(project.projectDir, path)
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                }
            }
        }
    }
}
