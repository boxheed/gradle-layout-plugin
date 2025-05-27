/* (C) 2024-2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Action
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
            project.getTasksByName("createLayout", false) != null
            !project.getTasksByName("createLayout", false).isEmpty()
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
            println project.getTasksByName("createLayout", false)
            def task = project.getTasksByName("createLayout", false)[0]
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
            println project.getTasksByName("createLayout", false)
            def task = project.getTasksByName("createLayout", false)[0]
            task.actions.each { Action action ->
                action.execute(task)
           }
            
        then:
            File file = new File(temporaryFolder.getRoot(), "path1")
            file.exists()
            file.isDirectory()
    }
    
    
}
