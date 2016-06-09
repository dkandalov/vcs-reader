package org.vcsreader.vcs.git

import org.vcsreader.lang.CommandLine

class GitRepository {
	static final pathToGit = "/usr/local/Cellar/git/2.5.0/bin/git"
	static final author = "Some Author"

	final String path
	private List<String> revisions
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
		// TODO do a dummy commit to make git create master branch?

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

	private def logCommitHashes() {
		printOutput = false
		def stdout = git("log").stdout()
		printOutput = true

		stdout.split("\n")
				.findAll{ it.startsWith("commit ") }
				.collect{ it.replace("commit ", "") }
				.reverse()
	}
}
