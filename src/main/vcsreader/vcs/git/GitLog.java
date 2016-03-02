package vcsreader.vcs.git;

import vcsreader.Change;
import vcsreader.Commit;
import vcsreader.lang.CommandLine;
import vcsreader.vcs.commandlistener.VcsCommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static vcsreader.Change.Type.DELETED;
import static vcsreader.Change.Type.ADDED;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.lang.Charsets.UTF8;
import static vcsreader.vcs.git.GitCommitParser.*;
import static vcsreader.vcs.git.GitCommandLine.isSuccessful;

/**
 * See https://git-scm.com/docs/git-log
 */
// suppress because it's similar to HgLog
@SuppressWarnings("Duplicates")
class GitLog implements VcsCommand<LogResult> {
	private final String gitPath;
	private final String folder;
	private final Date fromDate;
	private final Date toDate;

	private final CommandLine commandLine;
	private final List<CommandLine> externalSubCommands = new ArrayList<CommandLine>();


	public GitLog(String gitPath, String folder, Date fromDate, Date toDate) {
		this.gitPath = gitPath;
		this.folder = folder;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.commandLine = gitLog(gitPath, folder, fromDate, toDate);
	}

	@Override public LogResult execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			List<Commit> commits = parseListOfCommits(commandLine.stdout());
			commits = handleFileRenamesIn(commits);

			List<String> errors = (commandLine.stderr().trim().isEmpty() ? new ArrayList<String>() : asList(commandLine.stderr()));
			return new LogResult(commits, errors);
		} else {
			return new LogResult(new ArrayList<Commit>(), asList(commandLine.stderr()));
		}
	}

	static CommandLine gitLog(String gitPath, String folder, Date fromDate, Date toDate) {
		String from = "--after=" + Long.toString(fromDate.getTime() / 1000);
		String to = "--before=" + Long.toString((toDate.getTime() / 1000) - 1);
		String showFileStatus = "--name-status"; // see --diff-filter at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
		String forceUTF8ForCommitMessages = "--encoding=" + UTF8.name();
		CommandLine commandLine = new CommandLine(gitPath, "log", logFormat(), from, to, showFileStatus, forceUTF8ForCommitMessages);
		return commandLine.workingDir(folder).outputCharset(UTF8);
	}

	static CommandLine gitLogRenames(String gitPath, String folder, String revision) {
		// based on git4idea.history.GitHistoryUtils#getFirstCommitRenamePath
		return new CommandLine(gitPath, "show", "-M", "--pretty=format:", "--name-status", revision).workingDir(folder);
	}

	private List<Commit> handleFileRenamesIn(List<Commit> commits) {
		List<Commit> result = new ArrayList<Commit>();
		for (Commit commit : commits) {
			if (hasPotentialRenames(commit)) {
				CommandLine commandLine = gitLogRenames(gitPath, folder, commit.revision);
				externalSubCommands.add(commandLine);
				commandLine.execute();

				if (isSuccessful(commandLine)) {
					List<Change> updatedChanges = parseListOfChanges(commandLine.stdout(), commit.revision, commit.revisionBefore);
					commit = new Commit(commit.revision, commit.revisionBefore, commit.time, commit.author, commit.message, updatedChanges);
				}
			}
			result.add(commit);
		}
		return result;
	}

	private static boolean hasPotentialRenames(Commit commit) {
		boolean hasDeletions = false;
		boolean hasAdditions = false;
		for (Change change : commit.changes) {
			if (change.getType() == DELETED) hasDeletions = true;
			else if (change.getType() == ADDED) hasAdditions = true;
		}
		return hasDeletions && hasAdditions;
	}

	@Override public String describe() {
		String result = commandLine.describe();
		for (CommandLine commandLine : externalSubCommands) {
			result += "\n" + commandLine.describe();
		}
		return result;
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitLog gitLog = (GitLog) o;

		if (folder != null ? !folder.equals(gitLog.folder) : gitLog.folder != null) return false;
		if (fromDate != null ? !fromDate.equals(gitLog.fromDate) : gitLog.fromDate != null) return false;
		if (gitPath != null ? !gitPath.equals(gitLog.gitPath) : gitLog.gitPath != null) return false;
		if (toDate != null ? !toDate.equals(gitLog.toDate) : gitLog.toDate != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = gitPath != null ? gitPath.hashCode() : 0;
		result = 31 * result + (folder != null ? folder.hashCode() : 0);
		result = 31 * result + (fromDate != null ? fromDate.hashCode() : 0);
		result = 31 * result + (toDate != null ? toDate.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitLog{" +
				"toDate=" + toDate +
				", fromDate=" + fromDate +
				", folder='" + folder + '\'' +
				", gitPath='" + gitPath + '\'' +
				'}';
	}
}
