package org.vcsreader.vcs.hg;

import org.vcsreader.VcsCommit;
import org.vcsreader.lang.DateTimeUtil;
import org.vcsreader.vcs.Change;
import org.vcsreader.vcs.Commit;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.vcsreader.VcsChange.Type.*;
import static org.vcsreader.lang.DateTimeUtil.dateTimeFormatter;
import static org.vcsreader.lang.StringUtil.split;
import static org.vcsreader.vcs.Change.noFilePath;
import static org.vcsreader.vcs.Change.noRevision;

class HgCommitParser {
	private static final String commitStartSeparatorFormat = "\\x15\\x16\\x17\\x18\\x19";
	private static final String commitFieldSeparatorFormat = "\\x19\\x18\\x17\\x16\\x15";
	private static final String fileSeparatorFormat = "\\x17\\x16\\x15\\x19\\x18";
	private static final String commitStartSeparator = "\u0015\u0016\u0017\u0018\u0019";
	private static final String commitFieldsSeparator = "\u0019\u0018\u0017\u0016\u0015";
	private static final String fileSeparator = "\u0017\u0016\u0015\u0019\u0018";
	private static final String hgNoRevision = "0000000000000000000000000000000000000000";
	private static final DateTimeFormatter dateTimeFormatter = dateTimeFormatter("yyyy-MM-dd HH:mm:ss Z", ZoneOffset.UTC);

	public static List<VcsCommit> parseListOfCommits(String stdout) {
		ArrayList<VcsCommit> commits = new ArrayList<>();
		List<String> commitsAsString = split(stdout, commitStartSeparator);

		for (String s : commitsAsString) {
			commits.add(parseCommit(s, commitFieldsSeparator));
		}
		return commits;
	}

	private static VcsCommit parseCommit(String s, String fieldsSeparator) {
		List<String> values = split(s, fieldsSeparator);

		String revision = values.get(0);
		String revisionBefore = values.get(1);
		if (revisionBefore.equals(hgNoRevision)) {
			revisionBefore = noRevision;
		}
		Instant dateTime = dateTimeFormatter.parse(values.get(2), DateTimeUtil::asInstant);
		String author = values.get(3);
		String comment = values.get(4);

		List<Change> filesAdded = new ArrayList<>();
		for (String filePath : values.get(5).split(fileSeparator)) {
			if (filePath.isEmpty()) continue;
			filesAdded.add(new Change(ADDED, filePath, revision));
		}
		List<Change> filesDeleted = new ArrayList<>();
		for (String filePath : values.get(6).split(fileSeparator)) {
			if (filePath.isEmpty()) continue;
			filesDeleted.add(new Change(DELETED, noFilePath, filePath, revision, revisionBefore));
		}
		List<Change> filesMoved = new ArrayList<>();
		for (String newAndOldFilePath : values.get(7).split(fileSeparator)) {
			if (newAndOldFilePath.isEmpty()) continue;
			String[] parts = newAndOldFilePath.split("\\s+\\(");
			String filePath;
			String filePathBefore;
			if (parts.length == 2) {
				filePath = parts[0];
				filePathBefore = parts[1].substring(0, parts[1].length() - 1);
			} else {
				filePath = newAndOldFilePath;
				filePathBefore = noFilePath;
			}
			filesMoved.add(new Change(MOVED, filePath, filePathBefore, revision, revisionBefore));

			Iterator<Change> addedIterator = filesAdded.iterator();
			while (addedIterator.hasNext()) {
				Change change = addedIterator.next();
				if (change.getFilePath().equals(filePath)) {
					addedIterator.remove();
					break;
				}
			}
			Iterator<Change> deletedIterator = filesDeleted.iterator();
			while (deletedIterator.hasNext()) {
				Change change = deletedIterator.next();
				if (change.getFilePathBefore().equals(filePathBefore)) {
					deletedIterator.remove();
					break;
				}
			}
		}
		List<Change> filesModified = new ArrayList<>();
		for (String filePath : values.get(8).split(fileSeparator)) {
			if (filePath.isEmpty()) continue;
			filesModified.add(new Change(MODIFIED, filePath, filePath, revision, revisionBefore));
		}

		List<Change> changes = new ArrayList<>();
		changes.addAll(filesAdded);
		changes.addAll(filesDeleted);
		changes.addAll(filesMoved);
		changes.addAll(filesModified);

		return new Commit(revision, revisionBefore, dateTime, author, comment, changes);
	}

	public static String logTemplate() {
		// see https://www.selenic.com/mercurial/hg.1.html#templates
		String commitNode = "{node}";
		String commitParentNode = "{p1node}";
		String commitDate = "{date|isodatesec}";
		String author = "{person(author)}"; // use person() because author can also include email
		String description = "{desc}";
		String filesAdded = "{join(file_adds,'" + fileSeparatorFormat + "')}";
		String filesDeleted = "{join(file_dels,'" + fileSeparatorFormat + "')}";
		String filesCopied = "{join(file_copies,'" + fileSeparatorFormat + "')}";
		String filesModified = "{join(file_mods,'" + fileSeparatorFormat + "')}";

		return "" + commitStartSeparatorFormat +
				commitNode + commitFieldSeparatorFormat +
				commitParentNode + commitFieldSeparatorFormat +
				commitDate + commitFieldSeparatorFormat +
				author + commitFieldSeparatorFormat +
				description + commitFieldSeparatorFormat +
				filesAdded + commitFieldSeparatorFormat +
				filesDeleted + commitFieldSeparatorFormat +
				filesCopied + commitFieldSeparatorFormat +
				filesModified + commitFieldSeparatorFormat +
				"";
	}
}
