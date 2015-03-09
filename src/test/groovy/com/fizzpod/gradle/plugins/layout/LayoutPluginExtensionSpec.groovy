package com.fizzpod.gradle.plugins.layout;

import spock.lang.Specification

public class LayoutPluginExtensionSpec extends Specification {
	
	LayoutPluginExtension extension = new LayoutPluginExtension();
	
	def "Adding path to extension"() {
		when:
			extension.path("path")
		then:
			extension.getLayouts().size() == 1;
			extension.getLayouts()[0].path == "path"
	}
	
	def "Adding file to extension"() {
		when:
			extension.file("file", {})
		then:
			extension.getLayouts().size() == 1;
			extension.getLayouts()[0].file == "file"
	}
	
	def "Adding path and file to extension"() {
		when:
			extension.path("path")
			extension.file("file", {})
		then:
			extension.getLayouts().size() == 2;
			extension.getLayouts()[0].path == "path"
			extension.getLayouts()[1].file == "file"
	}
}
