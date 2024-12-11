/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger
import org.slf4j.LoggerFactory


class LayoutFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutFile.class)
	
	def file
	def contents
	
	LayoutFile(def file) {
		this.file = file
	} 
	
	LayoutFile(def file, def contents) {
		this.file = file
		this.contents = contents
	}
	
	public File write(def root) {
		def layoutFile = new File(root, file)
		LOGGER.info("Creating file {}", layoutFile)
		
		if(!layoutFile.exists()) {
			if(createDirs(layoutFile.getParentFile())) {
				layoutFile.createNewFile()
				if(contents) {
					layoutFile.write(contents())
				}
			}
		}
		return layoutFile
	}
	
	private boolean createDirs(File dir) {
		boolean exists = true
		if(!dir.exists()) {
			exists = dir.mkdirs()
		}
		return exists
	}
	
}
