/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LayoutDescriptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutDescriptor.class)

	def url
	def project
	def remotes
	boolean overwrite = false

	LayoutDescriptor(def url) {
		this.url = url
	}

	LayoutDescriptor(def url, def project, def remotes, boolean overwrite) {
		this.url = url
		this.project = project
		this.remotes = remotes
		this.overwrite = overwrite
	}

	public File write(def root) {
		def extension = evaluate()
		extension.write(root)
		return root
	}

	public LayoutPluginExtension evaluate() {
		def jsonText = getLayoutJson()
		return evaluateJson(jsonText)
	}

	public String getBaseUrl() {
		if (url == null) {
			return null
		}
		def urlStr = url.toString()
		int idx = urlStr.lastIndexOf("/")
		return idx >= 0 ? urlStr.substring(0, idx + 1) : urlStr
	}

	private String getLayoutJson() {
		LOGGER.debug("Getting layout JSON from {}", url)
		def jsonText = url.toURL().getText("UTF-8")
		LOGGER.debug("Parsing Descriptor: {}", jsonText)
		return jsonText
	}

	private LayoutPluginExtension evaluateJson(String jsonText) {
		def parsed = new JsonSlurper().parseText(jsonText)
		LayoutPluginExtension extension = new LayoutPluginExtension()
		extension.project = this.project
		extension.baseUrl = getBaseUrl()
		extension.overwrite = this.overwrite
		if (this.remotes != null) {
			extension.remotes = this.remotes
		}
		if (!(parsed instanceof Map)) {
			return extension
		}
		Map descriptor = (Map) parsed
		if (descriptor.containsKey("overwrite")) {
			extension.overwrite((boolean) descriptor.get("overwrite"))
		}
		if (descriptor.get("names") instanceof Collection) {
			descriptor.get("names").each { def n ->
				extension.name(n)
			}
		}
		if (descriptor.get("urls") instanceof Collection) {
			descriptor.get("urls").each { def u ->
				extension.url(u)
			}
		}
		if (descriptor.get("paths") instanceof Collection) {
			descriptor.get("paths").each { def p ->
				extension.path(p)
			}
		}
		addFilesFromDescriptor(extension, descriptor.get("files"), false)
		addFilesFromDescriptor(extension, descriptor.get("executables"), true)
		return extension
	}

	private void addFilesFromDescriptor(LayoutPluginExtension extension, def filesNode, boolean defaultExecutable) {
		if (filesNode instanceof Collection) {
			filesNode.each { def entry ->
				if (entry instanceof CharSequence) {
					Map opts = [remote: true]
					if (defaultExecutable) {
						opts.put("executable", true)
					}
					extension.file(entry.toString(), opts)
				} else if (entry instanceof Map) {
					Map opts = new LinkedHashMap((Map) entry)
					def filePath = opts.remove("file") ?: opts.remove("path")
					if (defaultExecutable && !opts.containsKey("executable")) {
						opts.put("executable", true)
					}
					if (!opts.containsKey("remote") && !opts.containsKey("contents") && !opts.containsKey("url")) {
						opts.put("remote", true)
					}
					extension.file(filePath, opts)
				}
			}
		} else if (filesNode instanceof Map) {
			((Map) filesNode).each { def filePath, def val ->
				Map opts = new LinkedHashMap()
				if (val instanceof Map) {
					opts.putAll((Map) val)
				} else if (val instanceof CharSequence) {
					opts.put("contents", val.toString())
				}
				if (defaultExecutable && !opts.containsKey("executable")) {
					opts.put("executable", true)
				}
				if (!opts.containsKey("remote") && !opts.containsKey("contents") && !opts.containsKey("url")) {
					opts.put("remote", true)
				}
				extension.file(filePath.toString(), opts)
			}
		}
	}

}
