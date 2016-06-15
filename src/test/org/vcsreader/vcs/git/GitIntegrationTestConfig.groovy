package org.vcsreader.vcs.git

import org.vcsreader.lang.FileUtil

class GitIntegrationTestConfig {
	static final String pathToGit = "/usr/local/Cellar/git/2.5.0/bin/git"
	static final String author = "Some Author"
	static final String authorWithEmail = "Some Author <some.author@mail.com>"
	static final String nonExistentPath = "/tmp/non-existent-path"

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
