package vcsreader.vcs.git;

import vcsreader.vcs.Change;
import vcsreader.vcs.Commit;
import vcsreader.VcsChange;
import vcsreader.VcsCommit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static vcsreader.VcsChange.Type.*;
import static vcsreader.lang.StringUtil.split;
import static vcsreader.lang.StringUtil.trim;

class GitCommitParser {
	private static final String commitStartSeparatorFormat = "%x15%x16%x17%x18%x19";
	private static final String commitFieldSeparatorFormat = "%x19%x18%x17%x16%x15";
	private static final String commitStartSeparator = "\u0015\u0016\u0017\u0018\u0019";
	private static final String commitFieldsSeparator = "\u0019\u0018\u0017\u0016\u0015";

	public static List<VcsCommit> parseListOfCommits(String stdout) {
		List<VcsCommit> commits = new ArrayList<VcsCommit>();
		List<String> commitsAsString = split(stdout, commitStartSeparator);

		for (String s : commitsAsString) {
			VcsCommit commit = parseCommit(s, commitFieldsSeparator);
			if (commit != null) {
				commits.add(commit);
			}
		}
		return commits;
	}

	private static VcsCommit parseCommit(String s, String fieldsSeparator) {
		List<String> values = split(s, fieldsSeparator);

		List<String> previousRevision = split(values.get(1), " ");
		boolean isFirstCommit = previousRevision.size() == 0;
		boolean isMergeCommit = previousRevision.size() > 1;
		if (isMergeCommit) return null;

		String revision = values.get(0);
		String revisionBefore = (isFirstCommit ? VcsChange.noRevision : previousRevision.get(0));
		Date commitDate = parseDate(values.get(2));
		String author = values.get(3);
		String message = trim(values.get(4), " \r\n\t");

		boolean hasNoChanges = values.size() < 6; // e.g. for commits with --allow-empty flag
		List<Change> changes = hasNoChanges ?
				Collections.<Change>emptyList() :
				parseListOfChanges(values.get(5), revision, revisionBefore);

		return new Commit(revision, revisionBefore, commitDate, author, message, changes);
	}

	static List<Change> parseListOfChanges(String changesAsString, String revision, String revisionBefore) {
		List<Change> changes = new ArrayList<Change>();

		for (String s : split(changesAsString, "\n")) {
			Change change = parseChange(s, revision, revisionBefore);
			if (change != null) {
				changes.add(change);
			}
		}

		return changes;
	}

	private static Change parseChange(String s, String revision, String revisionBefore) {
		if (s.trim().isEmpty()) return null;

		List<String> values = split(s, "\t");
		VcsChange.Type changeType = parseChangeType(values.get(0));

		boolean hasRenames = values.size() > 2;
		String filePath = unescapeQuotes(hasRenames ? values.get(2) : values.get(1));
		String filePathBefore = unescapeQuotes(hasRenames ? values.get(1) : filePath);

		if (changeType == ADDED) {
			filePathBefore = VcsChange.noFilePath;
			revisionBefore = VcsChange.noRevision;
		} else if (changeType == DELETED) {
			filePathBefore = filePath;
			filePath = VcsChange.noFilePath;
		}

		return new Change(changeType, filePath, filePathBefore, revision, revisionBefore);
	}

	/**
	 * See git4idea.GitUtil#unescapePath(java.lang.String) for more complete implementation.
	 */
	private static String unescapeQuotes(String filePath) {
		String quote = "\"";
		if (!filePath.startsWith(quote)) return filePath;
		return filePath.substring(1, filePath.length() - 1).replace("\\\"", "\"");
	}

	private static VcsChange.Type parseChangeType(String s) {
		// see "--diff-filter" at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
		char added = 'A';
		char copied = 'C';
		char modified = 'M';
		char typeChanged = 'T';
		char unmerged = 'U';
		char unknown = 'X';
		char deleted = 'D';
		char renamed = 'R';

		char c = s.charAt(0);
		if (c == added || c == copied) {
			return ADDED;
		} else if (c == modified || c == typeChanged || c == unmerged || c == unknown) {
			return MODIFIED;
		} else if (c == deleted) {
			return DELETED;
		} else if (c == renamed) {
			return MOVED;
		} else {
			throw new IllegalStateException("Unknown git change type: " + s);
		}
	}

	private static Date parseDate(String s) {
		return new Date(Long.parseLong(s) * 1000);
	}

	public static String logFormat() {
		// see "PRETTY FORMATS" at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
		String commitHash = "%H";
		String parentHashes = "%P";
		String authorDate = "%at";
		String authorName = "%an"; // see http://stackoverflow.com/questions/18750808/difference-between-author-and-committer-in-git
		String rawBody = "%s%n%n%-b"; // based on git4idea.checkin.GitCheckinEnvironment.GitCheckinOptions.getLastCommitMessage()

		return "--pretty=format:" +
				commitStartSeparatorFormat +
				commitHash + commitFieldSeparatorFormat +
				parentHashes + commitFieldSeparatorFormat +
				authorDate + commitFieldSeparatorFormat +
				authorName + commitFieldSeparatorFormat +
				rawBody + commitFieldSeparatorFormat;
	}
}
