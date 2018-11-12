package com.fizzpod.gradle.plugins.layout;

public class LayoutPluginExtension {
	
	def layouts = []
	
	def file(def file) {
		layouts << new LayoutFile(file)
		return this;
	}
	
	def file(def file, def contents) {
		layouts << new LayoutFile(file, contents)
		return this;
	}
	
	def path(def path) {
		layouts << new LayoutPath(path)
		return this;
	}
	
	def url(def url) {
		layouts << new LayoutDescriptor(url)
		return this;
	}
	
	def call() {
		println "called 1"
	}
	
	def call(Object... args) {
		println "called 2"
	}
	
	def call(Closure closure) {
		println "called 3 " + closure + " " + layouts
		closure.setDelegate(this)
		closure.call()
		println "called 3 " + closure + " " + layouts
	}
}
