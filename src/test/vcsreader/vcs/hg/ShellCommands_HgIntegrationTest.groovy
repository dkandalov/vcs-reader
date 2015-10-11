package vcsreader.vcs.hg
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import java.nio.charset.Charset

import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.hg.HgClone.hgClone
import static vcsreader.vcs.hg.HgIntegrationTestConfig.*
import static vcsreader.vcs.hg.HgLog.hgLog
import static vcsreader.vcs.hg.HgLogFileContent.hgLogFileContent
import static vcsreader.vcs.hg.HgUpdate.hgUpdate

class ShellCommands_HgIntegrationTest {
	@Test void "basic log"() {
		def command = hgLog(pathToHg, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert command.stderr() == ""
		assert command.stdout().contains("initial commit")
		assert command.exitCode() == 0
	}

	@Test void "failed log"() {
		def command = hgLog(pathToHg, nonExistentPath, date("01/01/2013"), date("01/01/2023")).execute()
		assert command.stdout() == ""
		assert command.stderr() != ""
		assert command.exitCode() != 0
	}

	@Test void "log file content"() {
		def command = hgLogFileContent(pathToHg, referenceProject, "file1.txt", revision(1), utf8).execute()
		assert command.stderr() == ""
		assert command.stdout().trim() == "file1 content"
		assert command.exitCode() == 0
	}

	@Test void "failed log file content"() {
		def command = hgLogFileContent(pathToHg, referenceProject, "non-existent file", revision(1), utf8).execute()
		assert command.stdout() == ""
		assert command.stderr().startsWith("non-existent file: no such file in rev")
		assert command.exitCode() == 1
	}

	@Test void "clone repository"() {
		def command = hgClone(pathToHg, "file://" + referenceProject, projectFolder).execute()
		assert command.stderr() == ""
		assert command.stdout() != ""
		assert command.exitCode() == 0
	}

	@Test void "failed clone of non-existent repository"() {
		def command = hgClone(pathToHg, "file://" + nonExistentPath, projectFolder).execute()
		assert command.stdout() == ""
		assert command.stderr().startsWith("abort: repository ${nonExistentPath} not found!")
		assert command.exitCode() == 255
	}

	@Test void "update repository"() {
		hgClone(pathToHg, "file://" + referenceProject, projectFolder).execute()
		def command = hgUpdate(pathToHg, projectFolder).execute()
		assert command.stderr() == ""
		assert command.stdout().contains("pulling from")
		assert command.exitCode() == 0
	}

	@Test void "failed update of non-existing repository"() {
		def command = hgUpdate(pathToHg, nonExistentPath).execute()
		assert command.stdout() == ""
		assert command.stderr() != ""
		assert command.exitCode() != 0
	}

	@Test void "failed update without upstream repository"() {
		hgClone(pathToHg, "file://" + referenceProject, projectFolder).execute()
		hgClone(pathToHg, "file://" + projectFolder, projectFolder2).execute()
		new File(projectFolder).deleteDir()

		def command = hgUpdate(pathToHg, projectFolder2).execute()
		assert command.stdout().trim() == "pulling from ${projectFolder}"
		assert command.stderr().contains("abort: repository ${projectFolder} not found!")
		assert command.exitCode() == 255
	}

	@Test void "log with non-ascii characters"() {
		def command = hgLog(pathToHg, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert command.stderr() == ""
		assert command.stdout().contains("non-ascii комментарий")
		assert command.exitCode() == 0

		command = hgLogFileContent(pathToHg, referenceProject, "non-ascii.txt", revision(8), utf8).execute()
		assert command.stderr() == ""
		assert command.stdout().trim() == "non-ascii содержимое"
		assert command.exitCode() == 0
	}

	@Before void setup() {
		new File(projectFolder).deleteDir()
		new File(projectFolder).mkdirs()
	}

	@BeforeClass static void setupConfig() {
		initTestConfig()
	}

	private static final Charset utf8 = Charset.forName("UTF-8")
	private static final String projectFolder = "/tmp/hg-commands-test/hg-repo-${ShellCommands_HgIntegrationTest.simpleName}"
	private static final String projectFolder2 = "/tmp/hg-commands-test/hg-repo-2-${ShellCommands_HgIntegrationTest.simpleName}"
}
