/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LayoutPath implements LayoutRenderable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPath.class);

    private final String pathValue; // Renamed from 'path' to avoid ambiguity if a getter named getPath() is desired for a different purpose

    public LayoutPath(String pathValue) {
        this.pathValue = pathValue;
    }

    @Override
    public File write(File rootDir) {
        // Ensure 'pathValue' is relative and rootDir is the actual base directory
        String relativePath = this.pathValue.startsWith("/") ? this.pathValue.substring(1) : this.pathValue;
        File targetDirectory = new File(rootDir, relativePath);

        LOGGER.info("Attempting to process directory: {}", targetDirectory.getAbsolutePath());

        if (!targetDirectory.exists()) {
            LOGGER.info("Creating directory: {}", targetDirectory.getAbsolutePath());
            if (targetDirectory.mkdirs()) {
                LOGGER.debug("Directory created successfully: {}", targetDirectory.getAbsolutePath());
            } else {
                // mkdirs() failed. This could be due to a file with the same name existing,
                // or lack of permissions, or an invalid path component.
                if (targetDirectory.isFile()) {
                    LOGGER.warn("Cannot create directory because a file with the same name already exists: {}", targetDirectory.getAbsolutePath());
                } else {
                    // Attempt to check parent directory existence and writability if more diagnostics are needed
                    File parentDir = targetDirectory.getParentFile();
                    if (parentDir != null && !parentDir.exists()){
                        LOGGER.warn("Parent directory {} does not exist.", parentDir.getAbsolutePath());
                    } else if (parentDir != null && !parentDir.canWrite()){
                        LOGGER.warn("No write permissions for parent directory {}.", parentDir.getAbsolutePath());
                    } else {
                        LOGGER.warn("Failed to create directory for an unknown reason: {}", targetDirectory.getAbsolutePath());
                    }
                }
            }
        } else {
            if (targetDirectory.isDirectory()) {
                LOGGER.info("Directory {} already exists. Skipping creation.", targetDirectory.getAbsolutePath());
            } else {
                LOGGER.warn("A file with the name {} already exists. Cannot create directory.", targetDirectory.getAbsolutePath());
            }
        }
        return targetDirectory;
    }

    // Getter for the path value
    public String getPathValue() {
        return pathValue;
    }
}
