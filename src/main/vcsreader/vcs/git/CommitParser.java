package vcsreader.vcs.git;

import vcsreader.Change;
import vcsreader.Commit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static vcsreader.Change.Type.*;
import static vcsreader.lang.StringUtil.split;
import static vcsreader.lang.StringUtil.trim;

class CommitParser {
    static List<Commit> parseListOfCommits(String stdout) {
        List<Commit> commits = new ArrayList<Commit>();

        String commitStartSeparator = "\u0015\u0016\u0017\u0018\u0019";
        String commitFieldsSeparator = "\u0019\u0018\u0017\u0016\u0015";

        List<String> commitsAsString = split(stdout, commitStartSeparator);

        for (String s : commitsAsString) {
            Commit commit = parseCommit(s, commitFieldsSeparator);
            if (commit != null) {
                commits.add(commit);
            }
        }
        return commits;
    }

    private static Commit parseCommit(String s, String commitFieldsSeparator) {
        List<String> values = split(s, commitFieldsSeparator);

        List<String> previousRevision = split(values.get(1), " ");
        boolean isFirstCommit = previousRevision.size() == 0;
        boolean isMergeCommit = previousRevision.size() > 1;
        if (isMergeCommit) return null;

        String revision = values.get(0);
        String revisionBefore = (isFirstCommit ? Change.noRevision : previousRevision.get(0));
        Date commitDate = parseDate(values.get(2));
        String authorName = values.get(3);
        String comment = trim(values.get(4), " \r\n\t");
        List<Change> changes = parseListOfChanges(values.get(5), revision, revisionBefore);

        return new Commit(revision, revisionBefore, commitDate, authorName, comment, changes);
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
        Change.Type changeType = parseChangeType(values.get(0));

        boolean hasRenames = values.size() > 2;
        String filePath = unescapeQuotes(hasRenames ? values.get(2) : values.get(1));
        String filePathBefore = unescapeQuotes(hasRenames ? values.get(1) : filePath);

        if (changeType == NEW) {
            filePathBefore = Change.noFilePath;
            revisionBefore = Change.noRevision;
        } else if (changeType == DELETED) {
            filePathBefore = filePath;
            filePath = Change.noFilePath;
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

    private static Change.Type parseChangeType(String s) {
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
            return NEW;
        } else if (c == modified || c == typeChanged || c == unmerged || c == unknown) {
            return MODIFICATION;
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
}
