package com.fizzpod.gradle.plugins.layout

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class LayoutPluginSpec extends Specification {

    def "plugin is applied correctly"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        try {
            project.pluginManager.apply LayoutPlugin
        } catch (Exception e) {
            e.printStackTrace()
            throw e
        }

        then:
        project.extensions.findByName("layout") != null
    }

    def "creates an empty file"() {
        given:
        def project = ProjectBuilder.builder().build()
        project.pluginManager.apply LayoutPlugin
        def layout = project.extensions.getByType(LayoutPluginExtension)
        layout.file "test-file.txt"

        when:
        def task = project.tasks.getByName("createLayout")
        task.actions.each { it.execute(task) }

        then:
        new File(project.projectDir, "test-file.txt").exists()
    }

    def "creates a directory"() {
        given:
        def project = ProjectBuilder.builder().build()
        project.pluginManager.apply LayoutPlugin
        def layout = project.extensions.getByType(LayoutPluginExtension)
        layout.path "test-dir"

        when:
        def task = project.tasks.getByName("createLayout")
        task.actions.each { it.execute(task) }

        then:
        new File(project.projectDir, "test-dir").isDirectory()
    }

    def "does not overwrite an existing file"() {
        given:
        def project = ProjectBuilder.builder().build()
        def existingFile = new File(project.projectDir, "test-file.txt")
        existingFile.text = "existing content"
        project.pluginManager.apply LayoutPlugin
        def layout = project.extensions.getByType(LayoutPluginExtension)
        layout.file "test-file.txt"

        when:
        def task = project.tasks.getByName("createLayout")
        task.actions.each { it.execute(task) }

        then:
        existingFile.text == "existing content"
    }
    def "creates a file with content"() {
        given:
        def project = ProjectBuilder.builder().build()
        project.pluginManager.apply LayoutPlugin
        def layout = project.extensions.getByType(LayoutPluginExtension)
        layout.file "test-file.txt", { "hello world" }

        when:
        def task = project.tasks.getByName("createLayout")
        task.actions.each { it.execute(task) }

        then:
        def createdFile = new File(project.projectDir, "test-file.txt")
        createdFile.exists()
        createdFile.text == "hello world"
    }

    def "creates a file with multi-line content"() {
        given:
        def project = ProjectBuilder.builder().build()
        project.pluginManager.apply LayoutPlugin
        def layout = project.extensions.getByType(LayoutPluginExtension)
        layout.file "test-file.txt", { """line 1
line 2""" }

        when:
        def task = project.tasks.getByName("createLayout")
        task.actions.each { it.execute(task) }

        then:
        def createdFile = new File(project.projectDir, "test-file.txt")
        createdFile.exists()
        createdFile.text == """line 1
line 2"""
    }
}
