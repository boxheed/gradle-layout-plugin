/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import groovy.lang.Closure
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class LayoutPluginExtension {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(LayoutPluginExtension::class.java)
        private const val DEFAULT_LAYOUT_FILE = "layout.groovy"
        private const val DEFAULT_REMOTE_ROOT = "https://raw.githubusercontent.com/boxheed/gradle-layout-plugin-layouts/master/"
    }

    var remotes: MutableList<String> = mutableListOf(DEFAULT_REMOTE_ROOT)
    val layouts: MutableList<LayoutItem> = mutableListOf()

    fun file(file: String): LayoutPluginExtension {
        layouts.add(LayoutFile(file))
        return this
    }

    fun file(file: String, contents: () -> String): LayoutPluginExtension {
        layouts.add(LayoutFile(file, contents))
        return this
    }

    fun path(path: String): LayoutPluginExtension {
        layouts.add(LayoutPath(path))
        return this
    }

    fun url(url: String): LayoutPluginExtension {
        layouts.add(LayoutDescriptor(url))
        return this
    }

    fun name(name: String): LayoutPluginExtension {
        if (remotes.isEmpty()) {
            LOGGER.warn("No remotes configured, cannot resolve name '$name'")
            return this
        }
        val remoteUrl = remotes[0] // Assuming the first remote is the one to use
        val fullUrl = "$remoteUrl$name/$DEFAULT_LAYOUT_FILE"
        LOGGER.info("Mapping name {} to URL {}", name, fullUrl)
        // The original Groovy code has println(url), which might be for debugging.
        // System.out.println(fullUrl) // Uncomment if direct console output is desired
        this.url(fullUrl)
        return this
    }

    fun remote(remote: String) {
        this.remotes = mutableListOf(remote) // Replaces the list with a new one containing the single remote
    }

    // Allows calling the extension with a closure, e.g., layout { ... }
    operator fun invoke(closure: Closure<*>) {
        LOGGER.debug("Registering as delegate with closure {}", closure)
        closure.delegate = this
        closure.call()
    }
}
