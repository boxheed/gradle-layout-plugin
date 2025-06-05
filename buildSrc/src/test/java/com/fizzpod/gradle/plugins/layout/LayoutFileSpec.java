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

public class LayoutFileSpec {

    @TempDir
    Path temporaryFolderPath; // JUnit 5 injects Path, can convert to File

    private File getTempFolder() {
        return temporaryFolderPath.toFile();
    }

    @Test
    void createFileInRootFolder() throws IOException {
        // when:
        LayoutFile layoutFile = new LayoutFile("file1");

        // then:
        layoutFile.write(getTempFolder());

        // expect:
        File file = new File(getTempFolder(), "file1");
        assertTrue(file.exists(), "File should exist");
        assertTrue(file.isFile(), "Path should be a file");
    }

    @Test
    void createFileWithForwardSlashAtStart() throws IOException {
        // when:
        LayoutFile layoutFile = new LayoutFile("/file1"); // Path for LayoutFile constructor

        // then:
        layoutFile.write(getTempFolder());

        // expect:
        // LayoutFile.java handles relativizing paths starting with "/"
        File file = new File(getTempFolder(), "file1");
        assertTrue(file.exists(), "File should exist");
        assertTrue(file.isFile(), "Path should be a file");
    }

    @Test
    void createFileInMultipleSubfolders() throws IOException {
        // when:
        LayoutFile layoutFile = new LayoutFile("/folder1/folder1_1/folder1_1_1/file1");

        // then:
        layoutFile.write(getTempFolder());

        // expect:
        File file = new File(getTempFolder(), "folder1/folder1_1/folder1_1_1/file1");
        assertTrue(file.exists(), "File should exist in subfolders");
        assertTrue(file.isFile(), "Path should be a file");
    }

    @Test
    void createFileWithFolderInTheWay() throws IOException {
        // setup:
        File existingFolder = new File(getTempFolder(), "folder1");
        assertTrue(existingFolder.mkdirs(), "Setup: Failed to create directory folder1");

        // when:
        LayoutFile layoutFile = new LayoutFile("/folder1"); // Trying to create a FILE named 'folder1'

        // then:
        layoutFile.write(getTempFolder()); // LayoutFile should not overwrite an existing directory with a file

        // expect:
        assertTrue(existingFolder.exists(), "Existing folder should still exist");
        assertTrue(existingFolder.isDirectory(), "Path should remain a directory");
    }

    @Test
    void createFileWithFileInSubFolderPosition() throws IOException {
        // setup:
        File blockingFile = new File(getTempFolder(), "file1");
        assertTrue(blockingFile.createNewFile(), "Setup: Failed to create blocking file");
        Files.writeString(blockingFile.toPath(), "This is a blocking file.");


        // when:
        // Trying to create a file where a segment of the path ("file1") is an existing file
        LayoutFile layoutFile = new LayoutFile("/file1/folder1_1/file1_1");

        // then:
        layoutFile.write(getTempFolder()); // This should fail to create the full path due to 'file1'

        // expect:
        assertTrue(blockingFile.exists(), "Existing file 'file1' should still exist");
        assertTrue(blockingFile.isFile(), "Existing 'file1' should be a file");

        File newFile = new File(getTempFolder(), "file1/folder1_1/file1_1");
        assertFalse(newFile.exists(), "New file should not be created because 'file1' is a file, not a directory");
    }

    @Test
    void createFileWithContent() throws IOException {
        // when:
        LayoutFile layoutFile = new LayoutFile("/file1", () -> "I am content");

        // then:
        layoutFile.write(getTempFolder());

        // expect:
        File newFile = new File(getTempFolder(), "file1");
        assertTrue(newFile.exists(), "File should be created");
        assertEquals("I am content", Files.readString(newFile.toPath()), "File content should match");
    }

    @Test
    void createFileWithMultilineContent() throws IOException {
        String multilineContent = "I am content line 1\nLine 2\nLine3";
        // when:
        LayoutFile layoutFile = new LayoutFile("/file1", () -> multilineContent);

        // then:
        layoutFile.write(getTempFolder());

        // expect:
        File newFile = new File(getTempFolder(), "file1");
        assertTrue(newFile.exists(), "File should be created with multiline content");
        assertEquals(multilineContent, Files.readString(newFile.toPath()), "Multiline file content should match");
    }
}
