/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File; // Though not directly used, it's contextually relevant via LayoutRenderable
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class LayoutPluginExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPluginExtension.class);

    private static final String DEFAULT_LAYOUT_FILE = "layout.groovy";
    private static final String DEFAULT_REMOTE_ROOT = "https://raw.githubusercontent.com/boxheed/gradle-layout-plugin-layouts/master/";

    // Modifiable list of remotes
    private List<String> remotes = new ArrayList<>(Collections.singletonList(DEFAULT_REMOTE_ROOT));

    // List to hold layout items (files, paths, or descriptors)
    private final List<LayoutRenderable> layouts = new ArrayList<>();

    // DSL method to add a file with no content
    public LayoutPluginExtension file(String filePath) {
        this.layouts.add(new LayoutFile(filePath));
        return this;
    }

    // DSL method to add a file with content provided by a Supplier (e.g., a lambda)
    public LayoutPluginExtension file(String filePath, Supplier<String> contentsSupplier) {
        this.layouts.add(new LayoutFile(filePath, contentsSupplier));
        return this;
    }

    // DSL method to add a directory path
    public LayoutPluginExtension path(String pathValue) {
        this.layouts.add(new LayoutPath(pathValue));
        return this;
    }

    // DSL method to add a layout descriptor from a URL
    public LayoutPluginExtension url(String urlValue) {
        this.layouts.add(new LayoutDescriptor(urlValue));
        return this;
    }

    // DSL method to add a layout by name, resolved against configured remotes
    public LayoutPluginExtension name(String nameValue) {
        if (this.remotes.isEmpty()) {
            LOGGER.warn("No remotes configured. Cannot resolve layout name '{}'", nameValue);
            return this;
        }
        // Using the first remote in the list by convention
        String baseUrl = this.remotes.get(0);
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        String fullUrl = baseUrl + nameValue + "/" + DEFAULT_LAYOUT_FILE;

        LOGGER.info("Mapping layout name '{}' to URL: {}", nameValue, fullUrl);
        // System.out.println(fullUrl); // Original Groovy code had a println here

        this.url(fullUrl); // Delegates to the url() method
        return this;
    }

    // Method to set/replace the list of remotes
    public void remote(String remoteUrl) {
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            LOGGER.warn("Remote URL cannot be null or empty. Remotes list not changed.");
            return;
        }
        // Replaces the existing list of remotes with a new list containing the single specified remote
        this.remotes = new ArrayList<>(Collections.singletonList(remoteUrl));
        LOGGER.info("Layout remotes updated to: {}", this.remotes);
    }

    // Method to add a remote to the existing list of remotes (alternative to replacing)
    public void addRemote(String remoteUrl) {
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            LOGGER.warn("Remote URL to add cannot be null or empty.");
            return;
        }
        if (!this.remotes.contains(remoteUrl)) {
            this.remotes.add(remoteUrl);
            LOGGER.info("Layout remote '{}' added. Current remotes: {}", remoteUrl, this.remotes);
        } else {
            LOGGER.info("Layout remote '{}' already configured.", remoteUrl);
        }
    }


    // Allows calling the extension instance with a Groovy closure (for Gradle build scripts)
    // e.g., layout { file 'my.txt' }
    public Object call(Closure<?> closure) {
        if (closure == null) {
            LOGGER.warn("Received null closure in call method.");
            return null;
        }
        LOGGER.debug("Configuring LayoutPluginExtension via closure delegate.");
        closure.setDelegate(this);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        return closure.call();
    }

    // Getter for the list of layout items
    public List<LayoutRenderable> getLayouts() {
        return Collections.unmodifiableList(this.layouts); // Return an unmodifiable view
    }

    // Getter for the list of remotes
    public List<String> getRemotes() {
        return Collections.unmodifiableList(this.remotes); // Return an unmodifiable view
    }
}
