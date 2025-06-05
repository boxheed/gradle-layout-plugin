/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class LayoutPluginExtensionSpec {

    private lateinit var extension: LayoutPluginExtension

    @BeforeEach
    fun setup() {
        extension = LayoutPluginExtension()
    }

    @Test
    fun `Adding path to extension`() {
        // when:
        extension.path("path")

        // then:
        assertEquals(1, extension.layouts.size, "Should have one layout item")
        val item = extension.layouts[0]
        assertTrue(item is LayoutPath, "Item should be a LayoutPath")
        assertEquals("path", (item as LayoutPath).path, "Path property should match")
    }

    @Test
    fun `Adding file to extension`() {
        // when:
        // Using a simple lambda for the content provider
        extension.file("file") { "test content" }

        // then:
        assertEquals(1, extension.layouts.size, "Should have one layout item")
        val item = extension.layouts[0]
        assertTrue(item is LayoutFile, "Item should be a LayoutFile")
        assertEquals("file", (item as LayoutFile).file, "File property should match")
        // Optionally, check content if needed, though this test focuses on adding the file
        assertNotNull((item as LayoutFile).contents, "Contents should not be null")
        assertEquals("test content", (item as LayoutFile).contents!!(), "Content should match")
    }

    @Test
    fun `Adding path and file to extension`() {
        // when:
        extension.path("path")
        extension.file("file") { "test content" }

        // then:
        assertEquals(2, extension.layouts.size, "Should have two layout items")

        val item1 = extension.layouts[0]
        assertTrue(item1 is LayoutPath, "First item should be a LayoutPath")
        assertEquals("path", (item1 as LayoutPath).path, "Path property of first item should match")

        val item2 = extension.layouts[1]
        assertTrue(item2 is LayoutFile, "Second item should be a LayoutFile")
        assertEquals("file", (item2 as LayoutFile).file, "File property of second item should match")
        assertNotNull((item2 as LayoutFile).contents, "Contents of second item should not be null")
        assertEquals("test content", (item2 as LayoutFile).contents!!(), "Content of second item should match")
    }

    // TODO: Add tests for url(), name(), remote(), and invoke() (Closure execution)
    // Testing the 'name' method which involves network/file access for URL might be more of an integration test.
    // Testing 'invoke' with Groovy Closure from Kotlin might require specific setup or a Groovy test environment.
}
