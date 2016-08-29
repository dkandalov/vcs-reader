package org.vcsreader.vcs.svn;

import org.vcsreader.VcsChange;
import org.vcsreader.VcsCommit;
import org.vcsreader.lang.CharsetUtil;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.Change;
import org.vcsreader.vcs.VcsCommand;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.vcsreader.VcsProject.LogResult;
import static org.vcsreader.lang.DateTimeUtil.UTC;
import static org.vcsreader.vcs.svn.SvnCommandLine.isSuccessful;
import static org.vcsreader.vcs.svn.SvnCommandLine.newExternalCommand;

/**
 * See http://svnbook.red-bean.com/en/1.8/svn.ref.svn.c.log.html
 */
class SvnLog implements VcsCommand<LogResult> {
	private final String pathToSvn;
	private final String repositoryUrl;
	private final String repositoryRoot;
	private final TimeRange timeRange;
	private final boolean useMergeHistory;
	private final boolean quoteDateRange;
	private final CommandLine commandLine;

	public SvnLog(String pathToSvn, String repositoryUrl, String repositoryRoot, TimeRange timeRange,
	              boolean useMergeHistory, boolean quoteDateRange) {
		this.pathToSvn = pathToSvn;
		this.repositoryUrl = repositoryUrl;
		this.repositoryRoot = repositoryRoot;
		this.timeRange = timeRange;
		this.useMergeHistory = useMergeHistory;
		this.quoteDateRange = quoteDateRange;
		this.commandLine = svnLog(pathToSvn, repositoryUrl, timeRange, useMergeHistory, quoteDateRange);
	}

