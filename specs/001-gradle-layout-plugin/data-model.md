# Data Model for Gradle Layout Plugin

## Entities

### LayoutExtension
This is the main extension object for the plugin, configured in `build.gradle`.

- **paths**: `Set<String>` - A set of directory paths to create.
- **files**: `Map<String, Closure>` - A map where keys are file paths and values are closures that return the file content.

### LayoutFile
A simple class to represent a file to be created.

- **path**: `String` - The path to the file.
- **content**: `String` - The content of the file.

### LayoutPath
A simple class to represent a directory to be created.

- **path**: `String` - The path to the directory.
