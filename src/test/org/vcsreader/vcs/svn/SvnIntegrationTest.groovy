package org.vcsreader.vcs.svn

import org.junit.Test
import org.vcsreader.VcsChange
import org.vcsreader.VcsProject
import org.vcsreader.lang.TimeRange
import org.vcsreader.vcs.Change
import org.vcsreader.vcs.Commit

import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat
import static org.vcsreader.VcsChange.Type.*
import static org.vcsreader.VcsChange.noRevision
import static org.vcsreader.lang.DateTimeUtil.*
import static org.vcsreader.vcs.TestUtil.*
import static org.vcsreader.vcs.svn.SvnIntegrationTestConfig.*
import static org.vcsreader.vcs.svn.SvnRepository.Scripts.*

class SvnIntegrationTest {
	private static final svnSettings = SvnSettings.defaults().withSvnPath(pathToSvn).withFailFast(true)

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
		def logResult = project.log(timeRange("01/08/2014", "02/08/2014"))

		assert logResult.commits().empty
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log single commit from project history (time range start is inclusive, end is exclusive)"() {
		def repository = 'repo with two commits with three added files'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(timeRange("10/08/2014", "11/08/2014"))

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

	@Test(expected = IllegalStateException)
	void "commit time before 1970 is not supported"() {
		def svnRepository = new SvnRepository().init()
		svnRepository.create("file.txt")
		svnRepository.commit("initial commit", dateTimeInstant("23:59:59 31/12/1969"))
	}

	@Test void "commit time is supported to at least 2999"() {
		def svnRepository = new SvnRepository().init()
		svnRepository.create("file.txt")
		svnRepository.commit("initial commit", dateTimeInstant("00:00:00 01/01/2999"))

		def project = newProject(svnRepository)
		def logResult = project.log(TimeRange.all)

		assert logResult.commits().size() == 1
	}

	@Test void "log several commits from project history"() {
		def repository = 'repo with two commits with three added files'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(timeRange("10/08/2014", "12/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("00:00:00 10/08/2014"),
				author,
				"initial commit",
				[new Change(ADDED, "file1.txt", revisions[0])]
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

	@Test void "log commits before/after/for all dates"() {
		def repository = 'repo with two commits with three added files'()

		def project = newProject(repository)

		project.log(TimeRange.before(date("11/08/2014"))).commits().with {
			assertThat(it[0].message, equalTo("initial commit"))
			assertThat(it.size(), equalTo(1))
		}

		project.log(TimeRange.after(date("11/08/2014"))).commits().with {
			assertThat(it[0].message, equalTo("added file2, file3"))
			assertThat(it.size(), equalTo(1))
		}

		project.log(TimeRange.all).commits().with {
			assertThat(it[0].message, equalTo("initial commit"))
			assertThat(it[1].message, equalTo("added file2, file3"))
			assertThat(it.size(), equalTo(2))
		}
	}

	@Test void "log commit with modified files"() {
		def repository = 'repo with two added and modified files'()
		def revisions = repository.revisions

		VcsProject project = newProject(repository)
		def logResult = project.log(timeRange("12/08/2014", "13/08/2014"))

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
		def logResult = project.log(timeRange("13/08/2014", "14/08/2014"))

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
		def logResult = project.log(timeRange("14/08/2014", "15/08/2014"))

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
		def logResult = project.log(timeRange("15/08/2014", "16/08/2014"))

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
		def logResult = project.log(timeRange("16/08/2014", "17/08/2014"))

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
		def logResult = project.log(timeRange("17/08/2014", "18/08/2014"))

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
			dummyCommit(dateTimeInstant("00:00:00 10/08/2014"))
			create("file.txt")
			commit("", dateTimeInstant("16:00:00 18/08/2014"))
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(timeRange("18/08/2014", "19/08/2014"))

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
			commit("initial commit", dateTimeInstant("00:00:00 10/08/2014"))

			// change property, otherwise svn won't commit
			propset("dummyproperty", "1", "file.txt")
			commit("commit with no changes", dateTimeInstant("17:00:00 19/08/2014"))
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(timeRange("19/08/2014", "20/08/2014"))

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
			commit("added file to be replaced", dateTimeInstant("15:00:00 20/08/2014"))

			delete("replaced-file.txt")
			create("replaced-file.txt", "replaced text")
			commit("replaced file", dateTimeInstant("15:00:00 21/08/2014"))
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(timeRange("20/08/2014", "22/08/2014"))

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
			dummyCommit(dateTimeInstant("00:00:00 10/08/2014"))
			mkdir("empty-dir")
			commit("added empty dir", dateTimeInstant("15:00:00 22/08/2014"))
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(timeRange("22/08/2014", "23/08/2014"))

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
		def logResult = project.log(timeRange("12/08/2014", "13/08/2014"))

		def change = withSortedChanges(logResult.commits()).first().changes.first()
		assert change.type == MODIFIED
		assert change.fileContent().value == "file1 new content"
		assert change.fileContentBefore().value == "file1 content"
	}

	@Test void "log content of new file"() {
		def repository = 'repo with two added and modified files'()

		def project = newProject(repository)
		def logResult = project.log(timeRange("11/08/2014", "12/08/2014"))

		def change = withSortedChanges(logResult.commits()).first().changes.first()
		assert change.type == ADDED
		assert change.fileContent().value == "file1 content"
		assert change.fileContentBefore() == VcsChange.FileContent.none
	}

	@Test void "log content of deleted file"() {
		def repository = 'repo with deleted file'()

		def project = newProject(repository)
		def logResult = project.log(timeRange("15/08/2014", "16/08/2014"))

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
		def logResult = project.log(timeRange("01/08/2014", "01/09/2014"))

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
		assert result.isSuccessful()
	}

	private static VcsProject newProject(SvnRepository repository) {
		def project = new VcsProject(new SvnVcsRoot("file://" + repository.repoPath, svnSettings))
		project.addListener(printingListener)
		project
	}
}
