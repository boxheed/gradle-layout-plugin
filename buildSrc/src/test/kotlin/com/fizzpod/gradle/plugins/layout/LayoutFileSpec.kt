/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File

class LayoutFileSpec {

    @TempDir
    lateinit var temporaryFolder: File

    @Test
    fun `create file in root folder`() {
        // when:
        val layoutFile = LayoutFile("file1")

        // then:
        layoutFile.write(temporaryFolder)

        // expect:
        val file = File(temporaryFolder, "file1")
        assertTrue(file.exists(), "File should exist")
        assertTrue(file.isFile, "Path should be a file")
    }

    @Test
    fun `create file with forward slash at start`() {
        // when:
        val layoutFile = LayoutFile("/file1")

        // then:
        layoutFile.write(temporaryFolder)

        // expect:
        // In Kotlin/Java File API, leading slash in pathname for child is usually problematic if root is already absolute.
        // However, File(parent, child) handles this by making 'child' relative to 'parent'.
        // If 'file1' was intended, it should be "file1". If "/file1" was intended to be relative, that's fine.
        // Assuming current behavior of LayoutFile is to treat it as relative path segment.
        val file = File(temporaryFolder, "file1") // Spock test implied "file1" not "/file1" under root
        assertTrue(file.exists(), "File should exist")
        assertTrue(file.isFile, "Path should be a file")
    }

    @Test
    fun `create file in multiple subfolders`() {
        // when:
        val layoutFile = LayoutFile("/folder1/folder1_1/folder1_1_1/file1") // Path for LayoutFile

        // then:
        layoutFile.write(temporaryFolder)

        // expect:
        val file = File(temporaryFolder, "folder1/folder1_1/folder1_1_1/file1") // Expected path
        assertTrue(file.exists(), "File should exist in subfolders")
        assertTrue(file.isFile, "Path should be a file")
    }

    @Test
    fun `create file with folder in the way`() {
        // setup:
        val existingFolder = File(temporaryFolder, "folder1")
        existingFolder.mkdirs() // Ensure the folder exists as per Spock's newFolder

        // when:
        val layoutFile = LayoutFile("/folder1") // Trying to create a file named 'folder1'

        // then:
        // The LayoutFile.write method will try to create a file.
        // If a directory already exists at that path, createNewFile() should fail.
        // The original Spock test seems to imply that if 'folder1' is a directory,
        // it should remain a directory, and no file named 'folder1' is created.
        // The current LayoutFile.kt logic is: if exists, skip. If it's a dir, it's skipped.
        layoutFile.write(temporaryFolder)


        // expect:
        assertTrue(existingFolder.exists(), "Existing folder should still exist")
        assertTrue(existingFolder.isDirectory, "Path should be a directory")
        // And no file named "folder1" should have overwritten the directory
        // Depending on LayoutFile's exact logic when path is a dir, this might need more specific assertions.
        // The current LayoutFile.kt: if (layoutFile.exists()) { LOGGER.info("File {} already exists. Skipping creation.") }
        // So, it won't try to create a file if something (file or dir) exists. This matches.
    }

    @Test
    fun `create file with file in sub folder position`() {
        // setup:
        val existingFile = File(temporaryFolder, "file1")
        existingFile.writeText("I am an existing file") // Spock's newFile creates a file

        // when:
        // Trying to create a file where a segment of the path is an existing *file*
        val layoutFile = LayoutFile("/file1/folder1_1/file1_1")

        // then:
        // LayoutFile.createDirs will try to create parentFile for "/file1/folder1_1/file1_1"
        // parentFile is "/file1/folder1_1". createDirs will attempt dir.mkdirs().
        // If "file1" is a file, then "file1/folder1_1" cannot be created as a directory.
        layoutFile.write(temporaryFolder)

        // expect:
        assertTrue(existingFile.exists(), "Existing file 'file1' should still exist")
        assertTrue(existingFile.isFile, "Existing 'file1' should be a file")

        val newFile = File(temporaryFolder, "file1/folder1_1/file1_1")
        assertFalse(newFile.exists(), "New file should not be created because parent path segment is a file")
    }

    @Test
    fun `create file with content`() {
        // when:
        val layoutFile = LayoutFile("/file1") { "I am content" }

        // then:
        layoutFile.write(temporaryFolder)

        // expect:
        val newFile = File(temporaryFolder, "file1")
        assertTrue(newFile.exists(), "File should be created")
        assertEquals("I am content", newFile.readText(), "File content should match")
    }

    @Test
    fun `create file with multiline content`() {
        val content = """I am content line 1
Line 2
Line3"""
        // when:
        val layoutFile = LayoutFile("/file1") { content }

        // then:
        layoutFile.write(temporaryFolder)

        // expect:
        val newFile = File(temporaryFolder, "file1")
        assertTrue(newFile.exists(), "File should be created with multiline content")
        assertEquals(content, newFile.readText(), "Multiline file content should match")
    }
}
