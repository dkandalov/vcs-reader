package org.vcsreader.vcs.hg

import static org.vcsreader.lang.FileUtil.findSequentNonExistentFile
import static org.vcsreader.lang.FileUtil.tempDirectoryFile

class HgIntegrationTestConfig {
	static String pathToHg = "/usr/local/Cellar/mercurial/3.5/bin/hg"
	static String nonExistentPath = "/tmp/non-existent-path"
	static String author = "Some Author"
	static final String authorWithEmail = "Some Author <some.author@mail.com>"

	static String newReferenceRepoPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "hg-reference-repo-", "")
		assert file.mkdirs()
		file.deleteOnExit()
		file.absolutePath
	}

	static String newProjectPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "hg-repo-", "")
		assert file.mkdirs()
		file.deleteOnExit()
		file.absolutePath
	}
}
