package com.fizzpod.gradle.plugins.layout

class LayoutPluginExtension {
    final Map<String, Closure> files = [:]
    final Set<String> paths = []

    void file(String path) {
        files[path] = null
    }

    void file(String path, Closure content) {
        files[path] = content
    }

    void path(String path) {
        paths.add(path)
    }
}
