package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LayoutPath {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPath)

	def path
	
	LayoutPath(def path) {
		this.path = path
	}
	
	public File write(def root) {
		
		def layoutDir = new File(root, path)
		if(!layoutDir.exists()) {
			LOGGER.info("Creating directory {}", layoutDir);
			layoutDir.mkdirs()
		} else {
		LOGGER.info("Skipping directory {} as it already exists", layoutDir);
		}
		return layoutDir
	}
	
}
