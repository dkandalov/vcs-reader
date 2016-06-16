package org.vcsreader.vcs.git

import static org.vcsreader.lang.FileUtil.findSequentNonExistentFile
import static org.vcsreader.lang.FileUtil.tempDirectoryFile

class GitIntegrationTestConfig {
	static final String pathToGit = System.getProperty("vcsreader.gitpath", "/usr/local/Cellar/git/2.5.0/bin/git")
	static final String author = "Some Author"
	static final String authorWithEmail = "Some Author <some.author@mail.com>"
	static final String nonExistentPath = "/tmp/non-existent-path"

	static String newReferenceRepoPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "git-reference-repo-", "")
		assert file.mkdirs()
		file.deleteOnExit()
		file.absolutePath
	}

	static String newProjectPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "git-repo-", "")
		assert file.mkdirs()
		file.deleteOnExit()
		file.absolutePath
	}
}
