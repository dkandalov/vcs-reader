package org.vcsreader.vcs.hg

import org.vcsreader.lang.CommandLine

import static org.vcsreader.vcs.hg.HgIntegrationTestConfig.authorWithEmail
import static org.vcsreader.vcs.hg.HgIntegrationTestConfig.newReferenceRepoPath


class HgRepository {
	final String path
	private List<String> revisions
	private boolean printOutput = true

	HgRepository(String path) {
		this.path = path
	}

	def init() {
		if (!new File(path).exists()) {
			throw new IllegalStateException()
		}
		hg("init")
		this
	}

	def getRevisions() {
		if (revisions == null) {
			revisions = logCommitHashes()
		}
		revisions
	}

	def delete(String fileName) {
		hg("rm", fileName)
	}

	def move(String from, String to) {
		hg("mv", from, to)
	}

	def mkdir(String folderName) {
		def wasCreated = new File(path + File.separator + folderName).mkdir()
		assert wasCreated
	}

	def create(String fileName, String text = "") {
		new File(path + File.separator + fileName).write(text)
	}

	def commit(String message, String date) {
		hg("add")
		hg("commit", "-u", authorWithEmail, "-d", date, "-m", message)
	}

	private def logCommitHashes() {
		printOutput = false
		def stdout = hg("log", "--template", '{node}\\n').stdout()
		printOutput = true

		stdout.trim().split("\n").reverse()
	}

	private def hg(Map environment = [:], String... args) {
		def commandLine = new CommandLine([HgIntegrationTestConfig.pathToHg] + args.toList())
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


	static class Scripts {
		static 'repo with two commits with three added files'() {
			new HgRepository(newReferenceRepoPath()).init().with {
				create("file1.txt")
				commit("initial commit", "Aug 10 00:00:00 2014 +0000")

				create("file2.txt")
				create("file3.txt")
				commit("added file2, file3", "Aug 11 00:00:00 2014 +0000")
				it
			}
		}

		static 'repo with two added and modified files'() {
			new HgRepository(newReferenceRepoPath()).init().with {
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
			new HgRepository(newReferenceRepoPath()).init().with {
				create("file.txt")
				commit("initial commit", "Aug 10 00:00:00 2014 +0000")

				mkdir("folder")
				move("file.txt",  "folder/file.txt")
				commit("moved file", "Aug 13 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with moved and renamed file'() {
			new HgRepository(newReferenceRepoPath()).init().with {
				create("file.txt")
				commit("initial commit", "Aug 10 00:00:00 2014 +0000")

				mkdir("folder")
				move("file.txt", "folder/renamed_file.txt")
				commit("moved and renamed file", "Aug 14 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with deleted file'() {
			new HgRepository(newReferenceRepoPath()).init().with {
				create("file.txt", "file content")
				commit("initial commit", "Aug 10 00:00:00 2014 +0000")

				delete("file.txt")
				commit("deleted file", "Aug 15 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with file with spaces and quotes'() {
			new HgRepository(newReferenceRepoPath()).init().with {
				create('"file with spaces.txt"')
				commit("added file with spaces and quotes", "Aug 16 14:00:00 2014 +0000")
				it
			}
		}

		static 'repo with non-ascii file name and commit message'() {
			new HgRepository(newReferenceRepoPath()).init().with {
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
