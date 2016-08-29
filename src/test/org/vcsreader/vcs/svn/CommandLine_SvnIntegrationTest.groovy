package org.vcsreader.vcs.svn

import org.junit.Test

import static org.vcsreader.lang.CharsetUtil.UTF8
import static org.vcsreader.lang.DateTimeUtil.timeRange
import static org.vcsreader.vcs.svn.SvnInfo.svnInfo
import static org.vcsreader.vcs.svn.SvnIntegrationTestConfig.nonExistentUrl
import static org.vcsreader.vcs.svn.SvnIntegrationTestConfig.pathToSvn
import static org.vcsreader.vcs.svn.SvnLog.svnLog
import static org.vcsreader.vcs.svn.SvnLogFileContent.svnLogFileContent
import static org.vcsreader.vcs.svn.SvnRepository.Scripts.*

class CommandLine_SvnIntegrationTest {
	@Test void "basic log"() {
		def repository = 'repo with two commits with three added files'()

		def commandLine = svnLog(pathToSvn, "file://" + repository.repoPath, timeRange("01/01/2013", "01/01/2023"), useMergeHistory, quoteDateRange).execute()

		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("initial commit")
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log"() {
		def commandLine = svnLog(pathToSvn, nonExistentUrl, timeRange("01/01/2013", "01/01/2023"), useMergeHistory, quoteDateRange).execute()

		assert !commandLine.stdout().contains("logentry")
		assert commandLine.stderr().contains("Unable to connect to a repository")
		assert commandLine.exitCode() == 1
	}

	@Test void "log file content"() {
		def repository = 'repo with two added and modified files'()

		def commandLine = svnLogFileContent(pathToSvn, "file://" + repository.repoPath, "file1.txt", repository.revisions[0], UTF8).execute()

		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "file1 content"
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log file content"() {
		def repository = someNonEmptyRepository()

		def commandLine = svnLogFileContent(pathToSvn, "file://" + repository.repoPath, "non-existent-file", repository.revisions[0], UTF8).execute()

		assert commandLine.stdout() == ""
		assert commandLine.stderr().contains("path not found")
		assert commandLine.exitCode() == 1
	}

	@Test void "get repository information"() {
		def repository = someNonEmptyRepository()

		def commandLine = svnInfo(pathToSvn, "file://" + repository.repoPath).execute()

		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("Repository Root: file://$repository.repoPath")
		assert commandLine.exitCode() == 0
	}

	@Test void "failed to get repository information"() {
		def commandLine = svnInfo(pathToSvn, nonExistentUrl).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr().contains("Unable to connect to a repository")
		assert commandLine.exitCode() == 1
	}

	@Test void "log with non-ascii characters"() {
		def repository = 'repo with non-ascii file name and commit message'()

		def commandLine = svnLog(pathToSvn, "file://" + repository.repoPath, timeRange("01/01/2013", "01/01/2023"), useMergeHistory, quoteDateRange).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("non-ascii комментарий")
		assert commandLine.exitCode() == 0

		commandLine = svnLogFileContent(pathToSvn, "file://" + repository.repoPath, "non-ascii.txt", repository.revisions[1], UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "non-ascii содержимое"
		assert commandLine.exitCode() == 0
	}

	private static final useMergeHistory = true
	private static final quoteDateRange = false
}
