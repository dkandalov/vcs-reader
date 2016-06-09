package org.vcsreader.vcs.git

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.vcsreader.VcsChange
import org.vcsreader.VcsProject
import org.vcsreader.vcs.Change
import org.vcsreader.vcs.Commit
import org.vcsreader.vcs.commandlistener.VcsCommand
import org.vcsreader.vcs.commandlistener.VcsCommandListener

import static org.vcsreader.VcsChange.Type.*
import static org.vcsreader.VcsChange.noRevision
import static org.vcsreader.lang.DateTimeUtil.date
import static org.vcsreader.lang.DateTimeUtil.dateTime
import static org.vcsreader.lang.FileUtil.findSequentNonExistentFile
import static org.vcsreader.lang.FileUtil.tempDirectoryFile
import static org.vcsreader.vcs.TestUtil.assertEqualCommits

class GitIntegrationTest {
	private static final repository = new GitRepository(newReferenceRepoPath())

	private static final author = repository.author
	private static final gitSettings = GitSettings.defaults().withGitPath(repository.pathToGit)
	private static final String projectFolder = newProjectPath()

	private VcsProject project

	@Test void "clone project"() {
		def repository = new GitRepository(newReferenceRepoPath()).init()
		def project = new VcsProject(new GitVcsRoot(newProjectPath(), repository.path))

		def cloneResult = project.cloneToLocal()
		assert cloneResult.vcsErrors().empty
		assert cloneResult.isSuccessful()
	}

	@Test void "clone project failure"() {
		def vcsRoot = new GitVcsRoot(newProjectPath(), "/tmp/non-existent-repo-path", gitSettings)
		def project = new VcsProject(vcsRoot)

		def cloneResult = project.cloneToLocal()

		assert !cloneResult.isSuccessful()
		assert cloneResult.vcsErrors().size() == 1
	}

	@Test void "update project"() {
		def updateResult = project.update()
		assert updateResult.vcsErrors().empty
		assert updateResult.isSuccessful()
	}

	@Test void "update project failure"() {
		new File(projectFolder).deleteDir() // delete project folder so that git update fails

		def updateResult = project.update()
		assert !updateResult.isSuccessful()
		assert updateResult.vcsErrors() != []
	}

