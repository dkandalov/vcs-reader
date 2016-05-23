package org.vcsreader.vcs.svn;

import org.vcsreader.vcs.Change;
import org.vcsreader.VcsChange;
import org.vcsreader.VcsCommit;
import org.vcsreader.lang.Charsets;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.commandlistener.VcsCommand;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Arrays.asList;
import static org.vcsreader.VcsProject.LogResult;
import static org.vcsreader.vcs.svn.SvnCommandLine.isSuccessful;
import static org.vcsreader.vcs.svn.SvnCommandLine.newExternalCommand;

/**
 * See http://svnbook.red-bean.com/en/1.8/svn.ref.svn.c.log.html
 */
class SvnLog implements VcsCommand<LogResult> {
	private final String pathToSvn;
	private final String repositoryUrl;
	private final String repositoryRoot;
	private final Date fromDate;
	private final Date toDate;
	private final boolean useMergeHistory;
	private final boolean quoteDateRange;
	private final CommandLine commandLine;

	public SvnLog(String pathToSvn, String repositoryUrl, String repositoryRoot, Date fromDate, Date toDate,
	              boolean useMergeHistory, boolean quoteDateRange) {
		this.pathToSvn = pathToSvn;
		this.repositoryUrl = repositoryUrl;
		this.repositoryRoot = repositoryRoot;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.useMergeHistory = useMergeHistory;
		this.quoteDateRange = quoteDateRange;
		this.commandLine = svnLog(pathToSvn, repositoryUrl, fromDate, toDate, useMergeHistory, quoteDateRange);
	}

	@Override public LogResult execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			List<VcsCommit> allCommits = SvnCommitParser.parseCommits(commandLine.stdout());
			List<VcsCommit> commits = transformToSubPathCommits(deleteCommitsBefore(fromDate, allCommits));
			return new LogResult(commits, new ArrayList<String>());
		} else {
			return new LogResult(Collections.<VcsCommit>emptyList(), asList(commandLine.stderr() + commandLine.exceptionStacktrace()));
		}
	}

	static CommandLine svnLog(String pathToSvn, String repositoryUrl, Date fromDate, Date toDate,
	                          boolean useMergeHistory, boolean quoteDateRange) {
		// see http://svnbook.red-bean.com/en/1.8/svn.branchmerge.advanced.html
		// see http://stackoverflow.com/questions/987337/preserving-history-when-merging-subversion-branches
		String mergeHistory = (useMergeHistory ? "--use-merge-history" : "");

		// see http://subversion.tigris.org/issues/show_bug.cgi?id=2938
		Charset svnXmlCharset = Charsets.UTF8;

		return newExternalCommand(
				pathToSvn, "log",
				repositoryUrl,
				"-r", svnDateRange(fromDate, toDate, quoteDateRange),
				mergeHistory,
				"--verbose",
				"--xml"
		).outputCharset(svnXmlCharset);
	}

	private static String svnDateRange(Date fromDate, Date toDate, boolean quoteDateRange) {
		toDate = new Date(toDate.getTime() - 1000); // make toDate exclusive

		// svn supports any ISO 8601 date format (https://en.wikipedia.org/wiki/ISO_8601)
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		String result = "{" + dateFormat.format(fromDate) + "}:{" + dateFormat.format(toDate) + "}";
		if (quoteDateRange) {
			result = "'" + result + "'";
		}
		return result;
	}

	/**
	 * Delete commits because "Subversion will find the most recent revision of the repository as of the date you give".
	 * See http://svnbook.red-bean.com/en/1.8/svn.tour.revs.specifiers.html#svn.tour.revs.keywords
	 */
	private static List<VcsCommit> deleteCommitsBefore(Date date, List<VcsCommit> commits) {
		Iterator<VcsCommit> iterator = commits.iterator();
		while (iterator.hasNext()) {
			VcsCommit commit = iterator.next();
			if (commit.getTime().before(date)) iterator.remove();
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
		List<VcsCommit> result = new ArrayList<VcsCommit>();
		for (VcsCommit commit : commits) {
			List<Change> modifiedChanges = new ArrayList<Change>();
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
		List<VcsCommit> result = new ArrayList<VcsCommit>();
		for (VcsCommit commit : commits) {
			List<VcsChange> filteredChanges = new ArrayList<VcsChange>();
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

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnLog svnLog = (SvnLog) o;

		if (useMergeHistory != svnLog.useMergeHistory) return false;
		if (quoteDateRange != svnLog.quoteDateRange) return false;
		if (!fromDate.equals(svnLog.fromDate)) return false;
		if (!pathToSvn.equals(svnLog.pathToSvn)) return false;
		if (!repositoryRoot.equals(svnLog.repositoryRoot)) return false;
		if (!repositoryUrl.equals(svnLog.repositoryUrl)) return false;
		if (!toDate.equals(svnLog.toDate)) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = pathToSvn.hashCode();
		result = 31 * result + repositoryUrl.hashCode();
		result = 31 * result + repositoryRoot.hashCode();
		result = 31 * result + fromDate.hashCode();
		result = 31 * result + toDate.hashCode();
		result = 31 * result + (useMergeHistory ? 1 : 0);
		result = 31 * result + (quoteDateRange ? 1 : 0);
		return result;
	}

	@Override public String toString() {
		return "SvnLog{" +
				"pathToSvn='" + pathToSvn + '\'' +
				", repositoryUrl='" + repositoryUrl + '\'' +
				", repositoryRoot='" + repositoryRoot + '\'' +
				", fromDate=" + fromDate +
				", toDate=" + toDate +
				", useMergeHistory=" + useMergeHistory +
				", quoteDateRange=" + quoteDateRange +
				'}';
	}
}
