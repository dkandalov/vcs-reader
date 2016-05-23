package org.vcsreader.vcs.git

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.vcsreader.vcs.Change
import org.vcsreader.vcs.Commit
import org.vcsreader.VcsChange
import org.vcsreader.VcsProject
import org.vcsreader.vcs.commandlistener.VcsCommand
import org.vcsreader.vcs.commandlistener.VcsCommandListener

import static org.vcsreader.VcsChange.Type.*
import static org.vcsreader.VcsChange.noRevision
import static org.vcsreader.lang.DateTimeUtil.date
import static org.vcsreader.lang.DateTimeUtil.dateTime
import static org.vcsreader.vcs.TestUtil.assertEqualCommits
import static org.vcsreader.vcs.git.GitIntegrationTestConfig.*

class GitIntegrationTest {
	private static final String projectFolder = "/tmp/git-commands-test/git-repo-${GitIntegrationTest.simpleName}/"

	private final gitSettings = GitSettings.defaults().withGitPath(pathToGit)
	private final vcsRoot = new GitVcsRoot(projectFolder, referenceProject, gitSettings)
	private final project = new VcsProject([vcsRoot]).addListener(new VcsCommandListener() {
		@Override void beforeCommand(VcsCommand<?> command) { println(command.describe()) }
		@Override void afterCommand(VcsCommand<?> command) {}
	})

	@Test void "clone project"() {
		def cloneResult = project.cloneToLocal()
		assert cloneResult.vcsErrors().empty
		assert cloneResult.isSuccessful()
	}

	@Test void "clone project failure"() {
		def vcsRoots = [new GitVcsRoot(projectFolder, nonExistentPath, gitSettings)]
		def project = new VcsProject(vcsRoots)

		def cloneResult = project.cloneToLocal()

		assert !cloneResult.isSuccessful()
		assert cloneResult.vcsErrors().size() == 1
	}

	@Test void "update project"() {
		project.cloneToLocal()
		def updateResult = project.update()
		assert updateResult.vcsErrors().empty
		assert updateResult.isSuccessful()
	}

	@Test void "update project failure"() {
		project.cloneToLocal()
		new File(projectFolder).deleteDir()

		def updateResult = project.update()
		assert !updateResult.isSuccessful()
		assert updateResult.vcsErrors() != []
	}

