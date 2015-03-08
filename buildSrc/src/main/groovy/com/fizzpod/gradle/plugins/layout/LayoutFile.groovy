package com.fizzpod.gradle.plugins.layout

class LayoutFile {

	def file
	def contents
	
	LayoutFile(def file) {
		this.file = file;
	} 
	
	LayoutFile(def file, def contents) {
		this.file = file;
		this.contents = contents;
	}
	
	public File write(def root) {
		def layoutFile = new File(root, file);
		if(!layoutFile.exists()) {
			layoutFile.createNewFile();
			if(contents) {
				layoutFile.write(contents())
			}
		}
		return layoutFile
	}
	
}
