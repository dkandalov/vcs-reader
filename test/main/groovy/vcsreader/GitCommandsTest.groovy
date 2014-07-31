package vcsreader

import org.junit.Before
import org.junit.Test

import static vcsreader.DateTimeUtil.date
import static vcsreader.GitCommands.*

class GitCommandsTest {
    @Test void "git log"() {
        def command = gitLog(prePreparedProject, date("01/01/2013"), date("01/01/2023"))
        assert command.stdout().contains("initial commit")
        assert command.stderr() == ""
        assert command.exitValue() == 0
    }

    @Test void "clone repository"() {
        def command = gitClone("file://" + prePreparedProject, projectFolder)
        assert command.stdout() == ""
        assert command.stderr().trim() == "Cloning into '/tmp/git-commands-test/git-repo'..."
        assert command.exitValue() == 0
    }

    @Test void "failed clone of non-existent repository"() {
        def command = gitClone("file:///tmp/not-existent-path", projectFolder)
        assert command.stdout() == ""
        assert command.stderr().startsWith(
                "Cloning into '/tmp/git-commands-test/git-repo'...\n" +
                "fatal: '/tmp/not-existent-path' does not appear to be a git repository")
        assert command.exitValue() == 128
    }

    @Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
    }


    private final String projectFolder = "/tmp/git-commands-test/git-repo/"
    private final String prePreparedProject = "/tmp/test-repos/git-repo"
}
