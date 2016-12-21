package org.vcsreader.vcs.hg;

import org.vcsreader.LogResult;
import org.vcsreader.VcsCommit;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.VcsCommand;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.vcsreader.vcs.hg.HgCommandLine.containsHgRepo;
import static org.vcsreader.vcs.hg.HgCommandLine.isSuccessful;

// suppress because it's similar to GitLog
@SuppressWarnings("Duplicates")
class HgLog implements VcsCommand<LogResult> {
	private final String hgPath;
	private final String folder;
	private final TimeRange timeRange;
	private final CommandLine commandLine;


	public HgLog(String hgPath, String folder, TimeRange timeRange) {
		this.hgPath = hgPath;
		this.folder = folder;
		this.timeRange = timeRange;
		this.commandLine = hgLog(hgPath, folder, timeRange);
	}

	@Override public LogResult execute() {
		if (!containsHgRepo(folder)) {
			throw new VcsCommand.Failure("Folder doesn't contain git repository: '" + folder + "'.");
		}

		commandLine.execute();

		if (isSuccessful(commandLine)) {
			List<VcsCommit> commits = HgCommitParser.parseListOfCommits(commandLine.stdout());
			List<String> errors = (commandLine.stderr().trim().isEmpty() ? new ArrayList<>() : asList(commandLine.stderr()));
			return new LogResult(commits, errors);
		} else {
			return new LogResult(new ArrayList<>(), asList(commandLine.stderr()));
		}
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	static CommandLine hgLog(String hgPath, String folder, TimeRange timeRange) {
		CommandLine commandLine = new CommandLine(
				hgPath, "log",
				"--encoding", UTF_8.name(),
				"-r", "date(\"" + asHgInstant(timeRange.from()) + " to " + asHgInstant(timeRange.to()) + "\")",
				"--template", HgCommitParser.logTemplate()
		);
		return commandLine.workingDir(folder).outputCharset(UTF_8);
	}

	private static String asHgInstant(Instant instant) {
		// see 'hg help dates'
		long epochSeconds = instant.getEpochSecond() - 1;
		long clampedEpochSeconds = min(Integer.MAX_VALUE, max(0, epochSeconds));
		String secondsSinceEpoch = Long.toString(clampedEpochSeconds);
		String utcOffset = "0";
		return secondsSinceEpoch + " " + utcOffset;
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HgLog hgLog = (HgLog) o;

		if (hgPath != null ? !hgPath.equals(hgLog.hgPath) : hgLog.hgPath != null) return false;
		if (folder != null ? !folder.equals(hgLog.folder) : hgLog.folder != null) return false;
		if (timeRange != null ? !timeRange.equals(hgLog.timeRange) : hgLog.timeRange != null) return false;
		return commandLine != null ? commandLine.equals(hgLog.commandLine) : hgLog.commandLine == null;
	}

	@Override public int hashCode() {
		int result = hgPath != null ? hgPath.hashCode() : 0;
		result = 31 * result + (folder != null ? folder.hashCode() : 0);
		result = 31 * result + (timeRange != null ? timeRange.hashCode() : 0);
		result = 31 * result + (commandLine != null ? commandLine.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "HgLog{" +
				"hgPath='" + hgPath + '\'' +
				", folder='" + folder + '\'' +
				", timeRange=" + timeRange +
				", commandLine=" + commandLine +
				'}';
	}
}
