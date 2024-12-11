/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LayoutPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPlugin.class)

	void apply(Project project) {
		def layoutPluginExtension = project.extensions.create("layout", LayoutPluginExtension)
		project.task([group: 'layout'], 'createLayout').doLast {
			layoutPluginExtension.layouts.each {
				it.write(project.rootDir)
			}
		}
	}
}
