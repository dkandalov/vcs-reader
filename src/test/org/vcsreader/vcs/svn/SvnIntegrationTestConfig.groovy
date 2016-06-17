package org.vcsreader.vcs.svn

import groovy.json.JsonSlurper

import static org.vcsreader.lang.FileUtil.deleteOnShutdown
import static org.vcsreader.lang.FileUtil.findSequentNonExistentFile
import static org.vcsreader.lang.FileUtil.tempDirectoryFile

class SvnIntegrationTestConfig {
	static String pathToSvn = System.getProperty("vcsreader.test.svnPath", "/usr/local/Cellar/subversion/1.8.13/bin/svn")
	static String pathToSvnAdmin = System.getProperty("vcsreader.test.svnAdminPath", "/usr/local/Cellar/subversion/1.8.13/bin/svnadmin")
	static String repositoryUrl
	static String nonExistentUrl
	static String author = "Some Author"

	static initTestConfig() {
		def configFile = new File("src/test/org/vcsreader/vcs/svn/svn-test-config.json")
		if (!configFile.exists()) {
			throw new IllegalStateException("Cannot find " + configFile.name + ".\n" +
					"Please make sure you run tests with project root as working directory.")
		}
		def config = new JsonSlurper().parse(configFile)
		pathToSvn = config["pathToSvn"] as String
		repositoryUrl = config["svnRepositoryUrl"] as String
		nonExistentUrl = "file://non-existent/url"
		author = config["author"] as String

		if (!new File(pathToSvn).exists())
			throw new FileNotFoundException("Cannot find '" + pathToSvn + "'. Please check content of '" + configFile.absolutePath + "'")
		if (!new File(repositoryUrl.replace("file://", "")).exists())
			throw new FileNotFoundException("Cannot find '" + repositoryUrl + "'. Please run svn-create-repo.rb")
	}

	static String revision(int n) {
		String.valueOf(n)
	}

	static String newReferenceRepoPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "svn-reference-repo-", "")
		assert file.mkdirs()
		deleteOnShutdown(file)
		file.absolutePath
	}

	static String newProjectPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "svn-project-", "")
		assert file.mkdirs()
		deleteOnShutdown(file)
		file.absolutePath
	}
}
