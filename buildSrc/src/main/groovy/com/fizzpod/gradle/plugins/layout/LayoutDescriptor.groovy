package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger
import org.slf4j.LoggerFactory


class LayoutDescriptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutDescriptor.class);

	def url

	LayoutDescriptor(def url) {
		this.url = url;
	}

	public File write(def root) {
		def description = getDescription();
		//parse description
		def extension = evaluateDescription(description)
		extension.layouts.each {
			it.write(root)
		}
		return root
	}
	
	private getDescription() {
		def description = url.toURL().text
		println description
		return description
	} 
	
	private evaluateDescription(def description) {
		def binding = new Binding();
		def shell = new GroovyShell(binding);
		LayoutPluginExtension extension = new LayoutPluginExtension();
		binding.setProperty("layout", extension);
		shell.evaluate(description);
		return extension;
	}
}
