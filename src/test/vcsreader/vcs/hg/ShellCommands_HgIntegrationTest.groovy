package vcsreader.vcs.hg
import org.junit.BeforeClass
import org.junit.Test

import java.nio.charset.Charset

import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.hg.HgIntegrationTestConfig.getPathToHg
import static vcsreader.vcs.hg.HgIntegrationTestConfig.initTestConfig
import static vcsreader.vcs.hg.HgIntegrationTestConfig.referenceProject
import static vcsreader.vcs.hg.HgIntegrationTestConfig.revision
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

	@BeforeClass static void setupConfig() {
		initTestConfig()
	}

	private static final Charset utf8 = Charset.forName("UTF-8")
}
