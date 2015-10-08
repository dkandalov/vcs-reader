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

class ShellCommands_HgIntegrationTest {
	@Test void "hg log"() {
		def command = hgLog(pathToHg, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert command.stderr() == ""
		assert command.stdout().contains("initial commit")
		assert command.exitCode() == 0
	}

	@Test void "hg log file content"() {
		def command = hgLogFileContent(pathToHg, referenceProject, "file1.txt", revision(1), utf8).execute()
		assert command.stderr() == ""
		assert command.stdout().trim() == "file1 content"
		assert command.exitCode() == 0
	}

	@Test void "clone repository"() {
		def command = hgClone(pathToHg, "file://" + referenceProject, projectFolder).execute()
		assert command.stdout() != ""
		assert command.stderr().trim() == ""
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
}
