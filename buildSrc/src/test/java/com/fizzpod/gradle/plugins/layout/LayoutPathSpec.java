/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LayoutPathSpec {

    @TempDir
    Path temporaryFolderPath;

    private File getTempFolder() {
        return temporaryFolderPath.toFile();
    }

    @Test
    void createPathInRootFolder() {
        // when:
        LayoutPath layoutPath = new LayoutPath("folder1");

        // then:
        layoutPath.write(getTempFolder());

        // expect:
        File folder = new File(getTempFolder(), "folder1");
        assertTrue(folder.exists(), "Folder should exist");
        assertTrue(folder.isDirectory(), "Path should be a directory");
    }

    @Test
    void createPathWithForwardSlashAtStart() {
        // when:
        LayoutPath layoutPath = new LayoutPath("/folder1");

        // then:
        layoutPath.write(getTempFolder());

        // expect:
        // LayoutPath.java handles relativizing paths starting with "/"
        File folder = new File(getTempFolder(), "folder1");
        assertTrue(folder.exists(), "Folder should exist");
        assertTrue(folder.isDirectory(), "Path should be a directory");
    }

    @Test
    void createPathWithMultipleSubfolders() {
        // when:
        LayoutPath layoutPath = new LayoutPath("/folder1/folder1_1/folder1_1_1");

        // then:
        layoutPath.write(getTempFolder());

        // expect:
        File folder = new File(getTempFolder(), "folder1/folder1_1/folder1_1_1");
        assertTrue(folder.exists(), "Folder structure should exist");
        assertTrue(folder.isDirectory(), "Path should be a directory");
    }

    @Test
    void createPathWithFileInTheWay() throws IOException {
        // setup:
        File existingFile = new File(getTempFolder(), "file1");
        assertTrue(existingFile.createNewFile(), "Setup: Failed to create file1");
        Files.writeString(existingFile.toPath(), "content");


        // when:
        LayoutPath layoutPath = new LayoutPath("/file1"); // Attempting to create a directory named "file1"

        // then:
        layoutPath.write(getTempFolder()); // LayoutPath.java's write should not create a dir if a file exists

        // expect:
        assertTrue(existingFile.exists(), "Existing file should still exist");
        assertTrue(existingFile.isFile(), "Path should remain a file");
        // Ensure it didn't somehow become a directory
        long size = Files.size(existingFile.toPath()); // Reading size to ensure it's still the file
        assertEquals("content".length(), (int)size, "File content should be intact");
    }

    @Test
    void createPathWithFileInSubFolderPosition() throws IOException {
        // setup:
        File blockingFile = new File(getTempFolder(), "file1");
        assertTrue(blockingFile.createNewFile(), "Setup: Failed to create blocking file 'file1'");

        // when:
        // Trying to create a directory structure where "file1" is a file, not a directory
        LayoutPath layoutPath = new LayoutPath("/file1/folder1_1");

        // then:
        layoutPath.write(getTempFolder()); // This should fail to create "folder1_1" inside "file1"

        // expect:
        assertTrue(blockingFile.exists(), "Existing file 'file1' should still exist");
        assertTrue(blockingFile.isFile(), "Existing 'file1' should remain a file");

        // The Spock test had "file1/folder1", but LayoutPath input was "/file1/folder1_1"
        File newFolder = new File(getTempFolder(), "file1/folder1_1");
        assertFalse(newFolder.exists(), "New folder 'file1/folder1_1' should not be created");
    }
}
