/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Supplier;

public class LayoutPluginExtensionSpec {

    private LayoutPluginExtension extension;

    @BeforeEach
    void setUp() {
        extension = new LayoutPluginExtension();
    }

    @Test
    void addingPathToExtension() {
        // when:
        extension.path("path");

        // then:
        List<LayoutRenderable> layouts = extension.getLayouts();
        assertEquals(1, layouts.size(), "Should have one layout item");
        assertTrue(layouts.get(0) instanceof LayoutPath, "Item should be a LayoutPath");
        assertEquals("path", ((LayoutPath) layouts.get(0)).getPathValue(), "Path property should match");
    }

    @Test
    void addingFileToExtension() {
        // when:
        Supplier<String> emptyContent = () -> "";
        extension.file("file", emptyContent);

        // then:
        List<LayoutRenderable> layouts = extension.getLayouts();
        assertEquals(1, layouts.size(), "Should have one layout item");
        assertTrue(layouts.get(0) instanceof LayoutFile, "Item should be a LayoutFile");
        LayoutFile layoutFile = (LayoutFile) layouts.get(0);
        assertEquals("file", layoutFile.getFilePath(), "File property should match");
        assertSame(emptyContent, layoutFile.getContentsSupplier(), "Contents supplier should match");
    }

    @Test
    void addingPathAndFileToExtension() {
        // when:
        extension.path("path");
        Supplier<String> emptyContent = () -> "";
        extension.file("file", emptyContent);

        // then:
        List<LayoutRenderable> layouts = extension.getLayouts();
        assertEquals(2, layouts.size(), "Should have two layout items");

        assertTrue(layouts.get(0) instanceof LayoutPath, "First item should be a LayoutPath");
        assertEquals("path", ((LayoutPath) layouts.get(0)).getPathValue(), "Path property of first item should match");

        assertTrue(layouts.get(1) instanceof LayoutFile, "Second item should be a LayoutFile");
        LayoutFile layoutFile = (LayoutFile) layouts.get(1);
        assertEquals("file", layoutFile.getFilePath(), "File property of second item should match");
        assertSame(emptyContent, layoutFile.getContentsSupplier(), "Contents supplier of second item should match");
    }

    // TODO: Consider adding tests for:
    // - url() method
    // - name() method (and its interaction with remotes)
    // - remote() and addRemote() methods
    // - call(Closure) method (this would likely require a Groovy environment or specific interop testing)
}
