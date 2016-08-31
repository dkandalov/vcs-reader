package org.vcsreader.vcs.svn

import org.vcsreader.lang.CommandLine

import java.time.Instant
import java.time.ZoneOffset

import static org.vcsreader.lang.DateTimeUtil.dateTimeFormatter
import static org.vcsreader.lang.DateTimeUtil.dateTime
import static org.vcsreader.vcs.svn.SvnIntegrationTestConfig.*

class SvnRepository {
	final String repoPath
	final String path
	final List<String> revisions = []
	private boolean printOutput = true

	SvnRepository(String path = newProjectPath(), String repoPath = newReferenceRepoPath()) {
		this.path = path
		this.repoPath = repoPath
	}

	def init() {
		if (!new File(path).exists()) throw new IllegalStateException()
		if (!new File(repoPath).exists()) throw new IllegalStateException()

		svnAdmin("create", repoPath)
		createDummyHookToAllowSvnPropertyChanges("${repoPath}/hooks/pre-revprop-change")
		svn("checkout", "file://${repoPath}", path)

		this
	}

	def delete(String fileName) {
		svn("rm", fileName)
	}

	def move(String from, String to) {
		svn("move", from, to)
	}

	def mkdir(String folderName) {
		svn("mkdir", folderName)
	}

	def create(String fileName, String text = "", boolean isExecutable = false) {
		def file = new File(path + File.separator + fileName)
		def isNewFile = !file.exists()
		file.write(text)
		file.setExecutable(isExecutable)

		if (isNewFile) {
			svn("add", fileName)
		}
	}

	def propset(String property, String value, String fileName) {
		svn("propset", property, value, fileName)
	}

	def commit(String message, Instant commitTime) {
		svn("commit", "-m", message)
		svn("propset", "svn:date", "--revprop", "-r", "HEAD", dateTimeFormatter("yyyy-MM-dd'T'HH:mm:ss.000000'Z'", ZoneOffset.UTC).format(commitTime))
		svn("propset", "svn:author", "--revprop", "-r", "HEAD", author)

		if (revisions.empty) revisions.add("1")
		else revisions.add((revisions.last().toInteger() + 1).toString())
	}

	def dummyCommit(Instant commitTime) {
		create("dummy")
		commit("dummy commit otherwise svn log doesn't show commits", commitTime)
	}

	private def svn(Map environment = [:], String... args) {
		runCommand(pathToSvn, path, environment, args)
	}

	private def svnAdmin(String... args) {
		runCommand(pathToSvnAdmin, repoPath, args)
	}

	private def runCommand(String command, String workingDir, Map environment = [:], String... args) {
		def commandLine = new CommandLine([command] + args.toList())
				.workingDir(workingDir)
				.environment(environment)
				.execute()
		if (printOutput) {
			println(commandLine.describe())
			println(commandLine.stdout())
			println(commandLine.stderr())
		}
		if (commandLine.exitCode() != 0) {
			throw new IllegalStateException("Exit code ${commandLine.exitCode()} for command: ${commandLine.describe()}")
		}
		commandLine
	}

	private static createDummyHookToAllowSvnPropertyChanges(String filePath) {
		def file = new File(filePath)
		file.write("#!/bin/sh\nexit 0")
		file.setExecutable(true)
	}


	static class Scripts {
		static 'repo with two commits with three added files'() {
			new SvnRepository().init().with {
				create("file1.txt")
				commit("initial commit", dateTime("00:00:00 10/08/2014"))

				create("file2.txt")
				create("file3.txt")
				commit("added file2, file3", dateTime("00:00:00 11/08/2014"))
				it
			}
		}

		static 'repo with two added and modified files'() {
			new SvnRepository().init().with {
				create("file1.txt", "file1 content")
				create("file2.txt", "file2 content")
				commit("added file1, file2", dateTime("00:00:00 11/08/2014"))

				create("file1.txt", "file1 new content")
				create("file2.txt", "file2 new content")
				commit("modified file1, file2", dateTime("15:00:00 12/08/2014"))
				it
			}
		}

		static 'repo with moved file'() {
			new SvnRepository().init().with {
				create("file.txt")
				commit("initial commit", dateTime("00:00:00 10/08/2014"))

				mkdir("folder")
				move("file.txt", "folder/file.txt")
				commit("moved file", dateTime("15:00:00 13/08/2014"))
				it
			}
		}

		static 'repo with moved and renamed file'() {
			new SvnRepository().init().with {
				create("file.txt", "file content")
				commit("initial commit", dateTime("00:00:00 10/08/2014"))

				mkdir("folder")
				move("file.txt", "folder/renamed_file.txt")
				commit("moved and renamed file", dateTime("15:00:00 14/08/2014"))
				it
			}
		}

		static 'repo with deleted file'() {
			new SvnRepository().init().with {
				create("file.txt", "file content")
				commit("initial commit", dateTime("00:00:00 10/08/2014"))

				delete("file.txt")
				commit("deleted file", dateTime("15:00:00 15/08/2014"))
				it
			}
		}

		static 'repo with file with spaces and quotes'() {
			new SvnRepository().init().with {
				create('dummy')
				commit("dummy commit otherwise svn log doesn't show commits", dateTime("00:00:00 10/08/2014"))

				create('"file with spaces.txt"')
				commit("added file with spaces and quotes", dateTime("15:00:00 16/08/2014"))
				it
			}
		}

		static 'repo with non-ascii file name and commit message'() {
			new SvnRepository().init().with {
				dummyCommit(dateTime("00:00:00 10/08/2014"))

				create("non-ascii.txt", "non-ascii содержимое")
				commit("non-ascii комментарий", dateTime("15:00:00 17/08/2014"))
				it
			}
		}

		static someNonEmptyRepository() {
			new SvnRepository().init().with {
				dummyCommit(dateTime("00:00:00 10/08/2014"))
				it
			}
		}
	}
}
