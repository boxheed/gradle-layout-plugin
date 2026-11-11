/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import spock.lang.Specification

public class LayoutPluginExtensionSpec extends Specification {

	LayoutPluginExtension extension = new LayoutPluginExtension()

	def "Adding path to extension"() {
		when:
			extension.path("path")
		then:
			extension.getLayouts().size() == 1
			extension.getLayouts()[0].path == "path"
	}

	def "Adding file to extension"() {
		when:
			extension.file("file", {})
		then:
			extension.getLayouts().size() == 1
			extension.getLayouts()[0].file == "file"
	}

	def "Adding path and file to extension"() {
		when:
			extension.path("path")
			extension.file("file", {})
		then:
			extension.getLayouts().size() == 2
			extension.getLayouts()[0].path == "path"
			extension.getLayouts()[1].file == "file"
	}

	def "Adding file with options map to extension"() {
		when:
			extension.baseUrl = "https://example.com/layouts/common/"
			extension.file(".github/workflows/ci.yml", [remote: true, overwrite: true, executable: true])
		then:
			extension.getLayouts().size() == 1
			LayoutFile lf = extension.getLayouts()[0]
			lf.file == ".github/workflows/ci.yml"
			lf.overwrite == true
			lf.executable == true
			lf.url == "https://example.com/layouts/common/.github/workflows/ci.yml"
	}

	def "Setting global overwrite propagates to files unless overridden"() {
		when:
			extension.overwrite(true)
			extension.file("file1.txt")
			extension.file("file2.txt", [overwrite: false])
		then:
			extension.getLayouts().size() == 2
			extension.getLayouts()[0].overwrite == true
			extension.getLayouts()[1].overwrite == false
	}

}
