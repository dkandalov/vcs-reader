package vcsreader.vcs
import org.junit.Before
import org.junit.Test

import static GitLogFileContent.gitLogFileContent
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.GitClone.gitClone
import static vcsreader.vcs.GitIntegrationTestConfig.*
import static vcsreader.vcs.GitLog.gitLog
import static vcsreader.vcs.GitLog.gitLogRenames
import static vcsreader.vcs.GitUpdate.gitUpdate

class ShellCommands_GitIntegrationTest {

    @Test void "git log file content"() {
        def command = gitLogFileContent(referenceProject, "file1.txt", firstRevision)
        assert command.stdout().trim() == "file1 content"
        assert command.stderr() == ""
        assert command.exitValue() == 0
    }

    @Test void "failed git log file content"() {
        def command = gitLogFileContent(referenceProject, "non-existent file", firstRevision)
        assert command.stdout() == ""
        assert command.stderr().startsWith("fatal: Path 'non-existent file' does not exist")
        assert command.exitValue() == 128
    }

    @Test void "git log"() {
        def command = gitLog(pathToGit, referenceProject, date("01/01/2013"), date("01/01/2023"))
        assert command.stdout().contains("initial commit")
        assert command.stderr() == ""
        assert command.exitValue() == 0
    }

    @Test void "git log renames"() {
        def command = gitLogRenames(pathToGit, referenceProject, revisions[3])
        assert command.stdout().contains("R100")
        assert command.stdout().contains("folder1/file1.txt")
        assert command.stderr() == ""
        assert command.exitValue() == 0
    }

    @Test void "failed git log"() {
        def command = gitLog(pathToGit, nonExistentPath, date("01/01/2013"), date("01/01/2023"))
        assert command.stdout() == ""
        assert command.stderr() != ""
        assert command.exitValue() != 0
    }

    @Test void "clone repository"() {
        def command = gitClone(pathToGit, "file://" + referenceProject, projectFolder)
        assert command.stdout() == ""
        assert command.stderr().trim() == "Cloning into '${projectFolder}'..."
        assert command.exitValue() == 0
    }

    @Test void "failed clone of non-existent repository"() {
        def command = gitClone(pathToGit, "file://" + nonExistentPath, projectFolder)
        assert command.stdout() == ""
        assert command.stderr().startsWith(
                "Cloning into '${projectFolder}'...\n" +
                "fatal: '/tmp/non-existent-path' does not appear to be a git repository")
        assert command.exitValue() == 128
    }

    @Test void "update repository"() {
        gitClone(pathToGit, "file://" + referenceProject, projectFolder)
        def command = gitUpdate(pathToGit, projectFolder)
        assert command.stdout() == "Already up-to-date.\n"
        assert command.stderr().trim() == ""
        assert command.exitValue() == 0
    }

    @Test void "failed update of non-existing repository"() {
        def command = gitUpdate(pathToGit, nonExistentPath)
        assert command.stdout() == ""
        assert command.stderr() != ""
        assert command.exitValue() != 0
    }

    @Test void "failed update without upstream repository"() {
        gitClone(pathToGit, "file://" + referenceProject, projectFolder + "")
        gitClone(pathToGit, "file://" + projectFolder, projectFolder2)
        new File(projectFolder).deleteDir()

        def command = gitUpdate(pathToGit, projectFolder2)
        assert command.stdout() == ""
        assert command.stderr().contains("fatal: Could not read from remote repository.")
        assert command.exitValue() == 1
    }

    @Before void setup() {
        new File(projectFolder).deleteDir()
        new File(projectFolder).mkdirs()
        new File(projectFolder2).deleteDir()
        new File(projectFolder2).mkdirs()
    }

    private static final String projectFolder = "/tmp/git-commands-test/git-repo-${ShellCommands_GitIntegrationTest.simpleName}"
    private static final String projectFolder2 = "/tmp/git-commands-test/git-repo-${ShellCommands_GitIntegrationTest.simpleName}-2"
}
