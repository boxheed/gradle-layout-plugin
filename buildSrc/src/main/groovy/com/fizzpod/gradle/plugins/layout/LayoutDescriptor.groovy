/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger
import org.slf4j.LoggerFactory


class LayoutDescriptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutDescriptor.class)

	def url

	LayoutDescriptor(def url) {
		this.url = url
	}

	public File write(def root) {
		def layoutDsl = getLayoutDsl()
		//evaluate description
		def extension = evaluateDsl(layoutDsl)
		extension.layouts.each {
			it.write(root)
		}
		return root
	}
	
	private getLayoutDsl() {
		LOGGER.debug("Getting DSL from {}", url)
		def layoutDsl = url.toURL().text
		LOGGER.debug("Parsing Descriptor: {}", layoutDsl)
		return layoutDsl
	} 
	
	private evaluateDsl(def dsl) {
		def binding = new Binding()
		def shell = new GroovyShell(binding)
		LayoutPluginExtension extension = new LayoutPluginExtension()
		binding.setProperty("layout", extension)
		shell.evaluate(dsl)
		return extension
	}
}
