package com.fizzpod.gradle.plugins.layout

import java.io.File;

class LayoutPath {

	def path
	
	LayoutPath(def path) {
		this.path = path
	}
	
	public File write(def root) {
		def layoutDir = new File(root, path)
		if(!layoutDir.exists()) {
			layoutDir.mkdirs()
		}
		return layoutDir
	}
	
}
