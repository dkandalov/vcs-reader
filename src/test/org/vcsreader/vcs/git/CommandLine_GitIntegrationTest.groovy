package org.vcsreader.vcs.git

import org.junit.Test

import static org.vcsreader.lang.Charsets.UTF8
import static org.vcsreader.lang.DateTimeUtil.date
import static org.vcsreader.vcs.git.GitClone.gitClone
import static org.vcsreader.vcs.git.GitIntegrationTestConfig.*
import static org.vcsreader.vcs.git.GitLog.gitLog
import static org.vcsreader.vcs.git.GitLog.gitLogRenames
import static org.vcsreader.vcs.git.GitLogFileContent.gitLogFileContent
import static org.vcsreader.vcs.git.GitRepository.Scripts.*
import static org.vcsreader.vcs.git.GitUpdate.gitUpdate

class CommandLine_GitIntegrationTest {
	private final String projectFolder = newProjectPath()

	@Test void "basic log"() {
		def repository = 'repo with two commits with three added files'()

		def commandLine = gitLog(pathToGit, repository.path, date("01/01/2013"), date("01/01/2023")).execute()

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
		def repository = 'repo with two added and modified files'()

		def commandLine = gitLogFileContent(pathToGit, repository.path, "file1.txt", repository.revisions[0], UTF8).execute()

		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "file1 content"
		assert commandLine.exitCode() == 0
	}

	@Test void "failed log file content"() {
		def repository = someNonEmptyRepository()

		def commandLine = gitLogFileContent(pathToGit, repository.path, "non-existent file", repository.revisions[0], UTF8).execute()

		assert commandLine.stdout() == ""
		assert commandLine.stderr().startsWith("fatal: Path 'non-existent file' does not exist")
		assert commandLine.exitCode() == 128
	}

	@Test void "log renames"() {
		def repository = 'repo with moved and renamed file'()

		def commandLine = gitLogRenames(pathToGit, repository.path, repository.revisions[1]).execute()

		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("R100")
		assert commandLine.stdout().contains("folder/renamed_file.txt")
		assert commandLine.exitCode() == 0
	}

	@Test void "clone repository"() {
		def repository = someNonEmptyRepository()

		def commandLine = gitClone(pathToGit, repository.path, projectFolder).execute()

		assert commandLine.stdout() == ""
		assert commandLine.stderr().trim().startsWith("Cloning into '${projectFolder}'...")
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
		def repository = someNonEmptyRepository()
		gitClone(pathToGit, repository.path, projectFolder).execute()

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
		def repository = someNonEmptyRepository()
		gitClone(pathToGit, repository.path, projectFolder).execute()
		new File(repository.path).deleteDir() // delete upstream repo so that update fails

		def commandLine = gitUpdate(pathToGit, projectFolder).execute()

		assert commandLine.stdout() == ""
		assert commandLine.stderr().contains("fatal: Could not read from remote repository.")
		assert commandLine.exitCode() == 1
	}

	@Test void "log with non-ascii characters"() {
		def repository = 'repo with non-ascii file name and commit message'()

		def commandLine = gitLog(pathToGit, repository.path, date("01/01/2013"), date("01/01/2023")).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().contains("non-ascii комментарий")
		assert commandLine.exitCode() == 0

		commandLine = gitLogFileContent(pathToGit, repository.path, "non-ascii.txt", repository.revisions[0], UTF8).execute()
		assert commandLine.stderr() == ""
		assert commandLine.stdout().trim() == "non-ascii содержимое"
		assert commandLine.exitCode() == 0
	}

}
