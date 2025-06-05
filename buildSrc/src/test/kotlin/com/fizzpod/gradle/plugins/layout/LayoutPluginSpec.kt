/* (C) 2024-2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class LayoutPluginSpec {

    @TempDir
    lateinit var projectDir: File // Used by ProjectBuilder

    private lateinit var project: Project

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    fun `Create project with layout plugin`() {
        // when:
        project.pluginManager.apply(LayoutPlugin::class.java) // Apply by class

        // then:
        assertNotNull(project.tasks.findByName("createLayout"), "Task createLayout should exist")
        assertTrue(project.tasks.getByName("createLayout") is Task, "createLayout should be a Task")
        assertNotNull(project.extensions.findByName("layout"), "Layout extension should exist")
        assertTrue(project.extensions.findByName("layout") is LayoutPluginExtension, "Extension should be of correct type")
    }

    @Test
    fun `Create single file in layout`() {
        // setup:
        project.pluginManager.apply(LayoutPlugin::class.java)
        val extension = project.extensions.getByType(LayoutPluginExtension::class.java)
        extension.file("file1", null) // Using null for content as in original test

        // when:
        val task = project.tasks.getByName("createLayout")
        // Directly execute task actions (mimicking Spock test's approach)
        task.actions.forEach { action ->
            action.execute(task)
        }

        // then:
        val file = File(projectDir, "file1")
        assertTrue(file.exists(), "File file1 should exist in projectDir")
        assertTrue(file.isFile, "file1 should be a file")
    }

    @Test
    fun `Create single path in layout`() {
        // setup:
        project.pluginManager.apply(LayoutPlugin::class.java)
        val extension = project.extensions.getByType(LayoutPluginExtension::class.java)
        extension.path("path1")

        // when:
        val task = project.tasks.getByName("createLayout")
        // Directly execute task actions
        task.actions.forEach { action ->
            action.execute(task)
        }

        // then:
        val file = File(projectDir, "path1") // projectDir is the root for this test project
        assertTrue(file.exists(), "Path path1 should exist in projectDir")
        assertTrue(file.isDirectory, "path1 should be a directory")
    }
}
