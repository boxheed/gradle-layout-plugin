/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class LayoutDescriptor implements LayoutRenderable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutDescriptor.class);

    private final String url;

    public LayoutDescriptor(String url) {
        this.url = url;
    }

    @Override
    public File write(File root) {
        String layoutDsl = getLayoutDsl();
        if (layoutDsl == null || layoutDsl.isEmpty()) {
            LOGGER.warn("Layout DSL from URL '{}' is empty or could not be read. Skipping processing for this descriptor.", url);
            return root; // Or handle error appropriately, e.g., by returning null or throwing an exception
        }

        LayoutPluginExtension extension = evaluateDsl(layoutDsl);

        if (extension != null) {
            List<LayoutRenderable> layouts = extension.getLayouts(); // Assuming getLayouts() is part of LayoutPluginExtension
            if (layouts != null) {
                for (LayoutRenderable item : layouts) {
                    if (item != null) {
                        try {
                            item.write(root);
                        } catch (Exception e) {
                            LOGGER.error("Error writing layout item defined in DSL from URL '{}': {}", url, e.getMessage(), e);
                            // Decide if one item failing should stop others. For now, it continues.
                        }
                    } else {
                        LOGGER.warn("A null layout item was encountered in DSL from URL '{}'.", url);
                    }
                }
            } else {
                LOGGER.warn("No layout items found in the evaluated DSL from URL '{}'.", url);
            }
        } else {
            LOGGER.warn("DSL evaluation from URL '{}' resulted in a null extension. No layouts will be processed.", url);
        }
        return root;
    }

    private String getLayoutDsl() {
        if (this.url == null || this.url.trim().isEmpty()) {
            LOGGER.error("URL for LayoutDescriptor is null or empty.");
            return null;
        }
        LOGGER.debug("Getting DSL from URL: {}", url);
        try {
            // Ensure the URL is properly formed before attempting to open a stream
            URL validatedUrl = new URL(this.url); // This can throw MalformedURLException
            try (InputStream inputStream = validatedUrl.openStream();
                 Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                if (!scanner.hasNext()) {
                    LOGGER.warn("No content found at URL: {}", this.url);
                    return ""; // Return empty string if URL has no content
                }
                return scanner.useDelimiter("\\A").next();
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL provided for LayoutDescriptor: {}", this.url, e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Failed to read content from URL: {}", this.url, e);
            return null;
        }
    }

    private LayoutPluginExtension evaluateDsl(String dsl) {
        if (dsl == null || dsl.trim().isEmpty()) {
            LOGGER.error("DSL string is null or empty, cannot evaluate.");
            return null;
        }
        LOGGER.debug("Evaluating DSL: {}", dsl);
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        // Assumes LayoutPluginExtension has a default constructor and is ready for Java usage.
        // This will be converted next.
        LayoutPluginExtension extension = new LayoutPluginExtension();
        binding.setProperty("layout", extension);

        try {
            shell.evaluate(dsl);
            return extension;
        } catch (Exception e) {
            LOGGER.error("Failed to evaluate DSL from URL '{}'. DSL content: \n{}\nError: {}", url, dsl, e.getMessage(), e);
            return null; // Return null or an empty extension to indicate failure
        }
    }

    public String getUrl() {
        return url;
    }
}