	@Test void "log no commits for empty project history interval"() {
		project.cloneToLocal()
		def logResult = project.log(date("01/08/2014"), date("02/08/2014"))

		assert logResult.commits().empty
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log single commit from project history (start date is inclusive, end date is exclusive)"() {
		project.cloneToLocal()
		def logResult = project.log(date("10/08/2014"), date("11/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(1), noRevision,
						dateTime("00:00:00 10/08/2014"),
						author,
						"initial commit",
						[new Change(ADDED, "file1.txt", revision(1))]
				)
		])
	}

	@Test void "log several commits from project history"() {
		project.cloneToLocal()
		def logResult = project.log(date("10/08/2014"), date("12/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(1), noRevision,
						dateTime("00:00:00 10/08/2014"),
						author,
						"initial commit",
						[new Change(ADDED, "file1.txt", revision(1))]
				),
				new Commit(
						revision(2), revision(1),
						dateTime("00:00:00 11/08/2014"),
						author,
						"added file2, file3",
						[
								new Change(ADDED, "file2.txt", "", revision(2), noRevision),
								new Change(ADDED, "file3.txt", "", revision(2), noRevision)
						]
				)
		])
	}

	@Test void "log modification commit"() {
		project.cloneToLocal()
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(3), revision(2),
						dateTime("14:00:00 12/08/2014"),
						author,
						"modified file2, file3",
						[
								new Change(MODIFIED, "file2.txt", "file2.txt", revision(3), revision(2)),
								new Change(MODIFIED, "file3.txt", "file3.txt", revision(3), revision(2))
						]
				)
		])
	}

	@Test void "log moved file commit"() {
		project.cloneToLocal()
		def logResult = project.log(date("13/08/2014"), date("14/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(4), revision(3),
						dateTime("14:00:00 13/08/2014"),
						author,
						"moved file1",
						[new Change(MOVED, "folder1/file1.txt", "file1.txt", revision(4), revision(3))]
				)
		])
	}

	@Test void "log moved and renamed file commit"() {
		project.cloneToLocal()
		def logResult = project.log(date("14/08/2014"), date("15/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(5), revision(4),
						dateTime("14:00:00 14/08/2014"),
						author,
						"moved and renamed file1",
						[new Change(MOVED, "folder2/renamed_file1.txt", "folder1/file1.txt", revision(5), revision(4))]
				)
		])
	}

	@Test void "log deleted file"() {
		project.cloneToLocal()
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(6), revision(5),
						dateTime("14:00:00 15/08/2014"),
						author,
						"deleted file1",
						[new Change(DELETED, "", "folder2/renamed_file1.txt", revision(6), revision(5))]
				)
		])
	}

	@Test void "log file with spaces and quotes in its name"() {
		project.cloneToLocal()
		def logResult = project.log(date("16/08/2014"), date("17/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(7), revision(6),
						dateTime("14:00:00 16/08/2014"),
						author,
						"added file with spaces and quotes",
						[new Change(ADDED, "\"file with spaces.txt\"", "", revision(7), noRevision)]
				)
		])
	}

	@Test void "log commit with non-ascii message and file content"() {
		project.cloneToLocal()
		def logResult = project.log(date("17/08/2014"), date("18/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(8), revision(7),
						dateTime("15:00:00 17/08/2014"),
						author,
						"non-ascii комментарий",
						[new Change(ADDED, "non-ascii.txt", "", revision(8), noRevision)]
				)
		])
	}

	@Test void "log commit with empty message"() {
		project.cloneToLocal()
		def logResult = project.log(date("18/08/2014"), date("19/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(9), revision(8),
						dateTime("16:00:00 18/08/2014"),
						author,
						"",
						[new Change(ADDED, "file4.txt", "", revision(9), noRevision)]
				)
		])
	}

	@Test void "log commit with no changes"() {
		project.cloneToLocal()
		def logResult = project.log(date("19/08/2014"), date("20/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(10), revision(9),
						dateTime("17:00:00 19/08/2014"),
						author,
						"commit with no changes",
						[]
				)
		])
	}

	@Test void "log branch rebase"() {
		project.cloneToLocal()
		def logResult = project.log(date("20/08/2014"), date("21/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(12), revision(11),
						dateTime("18:00:00 20/08/2014"),
						author,
						"added file1-branch.txt",
						[new Change(ADDED, "file1-branch.txt", "", revision(12), noRevision)]
				),
				new Commit(
						revision(11), revision(10),
						dateTime("18:10:00 20/08/2014"),
						author,
						"added file1-master.txt",
						[new Change(ADDED, "file1-master.txt", "", revision(11), noRevision)]
				)
		])
	}

	@Test void "log branch merge"() {
		project.cloneToLocal()
		def logResult = project.log(date("21/08/2014"), date("22/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(13), revision(12),
						dateTime("19:00:00 21/08/2014"),
						author,
						"added file2-branch.txt",
						[new Change(ADDED, "file2-branch.txt", "", revision(13), noRevision)]
				),
				new Commit(
						revision(14), revision(11), // note the revision "jump"
						dateTime("19:10:00 21/08/2014"),
						author,
						"added file2-master.txt",
						[new Change(ADDED, "file2-master.txt", "", revision(14), noRevision)]
				)
		])
	}

	@Test void "log content of modified file"() {
		project.cloneToLocal()
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == MODIFIED
		assert change.fileContent().value == "file2 new content"
		assert change.fileContentBefore().value == "file2 content"

		assert logResult.isSuccessful()
	}

	@Test void "log content of new file"() {
		project.cloneToLocal()
		def logResult = project.log(date("11/08/2014"), date("12/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == ADDED
		assert change.fileContent().value == "file2 content"
		assert change.fileContentBefore() == VcsChange.FileContent.none

		assert logResult.isSuccessful()
	}

	@Test void "log content of deleted file"() {
		project.cloneToLocal()
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == DELETED
		assert change.fileContent() == VcsChange.FileContent.none
		assert change.fileContentBefore().value == "file1 content"

		assert logResult.isSuccessful()
	}

	@Before void setup() {
		new File(projectFolder).deleteDir()
		new File(projectFolder).mkdirs()
	}

	@BeforeClass static void setupConfig() {
		initTestConfig()
	}
}
