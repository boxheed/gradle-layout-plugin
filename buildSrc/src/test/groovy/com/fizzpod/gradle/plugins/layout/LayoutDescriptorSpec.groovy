/* (C) 2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.layout

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LayoutDescriptorSpec extends Specification {

	@Rule
	TemporaryFolder temporaryFolder

	def "Evaluate composite JSON layout descriptor with remote files and project context"() {
		setup:
			File remoteBase = temporaryFolder.newFolder("remote")
			File licenseDir = new File(remoteBase, "license-apache-2.0")
			licenseDir.mkdirs()
			new File(licenseDir, "LICENSE").setText("Apache 2.0", "UTF-8")
			new File(licenseDir, "layout.json").setText("""
{
  "files": ["LICENSE"]
}
""", "UTF-8")

			File osvDir = new File(remoteBase, "security-osv-remediation")
			new File(osvDir, ".github/workflows").mkdirs()
			new File(osvDir, ".github/scripts").mkdirs()
			new File(osvDir, ".github/workflows/ci.yml").setText("name: CI", "UTF-8")
			new File(osvDir, ".github/scripts/run.sh").setText("#!/bin/sh\necho ok", "UTF-8")
			new File(osvDir, "layout.json").setText("""
{
  "overwrite": true,
  "files": [
    ".github/workflows/ci.yml"
  ],
  "executables": [
    ".github/scripts/run.sh"
  ]
}
""", "UTF-8")

			File pluginDir = new File(remoteBase, "gradle-plugin")
			new File(pluginDir, ".circleci").mkdirs()
			new File(pluginDir, ".circleci/config.yml").setText("standard circleci config", "UTF-8")
			new File(pluginDir, "layout.json").setText("""
{
  "names": [
    "license-apache-2.0",
    "security-osv-remediation"
  ],
  "files": {
    "README.md": { "contents": "# \${project.name}", "overwrite": false },
    ".circleci/config.yml": { "overwrite": true }
  }
}
""", "UTF-8")

			File targetRoot = temporaryFolder.newFolder("project")
			// Pre-populate an old workflow file to verify overwrite works
			File existingWorkflow = new File(targetRoot, ".github/workflows/ci.yml")
			existingWorkflow.getParentFile().mkdirs()
			existingWorkflow.setText("old workflow", "UTF-8")

			Project project = ProjectBuilder.builder()
				.withName("my-sample-plugin")
				.withProjectDir(targetRoot)
				.build()

			LayoutPluginExtension ext = new LayoutPluginExtension()
			ext.project = project
			ext.remote(remoteBase.toURI().toURL().toString())
			ext.name("gradle-plugin")

		when:
			ext.write(targetRoot)

		then:
			new File(targetRoot, "LICENSE").getText("UTF-8") == "Apache 2.0"
			new File(targetRoot, "README.md").getText("UTF-8") == "# my-sample-plugin"
			new File(targetRoot, ".circleci/config.yml").getText("UTF-8") == "standard circleci config"
			new File(targetRoot, ".github/workflows/ci.yml").getText("UTF-8") == "name: CI"
			File script = new File(targetRoot, ".github/scripts/run.sh")
			script.exists()
			script.getText("UTF-8") == "#!/bin/sh\necho ok"
			script.canExecute()
	}

	def "Local file configuration overrides remote layout overwrite flag"() {
		setup:
			File remoteBase = temporaryFolder.newFolder("remote")
			File pluginDir = new File(remoteBase, "gradle-plugin")
			new File(pluginDir, ".circleci").mkdirs()
			new File(pluginDir, ".circleci/config.yml").setText("standard circleci config", "UTF-8")
			new File(pluginDir, "AGENTS.md").setText("standard agents md", "UTF-8")
			new File(pluginDir, "layout.json").setText("""
{
  "overwrite": true,
  "files": [
    ".circleci/config.yml",
    "AGENTS.md"
  ]
}
""", "UTF-8")

			File targetRoot = temporaryFolder.newFolder("clusterfuzz-project")
			File customCircleCi = new File(targetRoot, ".circleci/config.yml")
			customCircleCi.getParentFile().mkdirs()
			customCircleCi.setText("custom clusterfuzz circleci config", "UTF-8")

			LayoutPluginExtension ext = new LayoutPluginExtension()
			ext.remote(remoteBase.toURI().toURL().toString())
			ext.name("gradle-plugin")
			// Local override: keep custom .circleci/config.yml, do not overwrite
			ext.file(".circleci/config.yml", [overwrite: false])

		when:
			ext.write(targetRoot)

		then:
			new File(targetRoot, ".circleci/config.yml").getText("UTF-8") == "custom clusterfuzz circleci config"
			new File(targetRoot, "AGENTS.md").getText("UTF-8") == "standard agents md"
	}

}