	@Override public LogResult execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			List<VcsCommit> allCommits = SvnCommitParser.parseCommits(commandLine.stdout());
			List<VcsCommit> commits = transformToSubPathCommits(deleteCommitsBefore(timeRange.from(), allCommits));
			return new LogResult(commits, new ArrayList<>());
		} else {
			return new LogResult(Collections.emptyList(), asList(commandLine.stderr()));
		}
	}

	static CommandLine svnLog(String pathToSvn, String repositoryUrl, TimeRange timeRange,
	                          boolean useMergeHistory, boolean quoteDateRange) {
		// see http://svnbook.red-bean.com/en/1.8/svn.branchmerge.advanced.html
		// see http://stackoverflow.com/questions/987337/preserving-history-when-merging-subversion-branches
		String mergeHistory = (useMergeHistory ? "--use-merge-history" : "");

		// see http://subversion.tigris.org/issues/show_bug.cgi?id=2938
		@SuppressWarnings("UnnecessaryLocalVariable")
		Charset svnXmlCharset = CharsetUtil.UTF8;

		return newExternalCommand(
				pathToSvn, "log",
				repositoryUrl,
				"-r", svnDateRange(timeRange, quoteDateRange),
				mergeHistory,
				"--verbose",
				"--xml"
		).outputCharset(svnXmlCharset);
	}

	private static String svnDateRange(TimeRange timeRange, boolean quoteDateRange) {
		timeRange = new TimeRange(timeRange.from(), timeRange.to().minusMillis(1000)); // This is to make toDate exclusive.

		// Svn supports any ISO 8601 date format (https://en.wikipedia.org/wiki/ISO_8601).
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(UTC.toZoneId());

		String result = "{" + formatter.format(timeRange.from()) + "}:{" + formatter.format(timeRange.to()) + "}";
		if (quoteDateRange) {
			result = "'" + result + "'";
		}
		return result;
	}

	/**
	 * Delete commits because "Subversion will find the most recent revision of the repository as of the date you give".
	 * See http://svnbook.red-bean.com/en/1.8/svn.tour.revs.specifiers.html#svn.tour.revs.keywords
	 */
	private static List<VcsCommit> deleteCommitsBefore(Instant instant, List<VcsCommit> commits) {
		Iterator<VcsCommit> iterator = commits.iterator();
		while (iterator.hasNext()) {
			VcsCommit commit = iterator.next();
			if (commit.getTime().getTime() < instant.toEpochMilli()) iterator.remove();
		}
		return commits;
	}

	private List<VcsCommit> transformToSubPathCommits(List<VcsCommit> commits) {
		String subPath = subPathOf(repositoryUrl, repositoryRoot);
		commits = removeChangesNotIn(subPath, commits);
		commits = modifyChanges(subPath, commits);
		return commits;
	}

	private static List<VcsCommit> modifyChanges(String subPath, List<VcsCommit> commits) {
		List<VcsCommit> result = new ArrayList<>();
		for (VcsCommit commit : commits) {
			List<Change> modifiedChanges = new ArrayList<>();
			for (VcsChange vcsChange : commit.getChanges()) {
				Change change = (Change) vcsChange;
				modifiedChanges.add(change.withTypeAndPaths(
						changeTypeConsideringSubPath(subPath, change),
						useSubPathAsRoot(subPath, change.getFilePath()),
						useSubPathAsRoot(subPath, change.getFilePathBefore())
				));
			}
			result.add(commit.withChanges(modifiedChanges));
		}
		return result;
	}

	private static List<VcsCommit> removeChangesNotIn(String subPath, List<VcsCommit> commits) {
		List<VcsCommit> result = new ArrayList<>();
		for (VcsCommit commit : commits) {
			List<VcsChange> filteredChanges = new ArrayList<>();
			for (VcsChange change : commit.getChanges()) {

				if (change.getFilePath().startsWith(subPath) || change.getFilePathBefore().startsWith(subPath)) {
					filteredChanges.add(change);
				}

			}
			result.add(commit.withChanges(filteredChanges));
		}
		return result;
	}

	private static String subPathOf(String repositoryUrl, String repositoryRoot) {
		String subPath = repositoryUrl.replace(repositoryRoot, "");
		if (subPath.startsWith("/")) subPath = subPath.substring(1);
		if (!subPath.isEmpty() && !subPath.endsWith("/")) subPath += "/";
		return subPath;
	}

	private static VcsChange.Type changeTypeConsideringSubPath(String subPath, VcsChange change) {
		if (change.getType() != VcsChange.Type.MOVED) return change.getType();
		else if (!change.getFilePath().startsWith(subPath)) return VcsChange.Type.DELETED;
		else if (!change.getFilePathBefore().startsWith(subPath)) return VcsChange.Type.ADDED;
		else return change.getType();
	}

	private static String useSubPathAsRoot(String subPath, String filePath) {
		int i = filePath.indexOf(subPath);
		if (i != 0) return VcsChange.noFilePath;
		else return filePath.substring(subPath.length());
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnLog svnLog = (SvnLog) o;

		if (useMergeHistory != svnLog.useMergeHistory) return false;
		if (quoteDateRange != svnLog.quoteDateRange) return false;
		if (pathToSvn != null ? !pathToSvn.equals(svnLog.pathToSvn) : svnLog.pathToSvn != null) return false;
		if (repositoryUrl != null ? !repositoryUrl.equals(svnLog.repositoryUrl) : svnLog.repositoryUrl != null)
			return false;
		if (repositoryRoot != null ? !repositoryRoot.equals(svnLog.repositoryRoot) : svnLog.repositoryRoot != null)
			return false;
		if (timeRange != null ? !timeRange.equals(svnLog.timeRange) : svnLog.timeRange != null) return false;
		return commandLine != null ? commandLine.equals(svnLog.commandLine) : svnLog.commandLine == null;

	}

	@Override public int hashCode() {
		int result = pathToSvn != null ? pathToSvn.hashCode() : 0;
		result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
		result = 31 * result + (repositoryRoot != null ? repositoryRoot.hashCode() : 0);
		result = 31 * result + (timeRange != null ? timeRange.hashCode() : 0);
		result = 31 * result + (useMergeHistory ? 1 : 0);
		result = 31 * result + (quoteDateRange ? 1 : 0);
		result = 31 * result + (commandLine != null ? commandLine.hashCode() : 0);
		return result;
	}
}
