package org.vcsreader.vcs.svn

import org.junit.Test
import org.vcsreader.VcsChange
import org.vcsreader.VcsProject
import org.vcsreader.vcs.Change
import org.vcsreader.vcs.Commit

import static org.vcsreader.VcsChange.Type.*
import static org.vcsreader.VcsChange.noRevision
import static org.vcsreader.lang.DateTimeUtil.date
import static org.vcsreader.lang.DateTimeUtil.dateTime
import static org.vcsreader.vcs.TestUtil.assertCommitsIn
import static org.vcsreader.vcs.TestUtil.withSortedChanges
import static org.vcsreader.vcs.svn.SvnIntegrationTestConfig.*
import static org.vcsreader.vcs.svn.SvnRepository.Scripts.*

class SvnIntegrationTest {
	private static final svnSettings = SvnSettings.defaults().withSvnPath(pathToSvn)

	@Test void "clone project always succeeds"() {
		def vcsRoots = [new SvnVcsRoot(nonExistentUrl, svnSettings)]
		def project = new VcsProject(vcsRoots)

		def cloneResult = project.cloneToLocal()

		assert cloneResult.vcsErrors().empty
		assert cloneResult.isSuccessful()
	}

	@Test void "update project always succeeds"() {
		def vcsRoots = [new SvnVcsRoot(nonExistentUrl, svnSettings)]
		def project = new VcsProject(vcsRoots)

		def updateResult = project.update()

		assert updateResult.vcsErrors().empty
		assert updateResult.isSuccessful()
	}

