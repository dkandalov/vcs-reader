package org.vcsreader.vcs.svn

import static org.vcsreader.lang.FileUtil.*

class SvnIntegrationTestConfig {
	static String pathToSvn = System.getProperty("vcsreader.test.svnPath", "/usr/local/Cellar/subversion/1.8.13/bin/svn")
	static String pathToSvnAdmin = System.getProperty("vcsreader.test.svnAdminPath", "/usr/local/Cellar/subversion/1.8.13/bin/svnadmin")
	static String nonExistentUrl = "file://non-existent/url"
	static String author = "Some Author"

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
