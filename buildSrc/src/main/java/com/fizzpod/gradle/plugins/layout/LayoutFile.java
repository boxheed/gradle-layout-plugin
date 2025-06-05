/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

public class LayoutFile implements LayoutRenderable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutFile.class);

    private final String filePath; // Renamed from 'file' to avoid confusion with java.io.File
    private final Supplier<String> contentsSupplier;

    public LayoutFile(String filePath) {
        this(filePath, null);
    }

    public LayoutFile(String filePath, Supplier<String> contentsSupplier) {
        this.filePath = filePath;
        this.contentsSupplier = contentsSupplier;
    }

    @Override
    public File write(File rootDir) {
        // Ensure 'filePath' is relative and rootDir is the actual base directory
        String relativePath = this.filePath.startsWith("/") ? this.filePath.substring(1) : this.filePath;
        File targetFile = new File(rootDir, relativePath);

        LOGGER.info("Attempting to process file: {}", targetFile.getAbsolutePath());

        if (!targetFile.exists()) {
            File parentDir = targetFile.getParentFile();
            if (createDirs(parentDir)) {
                try {
                    if (targetFile.createNewFile()) {
                        LOGGER.debug("File created: {}", targetFile.getAbsolutePath());
                        if (this.contentsSupplier != null) {
                            try (FileWriter writer = new FileWriter(targetFile)) {
                                writer.write(this.contentsSupplier.get());
                                LOGGER.debug("File content written to: {}", targetFile.getAbsolutePath());
                            } catch (IOException e) {
                                LOGGER.error("Failed to write content to file: {}", targetFile.getAbsolutePath(), e);
                                // Optionally, delete the created file if content writing fails
                                // targetFile.delete();
                            }
                        }
                    } else {
                        // This state (createNewFile returns false when exists() was false) is unusual
                        // but could happen due to a race condition or if the path is problematic (e.g., a directory).
                        LOGGER.warn("File {} already existed or could not be created (createNewFile returned false), despite initial check.", targetFile.getAbsolutePath());
                         if (targetFile.isDirectory()) {
                            LOGGER.warn("A directory with the same name {} already exists. Cannot create file.", targetFile.getAbsolutePath());
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to create file: {}", targetFile.getAbsolutePath(), e);
                }
            } else {
                // createDirs would have logged the reason for failure.
                LOGGER.warn("Could not create parent directories for: {}", targetFile.getAbsolutePath());
            }
        } else {
            LOGGER.info("File {} already exists. As per current logic, it will not be overwritten.", targetFile.getAbsolutePath());
            // If overwriting or updating based on new content is desired, that logic would go here.
            // For example, if contentsSupplier is not null, read existing, compare, and overwrite if different.
        }
        return targetFile;
    }

    private boolean createDirs(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return true;
            } else {
                LOGGER.warn("Cannot create directory. A file with the same name already exists: {}", dir.getAbsolutePath());
                return false;
            }
        }
        LOGGER.debug("Creating directory: {}", dir.getAbsolutePath());
        return dir.mkdirs();
    }

    // Getters for properties
    public String getFilePath() {
        return filePath;
    }

    public Supplier<String> getContentsSupplier() {
        return contentsSupplier;
    }
}
