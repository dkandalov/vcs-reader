package vcsreader.vcs.hg

import org.junit.BeforeClass
import org.junit.Test

import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.hg.HgIntegrationTestConfig.*
import static vcsreader.vcs.hg.HgLog.hgLog

class ShellCommands_HgIntegrationTest {
	@Test void "hg log"() {
		def command = hgLog(pathToHg, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert command.stderr() == ""
		assert command.stdout().contains("initial commit")
		assert command.exitCode() == 0
	}

	@BeforeClass static void setupConfig() {
		initTestConfig()
	}
}
