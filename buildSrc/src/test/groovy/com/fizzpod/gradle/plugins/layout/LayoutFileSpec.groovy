/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LayoutFileSpec extends Specification {

	@Rule
	TemporaryFolder temporaryFolder
	
	def "create file in root folder"() {
		when:
			def layoutFile = new LayoutFile("file1")
		then:
			layoutFile.write(temporaryFolder.getRoot())
		expect:
			File file = new File(temporaryFolder.getRoot(), "file1")
			file.exists()
			file.isFile()
	}
	
	def "create file with forward slash at start"() {
		when:
			def layoutFile = new LayoutFile("/file1")
		then:
			layoutFile.write(temporaryFolder.getRoot())
		expect:
			File file = new File(temporaryFolder.getRoot(), "file1")
			file.exists()
			file.isFile()
	}
	
	def "create file in multiple subfolders"() {
		when:
			def layoutFile = new LayoutFile("/folder1/folder1_1/folder1_1_1/file1")
		then:
			layoutFile.write(temporaryFolder.getRoot())
		expect:
			File file = new File(temporaryFolder.getRoot(), "folder1/folder1_1/folder1_1_1/file1")
			file.exists()
			file.isFile()
	}
	
	def "create file with folder in the way"() {
		setup:
			def file = temporaryFolder.newFolder("folder1")
		when:
			def layoutFile = new LayoutFile("/folder1")
		then:
			layoutFile.write(temporaryFolder.getRoot())
		expect:
			File existingFolder = new File(temporaryFolder.getRoot(), "folder1")
			existingFolder.exists()
			existingFolder.isDirectory()
	}
	
	def "create file with file in sub folder position"() {
		setup:
		def file = temporaryFolder.newFile("file1")
		when:
			def layoutFile = new LayoutFile("/file1/folder1_1/file1_1")
		then:
			layoutFile.write(temporaryFolder.getRoot())
		expect:
			File existingFile = new File(temporaryFolder.getRoot(), "file1")
			existingFile.exists()
			existingFile.isFile()
			File newFile = new File(temporaryFolder.getRoot(), "/file1/folder1_1/file1_1")
			!newFile.exists()
	}
	
	def "create file with content"() {
		when:
			def layoutFile = new LayoutFile("/file1", {
				"I am content"
			})
		then:
			layoutFile.write(temporaryFolder.getRoot())
		expect:
			File newFile = new File(temporaryFolder.getRoot(), "file1")
			newFile.exists()
			newFile.getText().equals("I am content")
	}
	
	def "create file with multiline content"() {
		when:
			def layoutFile = new LayoutFile("/file1", {
				"""I am content line 1
Line 2
Line3"""
			})
		then:
			layoutFile.write(temporaryFolder.getRoot())
		expect:
			File newFile = new File(temporaryFolder.getRoot(), "file1")
			newFile.exists()
			newFile.getText().equals("""I am content line 1
Line 2
Line3""")
	}
	
}
