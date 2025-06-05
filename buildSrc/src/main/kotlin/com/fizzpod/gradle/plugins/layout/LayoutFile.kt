/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class LayoutFile(val file: String, var contents: (() -> String)? = null) : LayoutItem {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(LayoutFile::class.java)
    }

    constructor(file: String) : this(file, null)

    override fun write(root: File): File {
        val layoutFile = File(root, file)
        LOGGER.info("Creating file {}", layoutFile.absolutePath) // Using absolutePath for clarity in logs

        if (!layoutFile.exists()) {
            if (createDirs(layoutFile.parentFile)) {
                try {
                    layoutFile.createNewFile()
                    contents?.let { callableContents ->
                        layoutFile.writeText(callableContents())
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to create or write file ${layoutFile.absolutePath}", e)
                    // Optionally rethrow or handle more gracefully
                }
            } else {
                LOGGER.warn("Could not create parent directories for ${layoutFile.absolutePath}")
            }
        } else {
            LOGGER.info("File {} already exists. Skipping creation.", layoutFile.absolutePath)
            // Potentially, if contents are provided, one might want to overwrite or update.
            // For now, matching Groovy: if exists, do nothing more with contents.
        }
        return layoutFile
    }

    private fun createDirs(dir: File?): Boolean {
        if (dir == null) {
            return false
        }
        return dir.exists() || dir.mkdirs()
    }
}
