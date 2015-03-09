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
			folder.isDirectory();
	}
	
	def "create path with forward slash at start"() {
		when:
			def layoutPath = new LayoutPath("/folder1");
		then:
			layoutPath.write(temporaryFolder.getRoot());
		expect:
			File folder = new File(temporaryFolder.getRoot(), "folder1");
			folder.exists();
			folder.isDirectory();
	}
	
	def "create path with multiple subfolders"() {
		when:
			def layoutPath = new LayoutPath("/folder1/folder1_1/folder1_1_1");
		then:
			layoutPath.write(temporaryFolder.getRoot());
		expect:
			File folder = new File(temporaryFolder.getRoot(), "folder1/folder1_1/folder1_1_1");
			folder.exists(); 
			folder.isDirectory();
	}
	
	def "create path with file in the way"() {
		setup: 
			def file = temporaryFolder.newFile("file1");
		when:
			def layoutPath = new LayoutPath("/file1");
		then:
			layoutPath.write(temporaryFolder.getRoot());
		expect:
			File existingFile = new File(temporaryFolder.getRoot(), "file1")
			existingFile.exists()
			existingFile.isFile()
	}
	
	
}
