package vcsreader.vcs.svn

import org.junit.BeforeClass
import org.junit.Test

import static SvnInfo.svnInfo
import static vcsreader.lang.Charsets.UTF8
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.svn.SvnIntegrationTestConfig.*
import static vcsreader.vcs.svn.SvnLog.svnLog
import static vcsreader.vcs.svn.SvnLogFileContent.svnLogFileContent

class CommandLine_SvnIntegrationTest {
	@Test void "basic log"() {
		def commandLine = svnLog(pathToSvn, repositoryUrl, date("01/01/2013"), date("01/01/2023"), useMergeHistory, quoteDateRange).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("initial commit")
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log"() {
		def commandLine = svnLog(pathToSvn, nonExistentUrl, date("01/01/2013"), date("01/01/2023"), useMergeHistory, quoteDateRange).execute()
		assert !commandLine.stdout().contains("logentry")
		assert commandLine.stderr().contains("Unable to connect to a repository")
		assert commandLine.exitCode() == 1
	}

	@Test void "log file content"() {
		def revision = "1"
		def commandLine = svnLogFileContent(pathToSvn, repositoryUrl, "file1.txt", revision, UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "file1 content"
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log file content"() {
		def revision = "1"
		def commandLine = svnLogFileContent(pathToSvn, repositoryUrl, "non-existent-file", revision, UTF8).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr().contains("path not found")
		assert commandLine.exitCode() == 1
	}

	@Test void "get repository information"() {
		def commandLine = svnInfo(pathToSvn, repositoryUrl).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("Repository Root: " + repositoryUrl)
		assert commandLine.exitCode() == 0
	}

	@Test void "failed to get repository information"() {
		def commandLine = svnInfo(pathToSvn, nonExistentUrl).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr().contains("Unable to connect to a repository")
		assert commandLine.exitCode() == 1
	}

	@Test void "log with non-ascii characters"() {
		def commandLine = svnLog(pathToSvn, repositoryUrl, date("01/01/2013"), date("01/01/2023"), useMergeHistory, quoteDateRange).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("non-ascii комментарий")
		assert commandLine.exitCode() == 0

		def revision = "8"
		commandLine = svnLogFileContent(pathToSvn, repositoryUrl, "non-ascii.txt", revision, UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "non-ascii содержимое"
		assert commandLine.exitCode() == 0
	}

	@BeforeClass static void setupConfig() {
		initTestConfig()
	}

	private static final useMergeHistory = true
	private static final quoteDateRange = false
}
