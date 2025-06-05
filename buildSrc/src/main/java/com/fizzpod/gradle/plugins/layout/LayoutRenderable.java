/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout;

import java.io.File;

@FunctionalInterface
public interface LayoutRenderable {
    File write(File root);
}
