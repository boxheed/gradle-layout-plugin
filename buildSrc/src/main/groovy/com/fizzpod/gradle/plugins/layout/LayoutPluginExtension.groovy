/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class LayoutPluginExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(LayoutPluginExtension.class)

	private static final String DEFAULT_LAYOUT_FILE = "layout.json"
	private static final String DEFAULT_REMOTE_ROOT = "https://raw.githubusercontent.com/boxheed/gradle-layout-plugin-layouts/master/"

	def remotes = [DEFAULT_REMOTE_ROOT]
	def layouts = []
	boolean overwrite = false
	def project
	String baseUrl

	def overwrite(boolean overwrite) {
		this.overwrite = overwrite
		return this
	}

	def file(def file) {
		return createFile([:], file, null)
	}

	def file(def first, def second) {
		if (first instanceof Map) {
			return createFile((Map) first, second, null)
		}
		if (second instanceof Map) {
			return createFile((Map) second, first, null)
		}
		return createFile([:], first, second)
	}

	def file(def first, def second, def third) {
		if (first instanceof Map) {
			return createFile((Map) first, second, third)
		}
		if (second instanceof Map) {
			return createFile((Map) second, first, third)
		}
		return createFile([:], first, second)
	}

	private def createFile(Map options, def file, def contents) {
		Map explicitOpts = options != null ? new LinkedHashMap(options) : new LinkedHashMap()
		Map effectiveOptions = new LinkedHashMap()
		effectiveOptions.put("overwrite", this.overwrite)
		effectiveOptions.put("project", this.project)
		if (options != null) {
			effectiveOptions.putAll(options)
		}
		if (effectiveOptions.get("url") != null) {
			String resolvedUrl = resolveUrl(effectiveOptions.get("url").toString())
			effectiveOptions.put("url", resolvedUrl)
			explicitOpts.put("url", resolvedUrl)
		} else if (effectiveOptions.get("remote")) {
			String pathStr = file.toString()
			String cleanPath = pathStr.startsWith("/") ? pathStr.substring(1) : pathStr
			String resolvedUrl = resolveUrl(cleanPath)
			effectiveOptions.put("url", resolvedUrl)
			explicitOpts.put("url", resolvedUrl)
		}
		effectiveOptions.put("explicitOptions", explicitOpts)
		layouts << new LayoutFile(file, effectiveOptions, contents)
		return this
	}

	public String resolveUrl(String target) {
		if (target == null) {
			return null
		}
		if (target.startsWith("http://") || target.startsWith("https://") || target.startsWith("file:")) {
			return target
		}
		String rootUrl = this.baseUrl ?: (this.remotes ? this.remotes[0].toString() : DEFAULT_REMOTE_ROOT)
		if (!rootUrl.endsWith("/")) {
			rootUrl = rootUrl + "/"
		}
		String cleanTarget = target.startsWith("/") ? target.substring(1) : target
		return rootUrl + cleanTarget
	}

	def path(def path) {
		layouts << new LayoutPath(path)
		return this
	}

	def url(def url) {
		layouts << new LayoutDescriptor(url, this.project, this.remotes, this.overwrite)
		return this
	}

	def name(def name) {
		String rootUrl = this.remotes[0].toString()
		if (!rootUrl.endsWith("/")) {
			rootUrl = rootUrl + "/"
		}
		def url = rootUrl + name + "/" + DEFAULT_LAYOUT_FILE
		LOGGER.info("Mapping name {} to URL {}", name, url)
		this.url(url)
		return this
	}

	def remote(def remote) {
		String remoteStr = remote.toString()
		if (!remoteStr.endsWith("/")) {
			remoteStr = remoteStr + "/"
		}
		remotes = [remoteStr]
		return this
	}

	public List resolveLayouts() {
		List resolvedPaths = []
		Map<String, LayoutFile> resolvedFiles = new LinkedHashMap<>()
		collectLayouts(this.layouts, resolvedPaths, resolvedFiles)
		List result = []
		result.addAll(resolvedPaths)
		result.addAll(resolvedFiles.values())
		return result
	}

	private void collectLayouts(List items, List resolvedPaths, Map<String, LayoutFile> resolvedFiles) {
		items.each { def item ->
			if (item instanceof LayoutPath) {
				resolvedPaths << item
			} else if (item instanceof LayoutFile) {
				String key = item.getNormalizedPath()
				if (resolvedFiles.containsKey(key)) {
					resolvedFiles.get(key).merge(item)
				} else {
					resolvedFiles.put(key, item)
				}
			} else if (item instanceof LayoutDescriptor) {
				LayoutPluginExtension childExt = item.evaluate()
				collectLayouts(childExt.layouts, resolvedPaths, resolvedFiles)
			}
		}
	}

	public File write(def root) {
		resolveLayouts().each {
			it.write(root)
		}
		return root
	}

	def call(Closure closure) {
		LOGGER.debug("Registering as delegate with closure {}", closure)
		closure.setDelegate(this)
		closure.call()
	}

}
