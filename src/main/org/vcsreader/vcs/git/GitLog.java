package org.vcsreader.vcs.git;

import org.vcsreader.VcsChange;
import org.vcsreader.VcsCommit;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.Change;
import org.vcsreader.vcs.Commit;
import org.vcsreader.vcs.VcsCommand;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.vcsreader.VcsChange.Type.ADDED;
import static org.vcsreader.VcsChange.Type.DELETED;
import static org.vcsreader.VcsProject.LogResult;
import static org.vcsreader.lang.CharsetUtil.UTF8;
import static org.vcsreader.vcs.git.GitCommandLine.isSuccessful;
import static org.vcsreader.vcs.git.GitCommitParser.*;

/**
 * See https://git-scm.com/docs/git-log
 */
// suppress because it's similar to HgLog
@SuppressWarnings("Duplicates")
class GitLog implements VcsCommand<LogResult> {
	private final String gitPath;
	private final String folder;
	private final TimeRange timeRange;

	private final CommandLine commandLine;
	private final List<CommandLine> externalSubCommands = new ArrayList<>();


	public GitLog(String gitPath, String folder, TimeRange timeRange) {
		this.gitPath = gitPath;
		this.folder = folder;
		this.timeRange = timeRange;
		this.commandLine = gitLog(gitPath, folder, timeRange);
	}

	@Override public LogResult execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			List<VcsCommit> commits = parseListOfCommits(commandLine.stdout());
			commits = handleFileRenamesIn(commits);

			List<String> errors = (commandLine.stderr().trim().isEmpty() ? new ArrayList<>() : asList(commandLine.stderr()));
			return new LogResult(commits, errors);
		} else {
			return new LogResult(new ArrayList<>(), asList(commandLine.stderr()));
		}
	}

	static CommandLine gitLog(String gitPath, String folder, TimeRange timeRange) {
		String showFileStatus = "--name-status"; // see --diff-filter at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
		String forceUTF8ForCommitMessages = "--encoding=" + UTF8.name();

		List<String> arguments = new ArrayList<>(asList(gitPath, "log", logFormat(), showFileStatus, forceUTF8ForCommitMessages));

		// MIN timestamp is not handled correctly by git and must be excluded from command line.
		if (timeRange.from() != Instant.MIN) {
			arguments.add("--after=" + Long.toString(timeRange.from().getEpochSecond()));
		}
		arguments.add("--before=" + Long.toString(timeRange.to().getEpochSecond() - 1));

		return new CommandLine(arguments).workingDir(folder).outputCharset(UTF8);
	}

	static CommandLine gitLogRenames(String gitPath, String folder, String revision) {
		// based on git4idea.history.GitHistoryUtils#getFirstCommitRenamePath
		return new CommandLine(gitPath, "show", "-M", "--pretty=format:", "--name-status", revision).workingDir(folder);
	}

	private List<VcsCommit> handleFileRenamesIn(List<VcsCommit> commits) {
		List<VcsCommit> result = new ArrayList<>();
		for (VcsCommit commit : commits) {
			if (hasPotentialRenames(commit)) {
				CommandLine commandLine = gitLogRenames(gitPath, folder, commit.getRevision());
				externalSubCommands.add(commandLine);
				commandLine.execute();

				if (isSuccessful(commandLine)) {
					List<Change> updatedChanges = parseListOfChanges(commandLine.stdout(), commit.getRevision(), commit.getRevisionBefore());
					commit = new Commit(commit.getRevision(), commit.getRevisionBefore(), commit.getTime(), commit.getAuthor(), commit.getMessage(), updatedChanges);
				}
			}
			result.add(commit);
		}
		return result;
	}

	private static boolean hasPotentialRenames(VcsCommit commit) {
		boolean hasDeletions = false;
		boolean hasAdditions = false;
		for (VcsChange change : commit.getChanges()) {
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

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitLog gitLog = (GitLog) o;

		if (gitPath != null ? !gitPath.equals(gitLog.gitPath) : gitLog.gitPath != null) return false;
		if (folder != null ? !folder.equals(gitLog.folder) : gitLog.folder != null) return false;
		if (timeRange != null ? !timeRange.equals(gitLog.timeRange) : gitLog.timeRange != null) return false;
		if (commandLine != null ? !commandLine.equals(gitLog.commandLine) : gitLog.commandLine != null) return false;
		return externalSubCommands != null ? externalSubCommands.equals(gitLog.externalSubCommands) : gitLog.externalSubCommands == null;
	}

	@Override public int hashCode() {
		int result = gitPath != null ? gitPath.hashCode() : 0;
		result = 31 * result + (folder != null ? folder.hashCode() : 0);
		result = 31 * result + (timeRange != null ? timeRange.hashCode() : 0);
		result = 31 * result + (commandLine != null ? commandLine.hashCode() : 0);
		result = 31 * result + (externalSubCommands != null ? externalSubCommands.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitLog{" +
				"gitPath='" + gitPath + '\'' +
				", folder='" + folder + '\'' +
				", timeRange=" + timeRange +
				", commandLine=" + commandLine +
				", externalSubCommands=" + externalSubCommands +
				'}';
	}
}
