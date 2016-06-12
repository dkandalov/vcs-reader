package org.vcsreader.vcs.git

import org.vcsreader.lang.FileUtil

class GitIntegrationTestConfig {
	static String pathToGit = "/usr/local/Cellar/git/2.5.0/bin/git"
	static String author = "Some Author"
	static String authorWithEmail = "Some Author <some.author@mail.com>"

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
