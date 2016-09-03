package org.vcsreader.vcs.hg

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
import static org.vcsreader.vcs.TestUtil.assertCommitsIn
import static org.vcsreader.vcs.TestUtil.printingListener
import static org.vcsreader.vcs.git.GitIntegrationTestConfig.author
import static org.vcsreader.vcs.hg.HgIntegrationTestConfig.*
import static org.vcsreader.vcs.hg.HgRepository.Scripts.*

class HgIntegrationTest {
	private static final hgSettings = HgSettings.defaults().withHgPath(pathToHg).withFailFast(false)

	@Test void "clone project"() {
		def repository = new HgRepository().init()
		def project = new VcsProject(new HgVcsRoot(newProjectPath(), repository.path, hgSettings))

		def cloneResult = project.cloneToLocal()

		assert cloneResult.vcsErrors().empty
		assert cloneResult.isSuccessful()
	}

	@Test void "clone project failure"() {
		def vcsRoot = new HgVcsRoot(newProjectPath(), nonExistentPath, hgSettings)
		def project = new VcsProject(vcsRoot)

		def cloneResult = project.cloneToLocal()

		assert !cloneResult.isSuccessful()
		assert cloneResult.vcsErrors().size() == 1
	}

	@Test void "update project"() {
		def repository = someNonEmptyRepository()

		def project = newProject(repository)
		project.cloneToLocal()

		def updateResult = project.update()
		assert updateResult.vcsErrors().empty
		assert updateResult.isSuccessful()
	}

	@Test void "update project failure"() {
		def repository = new HgRepository().init()
		def project = newProject(repository)
		// delete project directory so that update fails
		assert new File((project.vcsRoots().first() as HgVcsRoot).localPath()).deleteDir()

		def updateResult = project.update()

		assert !updateResult.isSuccessful()
		assert updateResult.vcsErrors() == []
		assert updateResult.exceptions() != []
	}

	@Test void "log no commits for empty project history interval"() {
		def repository = someNonEmptyRepository()

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
				dateTime("12:54:56 10/08/2014"),
				author,
				"initial commit",
				[new Change(ADDED, "file1.txt", revisions[0])]
			)
		])
	}

	@Test(expected = IllegalStateException)
	void "commit time before 1970 is not supported"() {
		// see https://www.mercurial-scm.org/pipermail/mercurial-devel/2016-March/082248.html
		def hgRepository = new HgRepository().init()
		hgRepository.create("file.txt")
		hgRepository.commit("initial commit", "Dec 31 23:59:59 1969 +0000")
	}

	@Test(expected = IllegalStateException)
	void "commit time after ~2038 (larger than 32 bit) is not supported"() {
		def hgRepository = new HgRepository().init()
		hgRepository.create("file.txt")
		hgRepository.commit("initial commit", "Jun 01 00:00:00 2038 +0000")
	}

	@Test void "log several commits from project history"() {
		def repository = 'repo with two commits with three added files'()
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(timeRange("10/08/2014", "12/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("12:54:56 10/08/2014"),
				author,
				"initial commit",
				[new Change(ADDED, "file1.txt", revisions[0])]
			),
			new Commit(
				revisions[1], revisions[0],
				dateTime("19:55:57 11/08/2014"),
				author,
				"added file2, file3",
				[
					new Change(ADDED, "file2.txt", "", revisions[1], noRevision),
					new Change(ADDED, "file3.txt", "", revisions[1], noRevision)
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

	@Test void "log modification commit"() {
		def repository = 'repo with two added and modified files'()
		def revisions = repository.revisions

		VcsProject project = newProject(repository)
		def logResult = project.log(timeRange("12/08/2014", "13/08/2014"))

		assertCommitsIn(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("14:00:00 12/08/2014"),
				author,
				"modified file1, file2",
				[
					new Change(MODIFIED, "file1.txt", "file1.txt", revisions[1], revisions[0]),
					new Change(MODIFIED, "file2.txt", "file2.txt", revisions[1], revisions[0])
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
				dateTime("14:00:00 13/08/2014"),
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
				dateTime("14:00:00 14/08/2014"),
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
				dateTime("14:00:00 15/08/2014"),
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
				revisions[0], noRevision,
				dateTime("14:00:00 16/08/2014"),
				author,
				"added file with spaces and quotes",
				[new Change(ADDED, "\"file with spaces.txt\"", "", revisions[0], noRevision)]
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
				revisions[0], noRevision,
				dateTime("15:00:00 17/08/2014"),
				author,
				"non-ascii комментарий",
				[new Change(ADDED, "non-ascii.txt", "", revisions[0], noRevision)]
			)
		])
	}

	@Test void "log content of modified file"() {
		def repository = 'repo with two added and modified files'()

		def project = newProject(repository)
		def logResult = project.log(timeRange("12/08/2014", "13/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == MODIFIED
		assert change.fileContent().value == "file1 new content"
		assert change.fileContentBefore().value == "file1 content"
	}

	@Test void "log content of new file"() {
		def repository = 'repo with two added and modified files'()

		def project = newProject(repository)
		def logResult = project.log(timeRange("11/08/2014", "12/08/2014"))

		def change = logResult.commits().first().changes.first()
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
	}

	private static VcsProject newProject(HgRepository repository) {
		def project = new VcsProject(new HgVcsRoot(newProjectPath(), repository.path, hgSettings))
		project.addListener(printingListener)
		project.cloneToLocal()
		project
	}
}
