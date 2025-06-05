/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayoutPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPlugin.class);

    @Override
    public void apply(Project project) {
        // Create the extension object with the name "layout"
        // The extension class LayoutPluginExtension.class must have a public default constructor.
        final LayoutPluginExtension layoutPluginExtension = project.getExtensions().create("layout", LayoutPluginExtension.class);

        // Create a task named "createLayout"
        Task createLayoutTask = project.getTasks().create("createLayout");
        createLayoutTask.setGroup("layout"); // Assigning to a task group
        createLayoutTask.setDescription("Creates the project layout based on configurations in the 'layout' extension.");

        // Define the action for the task
        createLayoutTask.doLast(task -> {
            LOGGER.info("Executing 'createLayout' task for project '{}'...", project.getPath());

            if (layoutPluginExtension.getLayouts() == null || layoutPluginExtension.getLayouts().isEmpty()) {
                LOGGER.info("No layouts defined in the 'layout' extension for project '{}'. Nothing to create.", project.getPath());
                return; // Exit early if there's nothing to do
            }

            // Using project.getRootDir() as the base for writing layouts, as per the original Groovy script.
            // This means layouts are typically created relative to the root project directory.
            File rootDir = project.getRootDir();
            LOGGER.info("Layouts will be written relative to: {}", rootDir.getAbsolutePath());

            for (LayoutRenderable item : layoutPluginExtension.getLayouts()) {
                try {
                    if (item != null) {
                        item.write(rootDir);
                    } else {
                        LOGGER.warn("Encountered a null LayoutRenderable item in project '{}'. This item will be skipped.", project.getPath());
                    }
                } catch (Exception e) {
                    // Log the error and continue with other layout items.
                    // To make the build fail on any error, rethrow as GradleException or TaskExecutionException.
                    LOGGER.error("Error writing layout item for project '{}': {}", project.getPath(), e.getMessage(), e);
                    // Example: throw new org.gradle.api.tasks.TaskExecutionException(task, e);
                }
            }
            LOGGER.info("'createLayout' task finished for project '{}'.", project.getPath());
        });
    }
}
