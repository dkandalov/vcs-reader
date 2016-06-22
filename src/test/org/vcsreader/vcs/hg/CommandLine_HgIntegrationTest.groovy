package org.vcsreader.vcs.hg

import org.junit.Test

import static org.vcsreader.lang.CharsetUtil.UTF8
import static org.vcsreader.lang.DateTimeUtil.date
import static org.vcsreader.vcs.hg.HgClone.hgClone
import static org.vcsreader.vcs.hg.HgIntegrationTestConfig.getNonExistentPath
import static org.vcsreader.vcs.hg.HgIntegrationTestConfig.getPathToHg
import static org.vcsreader.vcs.hg.HgIntegrationTestConfig.newProjectPath
import static org.vcsreader.vcs.hg.HgLog.hgLog
import static org.vcsreader.vcs.hg.HgLogFileContent.hgLogFileContent
import static org.vcsreader.vcs.hg.HgRepository.Scripts.*
import static org.vcsreader.vcs.hg.HgUpdate.hgUpdate

class CommandLine_HgIntegrationTest {
	private final String projectFolder = newProjectPath()

	@Test void "basic log"() {
		def repository = 'repo with two commits with three added files'()

		def commandLine = hgLog(pathToHg, repository.path, date("01/01/2013"), date("01/01/2023")).execute()

		assert commandLine.stdout().contains("initial commit")
		assert commandLine.stderr() == ""
		assert commandLine.exceptionStacktrace() == ""
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log"() {
		def commandLine = hgLog(pathToHg, nonExistentPath, date("01/01/2013"), date("01/01/2023")).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr() == ""
		assert commandLine.exceptionStacktrace().matches(/(?s).*java.io.IOException.*No such file or directory.*/)
		assert commandLine.exitCode() != 0
	}

	@Test void "log file content"() {
		def repository = 'repo with two added and modified files'()

		def commandLine = hgLogFileContent(pathToHg, repository.path, "file1.txt", repository.revisions[0], UTF8).execute()

		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "file1 content"
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log file content"() {
		def repository = someNonEmptyRepository()

		def commandLine = hgLogFileContent(pathToHg, repository.path, "non-existent file", repository.revisions[0], UTF8).execute()

		assert commandLine.stdout() == ""
		assert commandLine.stderr().startsWith("non-existent file: no such file in rev")
		assert commandLine.exitCode() == 1
	}

	@Test void "clone repository"() {
		def repository = someNonEmptyRepository()

		def commandLine = hgClone(pathToHg, repository.path, projectFolder).execute()

		assert commandLine.stderr() == ""
		assert commandLine.stdout() != ""
		assert commandLine.exitCode() == 0
	}

	@Test void "failed clone of non-existent repository"() {
		def commandLine = hgClone(pathToHg, "file://" + nonExistentPath, projectFolder).execute()

		assert commandLine.stdout() == ""
		assert commandLine.stderr().startsWith("abort: repository ${nonExistentPath} not found!")
		assert commandLine.exitCode() == 255
	}

	@Test void "update repository"() {
		def repository = someNonEmptyRepository()
		hgClone(pathToHg, repository.path, projectFolder).execute()

		def commandLine = hgUpdate(pathToHg, projectFolder).execute()

		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("pulling from")
		assert commandLine.exitCode() == 0
	}

	@Test void "failed update of non-existing repository"() {
		def commandLine = hgUpdate(pathToHg, nonExistentPath).execute()

		assert commandLine.stdout() == ""
		assert commandLine.stderr() == ""
		assert commandLine.exceptionStacktrace().matches(/(?s).*java.io.IOException.*No such file or directory.*/)
	}

	@Test void "failed update without upstream repository"() {
		def repository = someNonEmptyRepository()
		hgClone(pathToHg, repository.path, projectFolder).execute()
		new File(repository.path).deleteDir() // delete upstream repo so that update fails

		def commandLine = hgUpdate(pathToHg, projectFolder).execute()

		assert commandLine.stdout().startsWith("pulling from")
		assert commandLine.stderr().trim().matches("abort: repository .* not found!")
		assert commandLine.exitCode() == 255
	}

	@Test void "log with non-ascii characters"() {
		def repository = 'repo with non-ascii file name and commit message'()

		def commandLine = hgLog(pathToHg, repository.path, date("01/01/2013"), date("01/01/2023")).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("non-ascii комментарий")
		assert commandLine.exitCode() == 0

		commandLine = hgLogFileContent(pathToHg, repository.path, "non-ascii.txt", repository.revisions[0], UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "non-ascii содержимое"
		assert commandLine.exitCode() == 0
	}
}
