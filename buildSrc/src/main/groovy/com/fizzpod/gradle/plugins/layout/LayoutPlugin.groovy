package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LayoutPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPlugin.class);
	
		void apply(Project project) {
			project.extensions.create("layout", LayoutPluginExtension);
//			project.task([group: 'layout'], 'writeGitignore') << {
//				GitignoreFile gitignoreFile = new GitignoreFile();
//				LOGGER.info(".gitignore contents before writing: ");
//				LOGGER.info(gitignoreFile.getContents(project.getRootDir()));
//				gitignoreFile.writeContents(project.getRootDir(), project.gitignore.ignores);
//				LOGGER.info(".gitignore contents after writing: ");
//				LOGGER.info(gitignoreFile.getContents(project.getRootDir()));
//			}
//			
//			project.task([group: 'gitignore'], 'displayGitignore') << {
//				String contents = new GitignoreFile().getContents(project.getRootDir());
//				if(contents) {
//					println contents;
//				} else {
//					println ".gitignore file is either empty or does not exist.";
//				}
//			}
		}
}
