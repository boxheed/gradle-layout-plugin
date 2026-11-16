/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LayoutPluginSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    
    
    def "Create project with layout plugin"() {
        setup:
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def layoutPlugin = new LayoutPlugin()
            layoutPlugin.apply(project)
        then: 
            !project.getTasksByName("applyLayout", false).isEmpty()
            !project.getTasksByName("createLayout", false).isEmpty()
            !project.getTasksByName("checkLayout", false).isEmpty()
            project.getTasksByName("createLayout", false)[0].taskDependencies.getDependencies(null).contains(project.getTasksByName("applyLayout", false)[0])
            project.getExtensions().findByName("layout") != null
    }
    
    def "Create single file in layout"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def layoutPlugin = new LayoutPlugin()
            layoutPlugin.apply(project)
            LayoutPluginExtension extension = project.getExtensions().findByName("layout")
            extension.file("file1", null)
        when:
            def task = project.getTasksByName("applyLayout", false)[0]
            task.actions.each { Action action ->
                action.execute(task)
           }
            
        then:
            File file = new File(temporaryFolder.getRoot(), "file1")
            file.exists()
            file.isFile()
    }
    
    def "Create single path in layout"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def layoutPlugin = new LayoutPlugin()
            layoutPlugin.apply(project)
            LayoutPluginExtension extension = project.getExtensions().findByName("layout")
            extension.path("path1")
        when:
            def task = project.getTasksByName("applyLayout", false)[0]
            task.actions.each { Action action ->
                action.execute(task)
           }
            
        then:
            File file = new File(temporaryFolder.getRoot(), "path1")
            file.exists()
            file.isDirectory()
    }

    def "checkLayout passes when layout matches"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            new LayoutPlugin().apply(project)
            LayoutPluginExtension extension = project.getExtensions().findByName("layout")
            extension.path("dir1")
            extension.file("README.md", [overwrite: false], { "initial" })
            extension.file("managed.txt", [overwrite: true], { "expected" })
            project.getTasksByName("applyLayout", false)[0].actions.each { Action action ->
                action.execute(project.getTasksByName("applyLayout", false)[0])
            }
            new File(temporaryFolder.getRoot(), "README.md").setText("customized", "UTF-8")
        when:
            def checkTask = project.getTasksByName("checkLayout", false)[0]
            checkTask.actions.each { Action action ->
                action.execute(checkTask)
            }
        then:
            noExceptionThrown()
    }

    def "checkLayout fails when overwrite file is out of sync or missing"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            new LayoutPlugin().apply(project)
            LayoutPluginExtension extension = project.getExtensions().findByName("layout")
            extension.path("missing-dir")
            extension.file("managed.txt", [overwrite: true], { "expected" })
            new File(temporaryFolder.getRoot(), "managed.txt").setText("stale", "UTF-8")
        when:
            def checkTask = project.getTasksByName("checkLayout", false)[0]
            checkTask.actions.each { Action action ->
                action.execute(checkTask)
            }
        then:
            GradleException ex = thrown()
            ex.message.contains("Missing directory: missing-dir")
            ex.message.contains("Out-of-sync file: managed.txt")
    }

    def "checkLayout skips check in offline mode"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            project.gradle.startParameter.offline = true
            new LayoutPlugin().apply(project)
            LayoutPluginExtension extension = project.getExtensions().findByName("layout")
            extension.url("https://example.invalid/nonexistent/layout.json")
            extension.file("missing.txt", [overwrite: true], { "hello" })
        when:
            def checkTask = project.getTasksByName("checkLayout", false)[0]
            checkTask.actions.each { Action action ->
                action.execute(checkTask)
            }
        then:
            noExceptionThrown()
    }
    
    
}
