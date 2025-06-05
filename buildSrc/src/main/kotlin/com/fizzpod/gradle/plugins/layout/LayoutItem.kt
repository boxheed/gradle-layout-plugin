/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import java.io.File

interface LayoutItem {
    fun write(root: File): File
}
