/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import groovy.lang.Binding
import groovy.lang.GroovyShell
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL

class LayoutDescriptor(val url: String) : LayoutItem {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(LayoutDescriptor::class.java)
    }

    override fun write(root: File): File {
        val layoutDsl = getLayoutDsl()
        //evaluate description
        val extension = evaluateDsl(layoutDsl)
        extension.layouts.forEach {
            it.write(root) // Now type-safe if layouts contains LayoutItem instances
        }
        return root
    }

    private fun getLayoutDsl(): String {
        LOGGER.debug("Getting DSL from {}", url)
        val layoutDsl = URL(url).readText()
        LOGGER.debug("Parsing Descriptor: {}", layoutDsl)
        return layoutDsl
    }

    private fun evaluateDsl(dsl: String): LayoutPluginExtension {
        val binding = Binding()
        val shell = GroovyShell(binding)
        val extension = LayoutPluginExtension()
        binding.setProperty("layout", extension)
        shell.evaluate(dsl)
        return extension
    }
}
