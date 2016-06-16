package org.vcsreader.vcs.git

import org.vcsreader.lang.CommandLine

import static org.vcsreader.vcs.git.GitIntegrationTestConfig.authorWithEmail
import static org.vcsreader.vcs.git.GitIntegrationTestConfig.newReferenceRepoPath

class GitRepository {
	final String path
	private List<String> revisions
	private boolean printOutput = true


	GitRepository(String path) {
		this.path = path
	}

	def init() {
		if (!new File(path).exists()) {
			throw new IllegalStateException()
		}
		git("init")
		this
	}

	def getRevisions() {
		if (revisions == null) {
			revisions = logCommitHashes()
		}
		revisions
	}

	def delete(String fileName) {
		def wasDeleted = new File(path + File.separator + fileName).delete()
		assert wasDeleted
	}

	def move(String from, String to) {
		def fromPath = path + File.separator + from
		def toPath = path + File.separator + to
		def wasMoved = new File(fromPath).renameTo(new File(toPath))
		assert wasMoved
	}

	def mkdir(String folderName) {
		def wasCreated = new File(path + File.separator + folderName).mkdir()
		assert wasCreated
	}

	def create(String fileName, String text = "") {
		new File(path + File.separator + fileName).write(text)
	}

	def createAndCheckoutBranch(String branchName) {
		createBranch(branchName)
		checkoutBranch(branchName)
	}

	def createBranch(String branchName) {
		git("branch", branchName)
	}

	def checkoutBranch(String branchName) {
		git("checkout", branchName)
	}

	def mergeBranch(String branchName, String message, String date) {
		git("merge", branchName, "-m", message)
		def env = ["GIT_COMMITTER_DATE": date]
		git(env, "commit", "--amend", "--author", authorWithEmail, "--date", date, "-m", message)
	}

	def rebase(String branchName, String message, String committerDate) {
		git("rebase", branchName)
		def env = ["GIT_COMMITTER_DATE": committerDate]
		git(env, "commit", "--amend", "-m", message)
	}

	def commit(String message, String date) {
		git("add", "--all",  ".")
		// allow empty messages and empty commits (without changes) to test it
		git("commit", "--allow-empty-message", "--allow-empty", "-m", message)

		// committer date has to be specified because when requesting git log it checks committer date not author date
		def env = ["GIT_COMMITTER_DATE": date]
		git(env, "commit", "--allow-empty-message", "--allow-empty", "--amend", "--author", authorWithEmail, "--date", date, "-m", message)
	}

	private def git(Map environment = [:], String... args) {
		def commandLine = new CommandLine([GitIntegrationTestConfig.pathToGit] + args.toList())
				.workingDir(path)
				.environment(environment)
				.execute()
		if (printOutput) {
			println(commandLine.stdout())
			println(commandLine.stderr())
		}
		if (commandLine.exitCode() != 0) {
			throw new IllegalStateException("Exit code ${commandLine.exitCode()} for command: ${commandLine.describe()}")
		}
		commandLine
	}

	private def logCommitHashes() {
		printOutput = false
		def stdout = git("log").stdout()
		printOutput = true

		stdout.split("\n")
				.findAll{ it.startsWith("commit ") }
				.collect{ it.replace("commit ", "") }
				.reverse()
	}


	static class Scripts {
		static 'repo with two commits with three added files'() {
			new GitRepository(newReferenceRepoPath()).init().with {
				create("file1.txt")
				commit("initial commit", "Aug 10 00:00:00 2014 +0000")

				create("file2.txt")
				create("file3.txt")
				commit("added file2, file3", "Aug 11 00:00:00 2014 +0000")
				it
			}
		}

		static 'repo with two added and modified files'() {
			new GitRepository(newReferenceRepoPath()).init().with {
				create("file1.txt", "file1 content")
				create("file2.txt", "file2 content")
				commit("added file1, file2", "Aug 11 00:00:00 2014 +0000")

				create("file1.txt", "file1 new content")
				create("file2.txt", "file2 new content")
				commit("modified file1, file2", "Aug 12 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with moved file'() {
			new GitRepository(newReferenceRepoPath()).init().with {
				create("file.txt")
				commit("initial commit", "Aug 10 00:00:00 2014 +0000")

				mkdir("folder")
				move("file.txt",  "folder/file.txt")
				commit("moved file", "Aug 13 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with moved and renamed file'() {
			new GitRepository(newReferenceRepoPath()).init().with {
				create("file.txt")
				commit("initial commit", "Aug 10 00:00:00 2014 +0000")

				mkdir("folder")
				move("file.txt", "folder/renamed_file.txt")
				commit("moved and renamed file", "Aug 14 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with deleted file'() {
			new GitRepository(newReferenceRepoPath()).init().with {
				create("file.txt", "file content")
				commit("initial commit", "Aug 10 00:00:00 2014 +0000")

				delete("file.txt")
				commit("deleted file", "Aug 15 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with file with spaces and quotes'() {
			new GitRepository(newReferenceRepoPath()).init().with {
				create('"file with spaces.txt"')
				commit("added file with spaces and quotes", "Aug 16 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with non-ascii file name and commit message'() {
			new GitRepository(newReferenceRepoPath()).init().with {
				create("non-ascii.txt", "non-ascii содержимое")
				commit("non-ascii комментарий", "Aug 17 15:00:00 2014 +0000")
				it
			}
		}

		static someNonEmptyRepository() {
			'repo with two added and modified files'()
		}

	}
}
