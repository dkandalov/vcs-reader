package vcsreader.vcs.hg

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static vcsreader.lang.Charsets.UTF8
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.hg.HgClone.hgClone
import static vcsreader.vcs.hg.HgIntegrationTestConfig.*
import static vcsreader.vcs.hg.HgLog.hgLog
import static vcsreader.vcs.hg.HgLogFileContent.hgLogFileContent
import static vcsreader.vcs.hg.HgUpdate.hgUpdate

class CommandLine_HgIntegrationTest {
	@Test void "basic log"() {
		def commandLine = hgLog(pathToHg, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
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
		def commandLine = hgLogFileContent(pathToHg, referenceProject, "file1.txt", revision(1), UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "file1 content"
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log file content"() {
		def commandLine = hgLogFileContent(pathToHg, referenceProject, "non-existent file", revision(1), UTF8).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr().startsWith("non-existent file: no such file in rev")
		assert commandLine.exitCode() == 1
	}

	@Test void "clone repository"() {
		def commandLine = hgClone(pathToHg, "file://" + referenceProject, projectFolder).execute()
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
		hgClone(pathToHg, "file://" + referenceProject, projectFolder).execute()
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
		hgClone(pathToHg, "file://" + referenceProject, projectFolder).execute()
		hgClone(pathToHg, "file://" + projectFolder, projectFolder2).execute()
		new File(projectFolder).deleteDir()

		def commandLine = hgUpdate(pathToHg, projectFolder2).execute()
		assert commandLine.stdout().startsWith("pulling from")
		assert commandLine.stderr().trim().matches("abort: repository .* not found!")
		assert commandLine.exitCode() == 255
	}

	@Test void "log with non-ascii characters"() {
		def commandLine = hgLog(pathToHg, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("non-ascii комментарий")
		assert commandLine.exitCode() == 0

		commandLine = hgLogFileContent(pathToHg, referenceProject, "non-ascii.txt", revision(8), UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "non-ascii содержимое"
		assert commandLine.exitCode() == 0
	}

	@Before void setup() {
		new File(projectFolder).deleteDir()
		new File(projectFolder).mkdirs()
	}

	@BeforeClass static void setupConfig() {
		initTestConfig()
	}

	private static final String projectFolder = "/tmp/hg-commands-test/hg-repo-${CommandLine_HgIntegrationTest.simpleName}"
	private static final String projectFolder2 = "/tmp/hg-commands-test/hg-repo-2-${CommandLine_HgIntegrationTest.simpleName}"
}
