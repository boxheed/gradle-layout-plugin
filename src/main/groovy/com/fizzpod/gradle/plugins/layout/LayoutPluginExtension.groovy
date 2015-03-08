package com.fizzpod.gradle.plugins.layout;

public class LayoutPluginExtension {
	
	def layouts = []
	
	def file(def file, def contents) {
		layouts << new LayoutFile(file, contents)
		return this;
	}
	
	def path(def path) {
		layouts << new LayoutPath(path)
		return this;
	}
	
	
	
}
