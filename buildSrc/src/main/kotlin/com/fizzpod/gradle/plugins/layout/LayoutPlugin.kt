/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LayoutPlugin : Plugin<Project> {

    companion object {
        // Using LayoutPlugin::class.java for consistency, though just LayoutPlugin.javaClass could also work in some contexts
        private val LOGGER: Logger = LoggerFactory.getLogger(LayoutPlugin::class.java)
    }

    override fun apply(project: Project) {
        val layoutPluginExtension = project.extensions.create("layout", LayoutPluginExtension::class.java)

        project.task("createLayout") { task ->
            task.group = "layout"
            task.description = "Creates the project layout based on the defined configurations." // Added description
            task.doLast {
                LOGGER.info("Executing createLayout task for project ${project.path}")
                if (layoutPluginExtension.layouts.isEmpty()) {
                    LOGGER.info("No layouts defined in the 'layout' extension. Nothing to create.")
                } else {
                    layoutPluginExtension.layouts.forEach { layoutItem ->
                        try {
                            layoutItem.write(project.rootDir) // Using project.rootDir as in the original Groovy
                        } catch (e: Exception) {
                            LOGGER.error("Error writing layout item: ${e.message}", e)
                            // Optionally, rethrow to fail the task if any item fails
                            // throw GradleException("Error writing layout item", e)
                        }
                    }
                    LOGGER.info("Finished creating layout for project ${project.path}")
                }
            }
        }
    }
}
