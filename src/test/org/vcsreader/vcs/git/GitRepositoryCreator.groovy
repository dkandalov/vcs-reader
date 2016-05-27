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
		commit("initial commit", "Aug 10 00:00:00 2014 +0000")

		writeToFile("file2.txt", "file2 content")
		writeToFile("file3.txt", "file3 content")
		commit("added file2, file3", "Aug 11 00:00:00 2014 +0000")

		writeToFile("file2.txt", "file2 new content")
		writeToFile("file3.txt", "file3 new content")
		commit("modified file2, file3", "Aug 12 14:00:00 2014 +0000")

		mkdir("folder1")
		move("file1.txt",  "folder1/file1.txt")
		commit("moved file1", "Aug 13 14:00:00 2014 +0000")

		mkdir("folder2")
		move("folder1/file1.txt", "folder2/renamed_file1.txt")
		commit("moved and renamed file1", "Aug 14 14:00:00 2014 +0000")

		remove("folder2/renamed_file1.txt")
		commit("deleted file1", "Aug 15 14:00:00 2014 +0000")

		writeToFile("file with spaces.txt", "123")
		commit("added file with spaces and quotes", "Aug 16 14:00:00 2014 +0000")

		writeToFile("non-ascii.txt", "non-ascii содержимое")
		commit("non-ascii комментарий", "Aug 17 15:00:00 2014 +0000")

		writeToFile("file4.txt", "commit with no message")
		commit("", "Aug 18 16:00:00 2014 +0000")

		commit("commit with no changes", "Aug 19 17:00:00 2014 +0000")

		//// branch rebase and merge
		createBranch("a-branch")
		checkoutBranch("a-branch")
		writeToFile("file1-branch.txt", "file1 branch content")
		commit("added file1-branch.txt", "Aug 20 18:00:00 2014 +0000")

		checkoutBranch("master")
		writeToFile("file1-master.txt", "file1-master content")
		commit("added file1-master.txt", "Aug 20 18:10:00 2014 +0000")

		checkoutBranch("a-branch")

		// simulate what happens on rebase when commit date changes but author date doesn't
		rebase("master", "added file1-branch.txt", "Aug 20 18:20:00 2014 +0000")
		writeToFile("file2-branch.txt", "file2 branch content")
		commit("added file2-branch.txt", "Aug 21 19:00:00 2014 +0000")

		checkoutBranch("master")
		writeToFile("file2-master.txt", "file2-master content")
		commit("added file2-master.txt", "Aug 21 19:10:00 2014 +0000")
		mergeBranch("a-branch", "merged branch into master", "Aug 21 19:20:00 2014 +0000")
		//// end of branch rebase and merge

		println(logCommitHashes().collect{ '"' + it + '"' })
	}

	private def remove(String fileName) {
		new File(referenceProject + File.separator + fileName).delete()
	}

	private def move(String from, String to) {
		new File(referenceProject + File.separator + from)
			.renameTo(new File(referenceProject + File.separator + to))
	}

	private def mkdir(String folderName) {
		new File(referenceProject + File.separator + folderName).mkdir()
	}

	private def createBranch(String branchName) {
		git("branch", branchName)
	}

	private def checkoutBranch(String branchName) {
		git("checkout", branchName)
	}

	private def logCommitHashes() {
		def stdout = git("log").stdout()
		stdout.split("\n")
			.findAll{ it.startsWith("commit ") }
			.collect{ it.replace("commit ", "") }
			.reverse()
	}

	private def mergeBranch(String branchName, String message, String date) {
		git("merge", branchName, "-m", message)
		git("commit", "--amend", "--author", author, "--date", date, "-m", message)
	}

	private def rebase(String branchName, String message, String date) {
		git("rebase", branchName)
		git("commit", "--amend", "--author", author, "--date", date, "-m", message)
	}

	private def commit(String message, String date) {
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
		commandLine
	}

	private def writeToFile(String fileName, String text) {
		new File(referenceProject + File.separator + fileName).write(text)
	}
}
