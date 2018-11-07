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
		return root
	}
	
	private getDescription() {
		def description = url.toURL().text
		println description
		return description
	} 
}
