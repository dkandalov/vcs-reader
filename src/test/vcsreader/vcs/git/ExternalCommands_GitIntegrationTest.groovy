package vcsreader.vcs.git

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static vcsreader.lang.Charsets.UTF8
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.vcs.git.GitClone.gitClone
import static vcsreader.vcs.git.GitIntegrationTestConfig.*
import static vcsreader.vcs.git.GitLog.gitLog
import static vcsreader.vcs.git.GitLog.gitLogRenames
import static vcsreader.vcs.git.GitLogFileContent.gitLogFileContent
import static vcsreader.vcs.git.GitUpdate.gitUpdate

class ExternalCommands_GitIntegrationTest {

	@Test void "basic log"() {
		def command = gitLog(pathToGit, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert command.stdout().contains("initial commit")
		assert command.stderr() == ""
		assert command.exceptionStacktrace() == ""
		assert command.exitCode() == 0
	}

	@Test void "failed log"() {
		def command = gitLog(pathToGit, nonExistentPath, date("01/01/2013"), date("01/01/2023")).execute()
		assert command.stdout() == ""
		assert command.stderr() == ""
		assert command.exceptionStacktrace().matches(/(?s).*java.io.IOException.*No such file or directory.*/)
		assert command.exitCode() != 0
	}

	@Test void "log file content"() {
		def command = gitLogFileContent(pathToGit, referenceProject, "file1.txt", revision(1), UTF8).execute()
		assert command.stderr() == ""
		assert command.stdout().trim() == "file1 content"
		assert command.exitCode() == 0
	}

	@Test void "failed log file content"() {
		def command = gitLogFileContent(pathToGit, referenceProject, "non-existent file", revision(1), UTF8).execute()
		assert command.stdout() == ""
		assert command.stderr().startsWith("fatal: Path 'non-existent file' does not exist")
		assert command.exitCode() == 128
	}

	@Test void "log renames"() {
		def command = gitLogRenames(pathToGit, referenceProject, revision(4)).execute()
		assert command.stderr() == ""
		assert command.stdout().contains("R100")
		assert command.stdout().contains("folder1/file1.txt")
		assert command.exitCode() == 0
	}

	@Test void "clone repository"() {
		def command = gitClone(pathToGit, "file://" + referenceProject, projectFolder).execute()
		assert command.stdout() == ""
		assert command.stderr().trim() == "Cloning into '${projectFolder}'..."
		assert command.exitCode() == 0
	}

	@Test void "failed clone of non-existent repository"() {
		def command = gitClone(pathToGit, "file://" + nonExistentPath, projectFolder).execute()
		assert command.stdout() == ""
		assert command.stderr().startsWith(
				"Cloning into '${projectFolder}'...\n" +
						"fatal: '/tmp/non-existent-path' does not appear to be a git repository")
		assert command.exitCode() == 128
	}

	@Test void "update repository"() {
		gitClone(pathToGit, "file://" + referenceProject, projectFolder).execute()
		def command = gitUpdate(pathToGit, projectFolder).execute()
		assert command.stderr().trim() == ""
		assert command.stdout() == "Already up-to-date.\n"
		assert command.exitCode() == 0
	}

	@Test void "failed update of non-existing repository"() {
		def command = gitUpdate(pathToGit, nonExistentPath).execute()
		assert command.stdout() == ""
		assert command.stderr() == ""
		assert command.exitCode() != 0
		assert command.exceptionStacktrace().matches(/(?s).*java.io.IOException.*No such file or directory.*/)
	}

	@Test void "failed update without upstream repository"() {
		gitClone(pathToGit, "file://" + referenceProject, projectFolder).execute()
		gitClone(pathToGit, "file://" + projectFolder, projectFolder2).execute()
		new File(projectFolder).deleteDir()

		def command = gitUpdate(pathToGit, projectFolder2).execute()
		assert command.stdout() == ""
		assert command.stderr().contains("fatal: Could not read from remote repository.")
		assert command.exitCode() == 1
	}

	@Test void "log with non-ascii characters"() {
		def command = gitLog(pathToGit, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert command.stderr() == ""
		assert command.stdout().contains("non-ascii комментарий")
		assert command.exitCode() == 0

		command = gitLogFileContent(pathToGit, referenceProject, "non-ascii.txt", revision(8), UTF8).execute()
		assert command.stderr() == ""
		assert command.stdout().trim() == "non-ascii содержимое"
		assert command.exitCode() == 0
	}

	@Before void setup() {
		new File(projectFolder).deleteDir()
		new File(projectFolder).mkdirs()
		new File(projectFolder2).deleteDir()
		new File(projectFolder2).mkdirs()
	}

	@BeforeClass static void setupConfig() {
		initTestConfig()
	}

	private static
	final String projectFolder = "/tmp/git-commands-test/git-repo-${ExternalCommands_GitIntegrationTest.simpleName}"
	private static
	final String projectFolder2 = "/tmp/git-commands-test/git-repo-2-${ExternalCommands_GitIntegrationTest.simpleName}"
}
