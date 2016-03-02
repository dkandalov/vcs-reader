package vcsreader.vcs.svn

import org.junit.BeforeClass
import org.junit.Test
import vcsreader.vcs.Change
import vcsreader.vcs.Commit
import vcsreader.VcsChange
import vcsreader.VcsProject
import vcsreader.vcs.commandlistener.VcsCommand
import vcsreader.vcs.commandlistener.VcsCommandListener

import static vcsreader.VcsChange.Type.*
import static vcsreader.VcsChange.noRevision
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.lang.DateTimeUtil.dateTime
import static vcsreader.vcs.TestUtil.assertEqualCommits
import static vcsreader.vcs.TestUtil.withSortedChanges
import static vcsreader.vcs.svn.SvnIntegrationTestConfig.*

class SvnIntegrationTest {
	private final svnSettings = SvnSettings.defaults().withSvnPath(pathToSvn)
	private final project = new VcsProject([new SvnVcsRoot(repositoryUrl, svnSettings)]).addListener(new VcsCommandListener() {
		@Override void beforeCommand(VcsCommand<?> command) { println(command.describe()) }
		@Override void afterCommand(VcsCommand<?> command) {}
	})

	@Test void "clone project always succeed"() {
		def vcsRoots = [new SvnVcsRoot(nonExistentUrl, svnSettings)]
		def project = new VcsProject(vcsRoots)

		def cloneResult = project.cloneToLocal()
		assert cloneResult.vcsErrors().empty
		assert cloneResult.isSuccessful()
	}

	@Test void "update project"() {
		def updateResult = project.update()
		assert updateResult.vcsErrors().empty
		assert updateResult.isSuccessful()
	}

	@Test void "log no commits for empty project history interval"() {
		def logResult = project.log(date("01/08/2014"), date("02/08/2014"))

		assert logResult.commits().empty
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log single commit from project history (start date is inclusive)"() {
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
								new Change(ADDED, "file3.txt", "", revision(2), noRevision),
								new Change(ADDED, "file2.txt", "", revision(2), noRevision)
						]
				)
		])
	}

	@Test void "log modification commit"() {
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(3), revision(2),
						dateTime("15:00:00 12/08/2014"),
						author,
						"modified file2, file3",
						[
								new Change(MODIFIED, "file3.txt", "file3.txt", revision(3), revision(2)),
								new Change(MODIFIED, "file2.txt", "file2.txt", revision(3), revision(2))
						]
				)
		])
	}

	@Test void "log moved file commit"() {
		def logResult = project.log(date("13/08/2014"), date("14/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(4), revision(3),
						dateTime("15:00:00 13/08/2014"),
						author,
						"moved file1",
						[new Change(MOVED, "folder1/file1.txt", "file1.txt", revision(4), revision(1))]
				)
		])
	}

	@Test void "log moved and renamed file commit"() {
		def logResult = project.log(date("14/08/2014"), date("15/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(5), revision(4),
						dateTime("15:00:00 14/08/2014"),
						author,
						"moved and renamed file1",
						[new Change(MOVED, "folder2/renamed_file1.txt", "folder1/file1.txt", revision(5), revision(4)),
						 new Change(MODIFIED, "file2.txt", "file2.txt", "5", "4")]
				)
		])
	}

	@Test void "log deleted file"() {
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(6), revision(5),
						dateTime("15:00:00 15/08/2014"),
						author,
						"deleted file1",
						[new Change(DELETED, "", "folder2/renamed_file1.txt", revision(6), revision(5))]
				)
		])
	}

	@Test void "log file with spaces and quotes in its name"() {
		def logResult = project.log(date("16/08/2014"), date("17/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(7), revision(6),
						dateTime("15:00:00 16/08/2014"),
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

	@Test void "log replaced file"() {
		def logResult = project.log(date("20/08/2014"), date("22/08/2014"))

		assertEqualCommits(logResult, [
				new Commit(
						revision(11), revision(10),
						dateTime("15:00:00 20/08/2014"),
						author,
						"added file to be replaced",
						[new Change(ADDED, "replaced-file.txt", "", revision(11), noRevision)]
				),
				new Commit(
						revision(12), revision(11),
						dateTime("15:00:00 21/08/2014"),
						author,
						"replaced file",
						[new Change(DELETED, "", "replaced-file.txt", revision(12), revision(11)),
						 new Change(ADDED, "replaced-file.txt", "", revision(12), noRevision)]
				)
		])
	}

	@Test void "log added empty dir"() {
		def logResult = project.log(date("22/08/2014"), date("23/08/2014"))
		assertEqualCommits(logResult, [
				new Commit(
						revision(13), revision(12),
						dateTime("Fri Aug 22 16:00:00 BST 2014"),
						author,
						"added empty dir",
						[]
				)
		])
	}

	@Test void "log content of modified file"() {
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		def change = withSortedChanges(logResult.commits()).first().changes.first()
		assert change.type == MODIFIED
		assert change.fileContent().value == "file2 new content"
		assert change.fileContentBefore().value == "file2 content"
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log content of new file"() {
		def logResult = project.log(date("11/08/2014"), date("12/08/2014"))

		def change = withSortedChanges(logResult.commits()).first().changes.first()
		assert change.type == ADDED
		assert change.fileContent().value == "file2 content"
		assert change.fileContentBefore() == VcsChange.FileContent.none
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log content of deleted file"() {
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == DELETED
		assert change.fileContent() == VcsChange.FileContent.none
		assert change.fileContentBefore().value == "file1 content"
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "show only commits for the path when project is not root in svn repo"() {
		def nonRootProject = new VcsProject([new SvnVcsRoot(repositoryUrl + "/folder2", svnSettings)])

		def logResult = nonRootProject.log(date("01/08/2014"), date("01/09/2014"))
		assertEqualCommits(logResult, [
				new Commit(revision(5), revision(4), dateTime("15:00:00 14/08/2014"), author, "moved and renamed file1", [
						// note that change has ADDED type because it was moved from outside sub path
						new Change(ADDED, "renamed_file1.txt", "", revision(5), revision(4))
				]),
				new Commit(revision(6), revision(5), dateTime("15:00:00 15/08/2014"), author, "deleted file1", [
						new Change(DELETED, "", "renamed_file1.txt", revision(6), revision(5))
				])
		])
	}

	@Test void "content of commits in project which is not root in svn repo"() {
		def nonRootProject = new VcsProject([new SvnVcsRoot(repositoryUrl + "/folder2", svnSettings)])

		def logResult = nonRootProject.log(date("01/08/2014"), date("01/09/2014"))
		assert logResult.commits()[0].message == "moved and renamed file1"
		assert logResult.commits()[0].changes[0].fileContentBefore() == VcsChange.FileContent.none
		// no content because file was moved from outside of project root
		assert logResult.commits()[0].changes[0].fileContent().value == "file1 content"
		assert logResult.commits()[1].message == "deleted file1"
		assert logResult.commits()[1].changes[0].fileContentBefore().value == "file1 content"
		assert logResult.commits()[1].changes[0].fileContent() == VcsChange.FileContent.none
	}

	@Test void "get repository root"() {
		def svnInfo = new SvnInfo(pathToSvn, repositoryUrl + "/folder2")
		def result = svnInfo.execute()
		assert result.repositoryRoot == repositoryUrl
		assert result.errors().isEmpty()
	}

	@BeforeClass static void setupConfig() {
		initTestConfig()
	}
}
