package vcsreader.vcs.svn
import org.junit.Before
import org.junit.Test

import java.nio.charset.Charset

import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.svn.SvnIntegrationTestConfig.getPathToSvn
import static vcsreader.vcs.svn.SvnIntegrationTestConfig.repositoryUrl
import static vcsreader.vcs.svn.SvnLog.svnLog

class ShellCommands_SvnIntegrationTest {
    @Test void "svn log"() {
        def command = svnLog(pathToSvn, repositoryUrl, date("01/01/2013"), date("01/01/2023"))
        assert command.stdout().contains("initial commit")
        assert command.stderr() == ""
        assert command.exitValue() == 0
    }

    @Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
        new File(projectFolder2).deleteDir()
        new File(projectFolder2).mkdirs()
    }

    private static final Charset utf8 = Charset.forName("UTF-8")
    private static final String projectFolder = "/tmp/git-commands-test/git-repo-${ShellCommands_SvnIntegrationTest.simpleName}"
    private static final String projectFolder2 = "/tmp/git-commands-test/git-repo-${ShellCommands_SvnIntegrationTest.simpleName}-2"
}
