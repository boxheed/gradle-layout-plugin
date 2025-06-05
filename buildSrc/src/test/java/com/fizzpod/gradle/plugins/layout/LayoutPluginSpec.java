/* (C) 2024-2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LayoutPluginSpec {

    @TempDir
    Path projectDirPath; // Injected by JUnit 5

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(projectDirPath.toFile()).build();
    }

    @Test
    void createProjectWithLayoutPlugin() {
        // when:
        project.getPluginManager().apply(LayoutPlugin.class);

        // then:
        Task task = project.getTasks().findByName("createLayout");
        assertNotNull(task, "Task 'createLayout' should exist.");

        Object extension = project.getExtensions().findByName("layout");
        assertNotNull(extension, "Extension 'layout' should exist.");
        assertTrue(extension instanceof LayoutPluginExtension, "Extension should be of type LayoutPluginExtension.");
    }

    @Test
    void createSingleFileInLayout() {
        // setup:
        project.getPluginManager().apply(LayoutPlugin.class);
        LayoutPluginExtension extension = (LayoutPluginExtension) project.getExtensions().findByName("layout");
        assertNotNull(extension);
        extension.file("file1", null); // Passing null as the Supplier<String> as in the Groovy test

        // when:
        Task task = project.getTasks().findByName("createLayout");
        assertNotNull(task);

        // Execute task actions directly (mimicking Spock test's approach)
        // This is a unit test style, not a full functional test of the task execution via Gradle.
        task.getActions().forEach(action -> action.execute(task));

        // then:
        File file = new File(projectDirPath.toFile(), "file1"); // projectDirPath is the rootDir for this test project
        assertTrue(file.exists(), "File 'file1' should exist.");
        assertTrue(file.isFile(), "'file1' should be a file.");
    }

    @Test
    void createSinglePathInLayout() {
        // setup:
        project.getPluginManager().apply(LayoutPlugin.class);
        LayoutPluginExtension extension = (LayoutPluginExtension) project.getExtensions().findByName("layout");
        assertNotNull(extension);
        extension.path("path1");

        // when:
        Task task = project.getTasks().findByName("createLayout");
        assertNotNull(task);

        // Execute task actions directly
        task.getActions().forEach(action -> action.execute(task));

        // then:
        File folder = new File(projectDirPath.toFile(), "path1");
        assertTrue(folder.exists(), "Folder 'path1' should exist.");
        assertTrue(folder.isDirectory(), "'path1' should be a directory.");
    }
}
