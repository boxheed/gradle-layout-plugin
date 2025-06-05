/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class LayoutPath(val path: String) : LayoutItem {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(LayoutPath::class.java)
    }

    override fun write(root: File): File {
        val layoutDir = File(root, path)
        if (!layoutDir.exists()) {
            LOGGER.info("Creating directory {}", layoutDir.absolutePath)
            layoutDir.mkdirs()
        } else {
            LOGGER.info("Skipping directory {} as it already exists", layoutDir.absolutePath)
        }
        return layoutDir
    }
}
