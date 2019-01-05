package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayoutPluginExtension {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPluginExtension.class);
	
	private static final String DEFAULT_LAYOUT_FILE = "layout.groovy"
	private static final String DEFAULT_REMOTE_ROOT = "https://raw.githubusercontent.com/boxheed/gradle-layout-plugin-layouts/master/" 

	def remotes = [DEFAULT_REMOTE_ROOT]
		
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
	
	def name(def name) {
		def url = remotes[0] + name + "/" + DEFAULT_LAYOUT_FILE;
		LOGGER.info("Mapping name {} to URL {}", name, url)
		println(url)
		this.url(url)
		return this
	}
	
	def remote(def remote) {
		remotes = [remote]
	}

	def call(Closure closure) {
		LOGGER.debug("Registering as delegate with closure {}", closure)
		closure.setDelegate(this)
		closure.call()
	}
}
