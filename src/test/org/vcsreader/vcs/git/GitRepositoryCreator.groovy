package org.vcsreader.vcs.git

import org.vcsreader.lang.CommandLine

class GitRepositoryCreator {
	private final pathToGit = "/usr/local/Cellar/git/2.5.0/bin/git"
	private final referenceProject = "/tmp/reference-repos/git-repo"
	private final author = "Some Author <some.author@mail.com>"

	// TODO move initialisation to tests and remove main()
	public static void main(String[] args) {
		new GitRepositoryCreator().createReferenceRepository()
	}

	def createReferenceRepository() {
		new File(referenceProject).deleteDir()
		new File(referenceProject).mkdirs()

		git("init")
		writeToFile("file1.txt", "file1 content")
		gitCommit("initial commit", "Aug 10 00:00:00 2014 +0000")
	}

	private def gitCommit(String message, String date) {
		git("add", "--all",  ".")
		// allow empty messages and empty commits (without changes) to test it
		git("commit", "--allow-empty-message", "--allow-empty", "-m", message)
		git("commit", "--allow-empty-message", "--allow-empty", "--amend", "--author", author, "--date", date, "-m", message)
		// TODO what about GIT_COMMITTER_DATE env parameter, is it required?
	}

	private def git(String... args) {
		def commandLine = new CommandLine([pathToGit] + args.toList()).workingDir(referenceProject).execute()
		println(commandLine.stdout())
		println(commandLine.stderr())
		if (commandLine.exitCode() != 0) {
			throw new IllegalStateException("Exit code ${commandLine.exitCode()} for command: ${commandLine.describe()}")
		}
	}

	private def writeToFile(String fileName, String text) {
		new File(referenceProject + File.separator + fileName).write(text)
	}
}