	@Test void "log no commits for empty project history interval"() {
		def repository = 'repo with two commits with three added files'()

		def project = newProject(repository)
		def logResult = project.log(date("01/08/2014"), date("02/08/2014"))

		assert logResult.commits().empty
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log single commit from project history (start date is inclusive)"() {
		def repository = 'repo with two commits with three added files'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("10/08/2014"), date("11/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("00:00:00 10/08/2014"),
				author,
				"initial commit",
				[new Change(ADDED, "file1.txt", revisions[0])]
			)
		])
	}

	@Test void "log several commits from project history"() {
		def repository = 'repo with two commits with three added files'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("10/08/2014"), date("12/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("00:00:00 10/08/2014"),
				author,
				"initial commit",
				[new Change(ADDED, "file1.txt", revision(1))]
			),
			new Commit(
				revisions[1], revisions[0],
				dateTime("00:00:00 11/08/2014"),
				author,
				"added file2, file3",
				[
					new Change(ADDED, "file3.txt", "", revisions[1], noRevision),
					new Change(ADDED, "file2.txt", "", revisions[1], noRevision)
				]
			)
		])
	}

	@Test void "log commit with modified files"() {
		def repository = 'repo with two added and modified files'()
		def revisions = repository.revisions

		VcsProject project = newProject(repository)
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("15:00:00 12/08/2014"),
				author,
				"modified file1, file2",
				[
					new Change(MODIFIED, "file2.txt", "file2.txt", revisions[1], revisions[0]),
					new Change(MODIFIED, "file1.txt", "file1.txt", revisions[1], revisions[0])
				]
			)
		])
	}

	@Test void "log moved file commit"() {
		def repository = 'repo with moved file'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("13/08/2014"), date("14/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("15:00:00 13/08/2014"),
				author,
				"moved file",
				[new Change(MOVED, "folder/file.txt", "file.txt", revisions[1], revisions[0])]
			)
		])
	}

	@Test void "log moved and renamed file commit"() {
		def repository = 'repo with moved and renamed file'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("14/08/2014"), date("15/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("15:00:00 14/08/2014"),
				author,
				"moved and renamed file",
				[new Change(MOVED, "folder/renamed_file.txt", "file.txt", revisions[1], revisions[0])]
			)
		])
	}

	@Test void "log deleted file"() {
		def repository = 'repo with deleted file'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("15:00:00 15/08/2014"),
				author,
				"deleted file",
				[new Change(DELETED, "", "file.txt", revisions[1], revisions[0])]
			)
		])
	}

	@Test void "log file with spaces and quotes in its name"() {
		def repository = 'repo with file with spaces and quotes'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("16/08/2014"), date("17/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("15:00:00 16/08/2014"),
				author,
				"added file with spaces and quotes",
				[new Change(ADDED, '"file with spaces.txt"', "", revisions[1], noRevision)]
			)
		])
	}

	@Test void "log commit with non-ascii message and file content"() {
		def repository = 'repo with non-ascii file name and commit message'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("17/08/2014"), date("18/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("15:00:00 17/08/2014"),
				author,
				"non-ascii комментарий",
				[new Change(ADDED, "non-ascii.txt", "", revisions[1], noRevision)]
			)
		])
	}

	@Test void "log commit with empty message"() {
		def repository = new SvnRepository().init().with {
			dummyCommit(dateTime("00:00:00 10/08/2014"))
			create("file.txt")
			commit("", dateTime("16:00:00 18/08/2014"))
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("18/08/2014"), date("19/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("16:00:00 18/08/2014"),
				author,
				"",
				[new Change(ADDED, "file.txt", "", revisions[1], noRevision)]
			)
		])
	}

	@Test void "log commit with no file changes"() {
		def repository = new SvnRepository().init().with {
			create("file.txt")
			commit("initial commit", dateTime("00:00:00 10/08/2014"))

			// change property, otherwise svn won't commit
			propset("dummyproperty", "1", "file.txt")
			commit("commit with no changes", dateTime("17:00:00 19/08/2014"))
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("19/08/2014"), date("20/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("17:00:00 19/08/2014"),
				author,
				"commit with no changes",
				[]
			)
		])
	}

	@Test void "log replaced file"() {
		def repository = new SvnRepository().init().with {
			create("replaced-file.txt", "text")
			commit("added file to be replaced", dateTime("15:00:00 20/08/2014"))

			delete("replaced-file.txt")
			create("replaced-file.txt", "replaced text")
			commit("replaced file", dateTime("15:00:00 21/08/2014"))
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("20/08/2014"), date("22/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("15:00:00 20/08/2014"),
				author,
				"added file to be replaced",
				[new Change(ADDED, "replaced-file.txt", "", revisions[0], noRevision)]
			),
			new Commit(
				revisions[1], revisions[0],
				dateTime("15:00:00 21/08/2014"),
				author,
				"replaced file",
				[new Change(DELETED, "", "replaced-file.txt", revisions[1], revisions[0]),
				 new Change(ADDED, "replaced-file.txt", "", revisions[1], noRevision)]
			)
		])
	}

	@Test void "log added empty dir"() {
		def repository = new SvnRepository().init().with {
			dummyCommit(dateTime("00:00:00 10/08/2014"))
			mkdir("empty-dir")
			commit("added empty dir", dateTime("15:00:00 22/08/2014"))
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("22/08/2014"), date("23/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("Fri Aug 22 16:00:00 BST 2014"),
				author,
				"added empty dir",
				[]
			)
		])
	}

	@Test void "log content of modified file"() {
		def repository = 'repo with two added and modified files'()

		def project = newProject(repository)
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		def change = withSortedChanges(logResult.commits()).first().changes.first()
		assert change.type == MODIFIED
		assert change.fileContent().value == "file1 new content"
		assert change.fileContentBefore().value == "file1 content"
	}

	@Test void "log content of new file"() {
		def repository = 'repo with two added and modified files'()

		def project = newProject(repository)
		def logResult = project.log(date("11/08/2014"), date("12/08/2014"))

		def change = withSortedChanges(logResult.commits()).first().changes.first()
		assert change.type == ADDED
		assert change.fileContent().value == "file1 content"
		assert change.fileContentBefore() == VcsChange.FileContent.none
	}

	@Test void "log content of deleted file"() {
		def repository = 'repo with deleted file'()

		def project = newProject(repository)
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == DELETED
		assert change.fileContent() == VcsChange.FileContent.none
		assert change.fileContentBefore().value == "file content"
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log commits and changes only under project folder (even if there are changes in other parts of svn repo)"() {
		def repository = 'repo with moved and renamed file'()
		def revisions = repository.revisions

		def project = new VcsProject(new SvnVcsRoot("file://$repository.repoPath/folder", svnSettings))
		def logResult = project.log(date("01/08/2014"), date("01/09/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("15:00:00 14/08/2014"),
				author,
				"moved and renamed file", [
				// note that change has ADDED type because it was moved from outside sub path
				new Change(ADDED, "renamed_file.txt", "", revisions[1], revisions[0])
			])
		])

		def change = logResult.commits().first().changes.first()
		assert change.fileContentBefore() == VcsChange.FileContent.none // no content because file was moved from outside of project root
		assert change.fileContent().value == "file content"

	}

	@Test void "run svn info command to find repository root from relative url"() {
		def repository = 'repo with moved and renamed file'()

		def svnInfo = new SvnInfo(pathToSvn, "file://$repository.repoPath/folder")
		def result = svnInfo.execute()

		assert result.repositoryRoot == 'file://' + repository.repoPath
		assert result.errors().isEmpty()
	}

	private static VcsProject newProject(SvnRepository repository) {
		new VcsProject(new SvnVcsRoot("file://" + repository.repoPath, svnSettings))
	}
}
