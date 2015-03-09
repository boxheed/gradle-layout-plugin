package com.fizzpod.gradle.plugins.layout

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class LayoutPathSpec extends Specification {

	@Rule
	TemporaryFolder temporaryFolder
	
	def "create path in root folder"() {
		when: 
			def layoutPath = new LayoutPath("folder1");
		then:
			layoutPath.write(temporaryFolder.getRoot());
		expect:
			File folder = new File(temporaryFolder.getRoot(), "folder1");
			folder.exists();
	}
	
	def "create path with forward slash at start"() {
		when:
			def layoutPath = new LayoutPath("/folder1");
		then:
			layoutPath.write(temporaryFolder.getRoot());
		expect:
			File folder = new File(temporaryFolder.getRoot(), "folder1");
			folder.exists();
	}
	
	def "create path with multiple subfolders"() {
		when:
			def layoutPath = new LayoutPath("/folder1/folder1_1/folder1_1_1");
		then:
			layoutPath.write(temporaryFolder.getRoot());
		expect:
			File folder = new File(temporaryFolder.getRoot(), "/folder1/folder1_1/folder1_1_1");
			folder.exists(); 
	}
	
}
