package vcsreader

import org.junit.Before
import org.junit.Test

import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.GitCommands.*

class GitCommandsTest {
    @Test void "git log file content"() {
        def command = gitLogFile(prePreparedProject, "file1.txt", firstRevisionIn(prePreparedProject))
        assert command.stdout().trim() == "abc"
        assert command.stderr() == ""
        assert command.exitValue() == 0
    }

    @Test void "failed git log file content"() {
        def command = gitLogFile(prePreparedProject, "non-existent file", firstRevisionIn(prePreparedProject))
        assert command.stdout() == ""
        assert command.stderr().startsWith("fatal: Path 'non-existent file' does not exist")
        assert command.exitValue() == 128
    }

    @Test void "git log"() {
        def command = gitLog(prePreparedProject, date("01/01/2013"), date("01/01/2023"))
        assert command.stdout().contains("initial commit")
        assert command.stderr() == ""
        assert command.exitValue() == 0
    }

    @Test(expected = RuntimeException) void "failed git log"() {
        gitLog(nonExistentPath, date("01/01/2013"), date("01/01/2023"))
    }

    @Test void "clone repository"() {
        def command = gitClone("file://" + prePreparedProject, projectFolder)
        assert command.stdout() == ""
        assert command.stderr().trim() == "Cloning into '/tmp/git-commands-test/git-repo'..."
        assert command.exitValue() == 0
    }

    @Test void "failed clone of non-existent repository"() {
        def command = gitClone("file://" + nonExistentPath, projectFolder)
        assert command.stdout() == ""
        assert command.stderr().startsWith(
                "Cloning into '/tmp/git-commands-test/git-repo'...\n" +
                "fatal: '/tmp/non-existent-path' does not appear to be a git repository")
        assert command.exitValue() == 128
    }

    @Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
    }

    private static String firstRevisionIn(String projectPath) {
        def command = gitLog(projectPath, date("01/01/2013"), date("01/01/2023"))
        command.stdout().split("\n")[1]
    }


    private final String projectFolder = "/tmp/git-commands-test/git-repo/"
    private final String prePreparedProject = "/tmp/test-repos/git-repo"
    private final String nonExistentPath = "/tmp/non-existent-path"
}
