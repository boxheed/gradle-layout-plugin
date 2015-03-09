package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LayoutPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPlugin.class);
	
	void apply(Project project) {
		project.extensions.create("layout", LayoutPluginExtension);
		project.task([group: 'layout'], 'createLayout') << {
			project.layout.layouts.each {
				it.write(project.rootDir)
			}
		}
	}
}
