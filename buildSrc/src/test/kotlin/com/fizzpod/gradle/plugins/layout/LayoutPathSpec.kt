/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File

class LayoutPathSpec {

    @TempDir
    lateinit var temporaryFolder: File

    @Test
    fun `create path in root folder`() {
        // when:
        val layoutPath = LayoutPath("folder1")

        // then:
        layoutPath.write(temporaryFolder)

        // expect:
        val folder = File(temporaryFolder, "folder1")
        assertTrue(folder.exists(), "Folder should exist")
        assertTrue(folder.isDirectory, "Path should be a directory")
    }

    @Test
    fun `create path with forward slash at start`() {
        // when:
        val layoutPath = LayoutPath("/folder1") // Path for LayoutPath

        // then:
        layoutPath.write(temporaryFolder)

        // expect:
        val folder = File(temporaryFolder, "folder1") // Expected path, / is handled by File constructor
        assertTrue(folder.exists(), "Folder should exist")
        assertTrue(folder.isDirectory, "Path should be a directory")
    }

    @Test
    fun `create path with multiple subfolders`() {
        // when:
        val layoutPath = LayoutPath("/folder1/folder1_1/folder1_1_1") // Path for LayoutPath

        // then:
        layoutPath.write(temporaryFolder)

        // expect:
        val folder = File(temporaryFolder, "folder1/folder1_1/folder1_1_1") // Expected path
        assertTrue(folder.exists(), "Folder structure should exist")
        assertTrue(folder.isDirectory, "Path should be a directory")
    }

    @Test
    fun `create path with file in the way`() {
        // setup:
        val existingFile = File(temporaryFolder, "file1")
        existingFile.writeText("This is a test file.") // Spock's newFile creates a file

        // when:
        // Trying to create a directory where a file with the same name exists
        val layoutPath = LayoutPath("/file1")

        // then:
        // LayoutPath.write calls mkdirs(). If "file1" is a file, mkdirs() on it will fail.
        layoutPath.write(temporaryFolder)

        // expect:
        assertTrue(existingFile.exists(), "Existing file should still exist")
        assertTrue(existingFile.isFile, "Path should remain a file, not converted to directory")
        // Ensure it didn't somehow become a directory
        assertFalse(existingFile.isDirectory, "Path should not be a directory")
    }

    @Test
    fun `create path with file in sub folder position`() {
        // setup:
        val existingFile = File(temporaryFolder, "file1")
        existingFile.writeText("This is an existing file.") // Spock's newFile

        // when:
        // Trying to create a directory structure where a segment of the path ("file1") is already a file.
        val layoutPath = LayoutPath("/file1/folder1_1")

        // then:
        // LayoutPath.write will attempt File(root, "/file1/folder1_1").mkdirs().
        // This should fail because "file1" is a file, so "file1/folder1_1" cannot be created.
        layoutPath.write(temporaryFolder)

        // expect:
        assertTrue(existingFile.exists(), "Existing file 'file1' should still exist")
        assertTrue(existingFile.isFile, "Existing 'file1' should be a file")

        // The Spock test had "file1/folder1", assuming it meant "file1/folder1_1" based on the LayoutPath input
        val newFolder = File(temporaryFolder, "file1/folder1_1")
        assertFalse(newFolder.exists(), "New folder should not be created because a parent path segment is a file")
    }
}