	@Test void "log no commits for empty project history interval"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file.txt")
			commit("initial commit", "Aug 10 00:00:00 2014 +0000")
			it
		}

		def project = newProject(repository)
		def logResult = project.log(date("01/08/2014"), date("02/08/2014"))

		assert logResult.commits().empty
		assert logResult.vcsErrors().empty
		assert logResult.isSuccessful()
	}

	@Test void "log single commit from project history (start date is inclusive, end date is exclusive)"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file1.txt")
			commit("initial commit", "Aug 10 00:00:00 2014 +0000")

			createFile("file2.txt")
			commit("added file2", "Aug 11 00:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("10/08/2014"), date("11/08/2014"))

		assertEqualCommits(logResult, [
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
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file1.txt")
			commit("initial commit", "Aug 10 00:00:00 2014 +0000")

			createFile("file2.txt")
			createFile("file3.txt")
			commit("added file2, file3", "Aug 11 00:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("10/08/2014"), date("12/08/2014"))

		assertEqualCommits(logResult, [
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

	@Test void "log commit with modified files"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file2.txt", "file2 content")
			createFile("file3.txt", "file3 content")
			commit("added file2, file3", "Aug 11 00:00:00 2014 +0000")

			createFile("file2.txt", "file2 new content")
			createFile("file3.txt", "file3 new content")
			commit("modified file2, file3", "Aug 12 14:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		VcsProject project = newProject(repository)
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		assertEqualCommits(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("14:00:00 12/08/2014"),
				repository.author,
				"modified file2, file3",
				[
					new Change(MODIFIED, "file2.txt", "file2.txt", revisions[1], revisions[0]),
					new Change(MODIFIED, "file3.txt", "file3.txt", revisions[1], revisions[0])
				]
			)
		])
	}

	@Test void "log moved file commit"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file.txt")
			commit("initial commit", "Aug 10 00:00:00 2014 +0000")

			mkdir("folder")
			move("file.txt",  "folder/file.txt")
			commit("moved file", "Aug 13 14:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("13/08/2014"), date("14/08/2014"))

		assertEqualCommits(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("14:00:00 13/08/2014"),
				repository.author,
				"moved file",
				[new Change(MOVED, "folder/file.txt", "file.txt", revisions[1], revisions[0])]
			)
		])
	}

	@Test void "log moved and renamed file commit"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file.txt")
			commit("initial commit", "Aug 10 00:00:00 2014 +0000")

			mkdir("folder")
			move("file.txt", "folder/renamed_file.txt")
			commit("moved and renamed file", "Aug 14 14:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("14/08/2014"), date("15/08/2014"))

		assertEqualCommits(logResult, [
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
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file.txt")
			commit("initial commit", "Aug 10 00:00:00 2014 +0000")

			delete("file.txt")
			commit("deleted file", "Aug 15 14:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

		assertEqualCommits(logResult, [
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
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file with spaces.txt")
			commit("added file with spaces and quotes", "Aug 16 14:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("16/08/2014"), date("17/08/2014"))

		assertEqualCommits(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("14:00:00 16/08/2014"),
				author,
				"added file with spaces and quotes",
				[new Change(ADDED, "file with spaces.txt", "", revisions[0], noRevision)]
			)
		])
	}

	@Test void "log commit with non-ascii message and file content"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("non-ascii.txt", "non-ascii содержимое")
			commit("non-ascii комментарий", "Aug 17 15:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("17/08/2014"), date("18/08/2014"))

		assertEqualCommits(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("15:00:00 17/08/2014"),
				author,
				"non-ascii комментарий",
				[new Change(ADDED, "non-ascii.txt", "", revisions[0], noRevision)]
			)
		])
		def nonAsciiContent = logResult.commits().first().changes.first().fileContent().value
		assert nonAsciiContent == "non-ascii содержимое"
	}

	@Test void "log commit with empty message"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file4.txt")
			commit("", "Aug 18 16:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("18/08/2014"), date("19/08/2014"))

		assertEqualCommits(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("16:00:00 18/08/2014"),
				author,
				"",
				[new Change(ADDED, "file4.txt", "", revisions[0], noRevision)]
			)
		])
	}

	@Test void "log commit with no changes"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			commit("commit with no changes", "Aug 19 17:00:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("19/08/2014"), date("20/08/2014"))

		assertEqualCommits(logResult, [
			new Commit(
				revisions[0], noRevision,
				dateTime("17:00:00 19/08/2014"),
				author,
				"commit with no changes",
				[]
			)
		])
	}

	@Test void "log branch rebase"() {
		def repository = new GitRepository(newReferenceRepoPath()).init().with {
			createFile("file-master.txt", "file-master content")
			commit("added file-master.txt", "Aug 20 18:10:00 2014 +0000")

			createAndCheckoutBranch("a-branch")
			createFile("file-branch.txt", "file branch content")
			commit("added file-branch.txt", "Aug 20 18:00:00 2014 +0000")

			// simulate what happens on rebase when committer date changes but author date doesn't
			rebase("master", "added file-branch.txt", "Aug 20 18:20:00 2014 +0000")
			it
		}
		def revisions = repository.revisions

		def project = newProject(repository)
		def logResult = project.log(date("20/08/2014"), date("21/08/2014"))

		assertEqualCommits(logResult, [
			new Commit(
				revisions[1], revisions[0],
				dateTime("18:00:00 20/08/2014"),
				author,
				"added file-branch.txt",
				[new Change(ADDED, "file-branch.txt", "", revisions[1], noRevision)]
			),
			new Commit(
				revisions[0], noRevision,
				dateTime("18:10:00 20/08/2014"),
				author,
				"added file-master.txt",
				[new Change(ADDED, "file-master.txt", "", revisions[0], noRevision)]
			)
		])
	}

	@Test void "log branch merge"() {
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
		def logResult = project.log(date("12/08/2014"), date("13/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == MODIFIED
		assert change.fileContent().value == "file2 new content"
		assert change.fileContentBefore().value == "file2 content"

		assert logResult.isSuccessful()
	}

	@Test void "log content of new file"() {
		def logResult = project.log(date("11/08/2014"), date("12/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == ADDED
		assert change.fileContent().value == "file2 content"
		assert change.fileContentBefore() == VcsChange.FileContent.none

		assert logResult.isSuccessful()
	}

	@Test void "log content of deleted file"() {
		def logResult = project.log(date("15/08/2014"), date("16/08/2014"))

		def change = logResult.commits().first().changes.first()
		assert change.type == DELETED
		assert change.fileContent() == VcsChange.FileContent.none
		assert change.fileContentBefore().value == "file1 content"

		assert logResult.isSuccessful()
	}


	private static VcsProject newProject(GitRepository repository) {
		def project = new VcsProject(new GitVcsRoot(newProjectPath(), repository.path))
		project.cloneToLocal()
		project
	}

	private static String revision(int i) {
		repository.revisions[i - 1]
	}

	private static String newReferenceRepoPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "git-reference-repo-${GitIntegrationTest.simpleName}", "")
		assert file.mkdirs()
		file.deleteOnExit()
		file.absolutePath
	}

	private static String newProjectPath() {
		def file = findSequentNonExistentFile(tempDirectoryFile(), "git-repo-${GitIntegrationTest.simpleName}", "")
		assert file.mkdirs()
		file.deleteOnExit()
		file.absolutePath
	}

	@Before void setup() {
		new File(projectFolder).deleteDir()
		new File(projectFolder).mkdirs()

		def vcsRoot = new GitVcsRoot(projectFolder, repository.path, gitSettings)
		project = new VcsProject([vcsRoot]).addListener(new VcsCommandListener() {
			@Override void beforeCommand(VcsCommand<?> command) { println(command.describe()) }
			@Override void afterCommand(VcsCommand<?> command) {}
		})
		project.cloneToLocal()
	}

	@BeforeClass static void setupConfig() {
		repository.createReferenceRepository()
	}
}
