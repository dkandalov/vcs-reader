package org.vcsreader.vcs.hg

import static org.vcsreader.lang.FileUtil.deleteOnShutdown
import static org.vcsreader.lang.FileUtil.findSequentNonExistentFile
import static org.vcsreader.lang.FileUtil.tempDirectoryFile

class HgIntegrationTestConfig {
	static final String pathToHg = System.getProperty("vcsreader.test.hgPath", "/usr/local/Cellar/mercurial/3.5/bin/hg")
	static final String author = "Some Author"
	static final String authorWithEmail = "Some Author <some.author@mail.com>"
	static final String nonExistentPath = "/tmp/non-existent-path"

	static String newReferenceRepoPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "hg-reference-repo-", "")
		assert file.mkdirs()
		deleteOnShutdown(file)
		file.absolutePath
	}

	static String newProjectPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "hg-project-repo-", "")
		assert file.mkdirs()
		deleteOnShutdown(file)
		file.absolutePath
	}
}
