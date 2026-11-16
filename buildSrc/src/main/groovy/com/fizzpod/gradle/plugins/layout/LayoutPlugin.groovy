/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LayoutPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPlugin.class)

	void apply(Project project) {
		def layoutPluginExtension = project.extensions.create("layout", LayoutPluginExtension)
		layoutPluginExtension.project = project
		def applyTask = project.task([group: 'layout'], 'applyLayout').doLast {
			layoutPluginExtension.write(project.rootDir)
		}
		project.task([group: 'layout', description: 'Deprecated alias for applyLayout.'], 'createLayout') {
			dependsOn(applyTask)
			doLast {
				LOGGER.warn("Task 'createLayout' is deprecated; use 'applyLayout' instead.")
			}
		}
		project.task([group: 'layout'], 'checkLayout').doLast {
			List<String> issues = layoutPluginExtension.check(project.rootDir)
			if (!issues.isEmpty()) {
				throw new GradleException("Layout check failed. Run './gradlew applyLayout' to update:\n - " + issues.join("\n - "))
			}
		}
	}
}
