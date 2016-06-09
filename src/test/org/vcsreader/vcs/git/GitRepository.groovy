package org.vcsreader.vcs.git

import org.vcsreader.lang.CommandLine

class GitRepository {
	final pathToGit = "/usr/local/Cellar/git/2.5.0/bin/git"
	final String path
	private List<String> revisions
	final author = "Some Author"
	private final authorWithEmail = "${author} <some.author@mail.com>"
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

	def createReferenceRepository() {
		git("init")
		createFile("file1.txt", "file1 content")
		commit("initial commit", "Aug 10 00:00:00 2014 +0000")

		createFile("file2.txt", "file2 content")
		createFile("file3.txt", "file3 content")
		commit("added file2, file3", "Aug 11 00:00:00 2014 +0000")

		createFile("file2.txt", "file2 new content")
		createFile("file3.txt", "file3 new content")
		commit("modified file2, file3", "Aug 12 14:00:00 2014 +0000")

		mkdir("folder1")
		move("file1.txt",  "folder1/file1.txt")
		commit("moved file1", "Aug 13 14:00:00 2014 +0000")

		mkdir("folder2")
		move("folder1/file1.txt", "folder2/renamed_file1.txt")
		commit("moved and renamed file1", "Aug 14 14:00:00 2014 +0000")

		delete("folder2/renamed_file1.txt")
		commit("deleted file1", "Aug 15 14:00:00 2014 +0000")

		createFile("file with spaces.txt", "123")
		commit("added file with spaces and quotes", "Aug 16 14:00:00 2014 +0000")

		createFile("non-ascii.txt", "non-ascii содержимое")
		commit("non-ascii комментарий", "Aug 17 15:00:00 2014 +0000")

		createFile("file4.txt", "commit with no message")
		commit("", "Aug 18 16:00:00 2014 +0000")

		commit("commit with no changes", "Aug 19 17:00:00 2014 +0000")

		//// branch rebase and merge
		createBranch("a-branch")
		checkoutBranch("a-branch")
		createFile("file1-branch.txt", "file1 branch content")
		commit("added file1-branch.txt", "Aug 20 18:00:00 2014 +0000")

		checkoutBranch("master")
		createFile("file1-master.txt", "file1-master content")
		commit("added file1-master.txt", "Aug 20 18:10:00 2014 +0000")

		checkoutBranch("a-branch")

		// simulate what happens on rebase when commit date changes but author date doesn't
		rebase("master", "added file1-branch.txt", "Aug 20 18:20:00 2014 +0000")
		createFile("file2-branch.txt", "file2 branch content")
		commit("added file2-branch.txt", "Aug 21 19:00:00 2014 +0000")

		checkoutBranch("master")
		createFile("file2-master.txt", "file2-master content")
		commit("added file2-master.txt", "Aug 21 19:10:00 2014 +0000")
		mergeBranch("a-branch", "merged branch into master", "Aug 21 19:20:00 2014 +0000")
		//// end of branch rebase and merge

		revisions = logCommitHashes()
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

	def createFile(String fileName, String text = "") {
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

	private def logCommitHashes() {
		printOutput = false
		def stdout = git("log").stdout()
		printOutput = true

		stdout.split("\n")
			.findAll{ it.startsWith("commit ") }
			.collect{ it.replace("commit ", "") }
			.reverse()
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
		def commandLine = new CommandLine([pathToGit] + args.toList())
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
}
