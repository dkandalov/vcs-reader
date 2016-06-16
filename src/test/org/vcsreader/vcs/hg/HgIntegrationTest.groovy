package org.vcsreader.vcs.hg

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
import static org.vcsreader.vcs.hg.HgIntegrationTestConfig.*
import static org.vcsreader.vcs.hg.HgRepository.Scripts.*

class HgIntegrationTest {
	private static final hgSettings = HgSettings.defaults().withHgPath(pathToHg)

	@Test void "clone project"() {
		def repository = new HgRepository(newReferenceRepoPath()).init()
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
		def repository = new HgRepository(newReferenceRepoPath()).init()
		def project = newProject(repository)
		// delete project directory so that update fails
		assert new File((project.vcsRoots().first() as HgVcsRoot).localPath).deleteDir()

		def updateResult = project.update()

		assert !updateResult.isSuccessful()
		assert updateResult.vcsErrors() != []
	}

	@Test void "log no commits for empty project history interval"() {
		def repository = someNonEmptyRepository()

		def project = newProject(repository)
		def logResult = project.log(date("01/08/2014"), date("02/08/2014"))

		assert logResult.commits().empty
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log single commit from project history (start date is inclusive, end date is exclusive)"() {
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
				[new Change(ADDED, "file1.txt", revisions[0])]
			),
			new Commit(
				revisions[1], revisions[0],
				dateTime("00:00:00 11/08/2014"),
				author,
				"added file2, file3",
				[
					new Change(ADDED, "file2.txt", "", revisions[1], noRevision),
					new Change(ADDED, "file3.txt", "", revisions[1], noRevision)
				]
			)
		])
	}

	@Test void "log modification commit"() {
		def repository = 'repo with two added and modified files'()
		def revisions = repository.revisions

		VcsProject project = newProject(repository)
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

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
		def logResult = project.log(date("13/08/2014"), date("14/08/2014"))

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
		def logResult = project.log(date("14/08/2014"), date("15/08/2014"))

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
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

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
		def logResult = project.log(date("16/08/2014"), date("17/08/2014"))

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
		def logResult = project.log(date("17/08/2014"), date("18/08/2014"))

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
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == MODIFIED
		assert change.fileContent().value == "file1 new content"
		assert change.fileContentBefore().value == "file1 content"
	}

	@Test void "log content of new file"() {
		def repository = 'repo with two added and modified files'()

		def project = newProject(repository)
		def logResult = project.log(date("11/08/2014"), date("12/08/2014"))

		def change = logResult.commits().first().changes.first()
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
	}

	private static VcsProject newProject(HgRepository repository) {
		def project = new VcsProject(new HgVcsRoot(newProjectPath(), repository.path, hgSettings))
		project.cloneToLocal()
		project
	}
}
