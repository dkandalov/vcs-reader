package org.vcsreader.vcs.git

import groovy.json.JsonSlurper
import org.vcsreader.lang.FileUtil

class GitIntegrationTestConfig {
	static String nonExistentPath = "/tmp/non-existent-path"
	private static String pathToGit
	static String author

	private static boolean initialized = false

	static String getPathToGit() {
		if (!initialized) {
			initTestConfig()
			initialized = true
		}
		pathToGit
	}

	static initTestConfig() {
		def configFile = new File("src/test/org/vcsreader/vcs/git/git-test-config.json")
		if (!configFile.exists()) {
			throw new IllegalStateException("Cannot find " + configFile.name + ".\n" +
					"Please make sure you run tests with project root as working directory.")
		}
		def config = new JsonSlurper().parse(configFile)
		pathToGit = config["pathToGit"] as String
		author = config["author"] as String

		if (!new File(pathToGit).exists())
			throw new FileNotFoundException("Cannot find '" + pathToGit + "'. Please check content of '" + configFile.absolutePath + "'")
	}

	static String newReferenceRepoPath() {
		def file = FileUtil.findSequentNonExistentFile(FileUtil.tempDirectoryFile(), "git-reference-repo-", "")
		assert file.mkdirs()
		file.deleteOnExit()
		file.absolutePath
	}

	static String newProjectPath() {
		def file = FileUtil.findSequentNonExistentFile(FileUtil.tempDirectoryFile(), "git-repo-${GitIntegrationTest.simpleName}", "")
		assert file.mkdirs()
		file.deleteOnExit()
		file.absolutePath
	}
}
