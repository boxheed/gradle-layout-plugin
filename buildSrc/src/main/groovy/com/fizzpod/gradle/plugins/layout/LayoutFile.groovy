/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LayoutFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutFile.class)

	def file
	def contents
	boolean overwrite = false
	boolean executable = false
	def url
	def project
	Map explicitOptions = new LinkedHashMap()

	LayoutFile(def file) {
		this.file = file
	}

	LayoutFile(def file, def contents) {
		this.file = file
		if (contents instanceof Map) {
			applyOptions((Map) contents)
		} else {
			this.contents = contents
		}
	}

	LayoutFile(def file, Map options, def contents) {
		this.file = file
		this.contents = contents
		applyOptions(options)
	}

	private void applyOptions(Map options) {
		if (options == null) {
			return
		}
		if (options.containsKey("explicitOptions") && options.explicitOptions instanceof Map) {
			this.explicitOptions.putAll((Map) options.explicitOptions)
		} else {
			this.explicitOptions.putAll(options)
		}
		if (options.containsKey("overwrite")) {
			this.overwrite = (boolean) options.overwrite
		}
		if (options.containsKey("executable")) {
			this.executable = (boolean) options.executable
		}
		if (options.containsKey("url")) {
			this.url = options.url
		}
		if (options.containsKey("project")) {
			this.project = options.project
		}
		if (options.containsKey("contents") && this.contents == null) {
			this.contents = options.contents
		}
	}

	public String getNormalizedPath() {
		if (file == null) {
			return ""
		}
		return file.toString().replaceAll("^/+", "")
	}

	public LayoutFile merge(LayoutFile override) {
		if (override == null) {
			return this
		}
		if (override.explicitOptions.containsKey("overwrite")) {
			this.overwrite = override.overwrite
			this.explicitOptions.put("overwrite", override.overwrite)
		}
		if (override.explicitOptions.containsKey("executable")) {
			this.executable = override.executable
			this.explicitOptions.put("executable", override.executable)
		}
		if (override.url != null) {
			this.url = override.url
		}
		if (override.contents != null) {
			this.contents = override.contents
		}
		if (override.project != null) {
			this.project = override.project
		}
		return this
	}

	public File write(def root) {
		def layoutFile = new File(root, file)
		LOGGER.info("Creating file {}", layoutFile)

		if (!layoutFile.isDirectory() && (!layoutFile.exists() || overwrite)) {
			if (createDirs(layoutFile.getParentFile())) {
				if (!layoutFile.exists()) {
					layoutFile.createNewFile()
				}
				def resolvedContents = resolveContents()
				if (resolvedContents != null) {
					layoutFile.setText(resolvedContents.toString(), "UTF-8")
				} else if (overwrite) {
					layoutFile.setText("", "UTF-8")
				}
				if (executable) {
					layoutFile.setExecutable(true)
				}
			}
		}
		return layoutFile
	}

	private def resolveContents() {
		if (contents instanceof Closure) {
			return contents.call()
		}
		if (contents != null) {
			return interpolate(contents.toString())
		}
		if (url != null) {
			LOGGER.debug("Fetching file content from {}", url)
			return url.toURL().getText("UTF-8")
		}
		return null
	}

	private String interpolate(String text) {
		if (text == null || project == null) {
			return text
		}
		String result = text
		result = result.replace('${project.name}', String.valueOf(project.name ?: ""))
		result = result.replace('${project.group}', String.valueOf(project.group ?: ""))
		result = result.replace('${project.version}', String.valueOf(project.version ?: ""))
		result = result.replace('${project.description}', String.valueOf(project.description ?: ""))
		return result
	}

	private boolean createDirs(File dir) {
		boolean exists = true
		if (!dir.exists()) {
			exists = dir.mkdirs()
		}
		return exists
	}

}
