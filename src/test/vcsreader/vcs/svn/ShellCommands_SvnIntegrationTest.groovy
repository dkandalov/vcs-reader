package vcsreader.vcs.svn

import org.junit.BeforeClass
import org.junit.Test

import java.nio.charset.Charset

import static SvnInfo.svnInfo
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.svn.SvnIntegrationTestConfig.*
import static vcsreader.vcs.svn.SvnLog.svnLog
import static vcsreader.vcs.svn.SvnLogFileContent.svnLogFileContent

class ShellCommands_SvnIntegrationTest {
    @Test void "svn log"() {
        def command = svnLog(pathToSvn, repositoryUrl, date("01/01/2013"), date("01/01/2023"), useMergeHistory).execute()
        assert command.stderr() == ""
        assert command.stdout().contains("initial commit")
        assert command.exitValue() == 0
    }

    @Test void "failed svn log"() {
        def command = svnLog(pathToSvn, nonExistentUrl, date("01/01/2013"), date("01/01/2023"), useMergeHistory).execute()
        assert !command.stdout().contains("logentry")
        assert command.stderr().contains("Unable to connect to a repository")
        assert command.exitValue() == 1
    }

    @Test void "svn log file content"() {
        def revision = "1"
        def command = svnLogFileContent(pathToSvn, repositoryUrl, "file1.txt", revision, utf8).execute()
        assert command.stderr() == ""
        assert command.stdout().trim() == "file1 content"
        assert command.exitValue() == 0
    }

    @Test void "failed svn log file content"() {
        def revision = "1"
        def command = svnLogFileContent(pathToSvn, repositoryUrl, "non-existent-file", revision, utf8).execute()
        assert command.stdout() == ""
        assert command.stderr().contains("path not found")
        assert command.exitValue() == 1
    }

    @Test void "get repository information"() {
        def command = svnInfo(pathToSvn, repositoryUrl).execute()
        assert command.stderr() == ""
        assert command.stdout().contains("Repository Root: " + repositoryUrl)
        assert command.exitValue() == 0
    }

    @Test void "failed to get repository information"() {
        def command = svnInfo(pathToSvn, nonExistentUrl).execute()
        assert command.stdout() == ""
        assert command.stderr().contains("Unable to connect to a repository")
        assert command.exitValue() == 1
    }

    @BeforeClass static void setupConfig() {
        initTestConfig()
    }

    private static final utf8 = Charset.forName("UTF-8")
    private static final useMergeHistory = true
}
