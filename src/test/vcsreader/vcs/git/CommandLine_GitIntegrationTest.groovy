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

class CommandLine_GitIntegrationTest {

	@Test void "basic log"() {
		def commandLine = gitLog(pathToGit, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert commandLine.stdout().contains("initial commit")
		assert commandLine.stderr() == ""
		assert commandLine.exceptionStacktrace() == ""
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log"() {
		def commandLine = gitLog(pathToGit, nonExistentPath, date("01/01/2013"), date("01/01/2023")).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr() == ""
		assert commandLine.exceptionStacktrace().matches(/(?s).*java.io.IOException.*No such file or directory.*/)
		assert commandLine.exitCode() != 0
	}

	@Test void "log file content"() {
		def commandLine = gitLogFileContent(pathToGit, referenceProject, "file1.txt", revision(1), UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "file1 content"
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log file content"() {
		def commandLine = gitLogFileContent(pathToGit, referenceProject, "non-existent file", revision(1), UTF8).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr().startsWith("fatal: Path 'non-existent file' does not exist")
		assert commandLine.exitCode() == 128
	}

	@Test void "log renames"() {
		def commandLine = gitLogRenames(pathToGit, referenceProject, revision(4)).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("R100")
		assert commandLine.stdout().contains("folder1/file1.txt")
		assert commandLine.exitCode() == 0
	}

	@Test void "clone repository"() {
		def commandLine = gitClone(pathToGit, "file://" + referenceProject, projectFolder).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr().trim() == "Cloning into '${projectFolder}'..."
		assert commandLine.exitCode() == 0
	}

	@Test void "failed clone of non-existent repository"() {
		def commandLine = gitClone(pathToGit, "file://" + nonExistentPath, projectFolder).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr().startsWith(
				"Cloning into '${projectFolder}'...\n" +
						"fatal: '/tmp/non-existent-path' does not appear to be a git repository")
		assert commandLine.exitCode() == 128
	}

	@Test void "update repository"() {
		gitClone(pathToGit, "file://" + referenceProject, projectFolder).execute()
		def commandLine = gitUpdate(pathToGit, projectFolder).execute()
		assert commandLine.stderr().trim() == ""
		assert commandLine.stdout() == "Already up-to-date.\n"
		assert commandLine.exitCode() == 0
	}

	@Test void "failed update of non-existing repository"() {
		def commandLine = gitUpdate(pathToGit, nonExistentPath).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr() == ""
		assert commandLine.exitCode() != 0
		assert commandLine.exceptionStacktrace().matches(/(?s).*java.io.IOException.*No such file or directory.*/)
	}

	@Test void "failed update without upstream repository"() {
		gitClone(pathToGit, "file://" + referenceProject, projectFolder).execute()
		gitClone(pathToGit, "file://" + projectFolder, projectFolder2).execute()
		new File(projectFolder).deleteDir()

		def commandLine = gitUpdate(pathToGit, projectFolder2).execute()
		assert commandLine.stdout() == ""
		assert commandLine.stderr().contains("fatal: Could not read from remote repository.")
		assert commandLine.exitCode() == 1
	}

	@Test void "log with non-ascii characters"() {
		def commandLine = gitLog(pathToGit, referenceProject, date("01/01/2013"), date("01/01/2023")).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("non-ascii комментарий")
		assert commandLine.exitCode() == 0

		commandLine = gitLogFileContent(pathToGit, referenceProject, "non-ascii.txt", revision(8), UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "non-ascii содержимое"
		assert commandLine.exitCode() == 0
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

	private static final String projectFolder = "/tmp/git-commands-test/git-repo-${CommandLine_GitIntegrationTest.simpleName}"
	private static final String projectFolder2 = "/tmp/git-commands-test/git-repo-2-${CommandLine_GitIntegrationTest.simpleName}"
}
