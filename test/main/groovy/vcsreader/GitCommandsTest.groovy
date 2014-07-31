package vcsreader

import org.junit.Before
import org.junit.Test

import static vcsreader.GitCommands.gitClone

class GitCommandsTest {
    @Test void "clone repository"() {
        def command = gitClone("file:///tmp/test-repos/git-repo", toFolder)
        assert command.finished()
        assert command.stdout() == ""
        assert command.stderr().trim() == "Cloning into '/tmp/git-commands-test/git-repo'..."
        assert command.exitValue() == 0
    }

    @Test void "failed clone of non-existent repository"() {
        def command = gitClone("file:///tmp/not-existent-path", toFolder)
        assert command.finished()
        assert command.stdout() == ""
        assert command.stderr().startsWith(
                "Cloning into '/tmp/git-commands-test/git-repo'...\n" +
                "fatal: '/tmp/not-existent-path' does not appear to be a git repository")
        assert command.exitValue() == 128
    }

    @Before void setup() {
        new File(toFolder).deleteDir()
        new File(toFolder).mkdirs()
    }

    private final String toFolder = "/tmp/git-commands-test/git-repo/"
}
