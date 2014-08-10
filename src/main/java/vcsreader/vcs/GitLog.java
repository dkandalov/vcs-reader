package vcsreader.vcs;

import vcsreader.Change;
import vcsreader.CommandExecutor;
import vcsreader.Commit;
import vcsreader.VcsProject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static vcsreader.lang.StringUtil.split;

class GitLog implements CommandExecutor.Command {
    private final String folder;
    private final Date fromDate;
    private final Date toDate;
    private final String gitPath;

    public GitLog(String gitPath, String folder, Date fromDate, Date toDate) {
        this.folder = folder;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.gitPath = gitPath;
    }

    @Override public CommandExecutor.Result execute() {
        ShellCommand shellCommand = gitLog(gitPath, folder, fromDate, toDate);
        List<Commit> commits = parseListOfCommits(shellCommand.stdout());
        List<String> errors = (shellCommand.stderr().trim().isEmpty() ? new ArrayList<String>() : asList(shellCommand.stderr()));
        return new VcsProject.LogResult(commits, errors);
    }

    static ShellCommand gitLog(String gitPath, String folder, Date fromDate, Date toDate) {
        String from = "--after=" + Long.toString(fromDate.getTime() / 1000);
        String to = "--before=" + Long.toString(toDate.getTime() / 1000);
        String showFileStatus = "--name-status"; // see --diff-filter at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        ShellCommand shellCommand = new ShellCommand(gitPath, "log", logFormat(), from, to, showFileStatus);
        return shellCommand.execute(new File(folder));
    }

    private static String logFormat() {
        // see "PRETTY FORMATS" at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        String commitHash = "%H";
        String parentHashes = "%P";
        String commitDate = "%ct";
        String authorName = "%an"; // see http://stackoverflow.com/questions/18750808/difference-between-author-and-committer-in-git
        String subject = "%s";

        String commitStartSeparator = "%x11%x12%x13%n";
        String fieldSeparator = "%x10%x11%x12%n";

        return "--pretty=format:" +
                commitStartSeparator +
                commitHash + fieldSeparator +
                parentHashes + fieldSeparator +
                commitDate + fieldSeparator +
                authorName + fieldSeparator +
                subject + fieldSeparator;
    }

    private static List<Commit> parseListOfCommits(String stdout) {
        List<Commit> commits = new ArrayList<Commit>();

        String commitStartSeparator = "\u0011\u0012\u0013\n";
        String commitFieldsSeparator = "\u0010\u0011\u0012\n";

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
        String comment = values.get(4);
        List<Change> changes = parseListOfChanges(values.get(5), revision, revisionBefore);

        return new Commit(revision, commitDate, authorName, comment, changes);
    }

    private static List<Change> parseListOfChanges(String changesAsString, String revision, String revisionBefore) {
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
        String fileName = values.get(1);
        return new Change(changeType, fileName, revision, revisionBefore);
    }

    private static Change.Type parseChangeType(String s) {
        if (s.length() != 1)
            throw new IllegalArgumentException("Git change type expected to be a char but was: '" + s + "'");

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
            return Change.Type.NEW;
        } else if (c == modified || c == typeChanged || c == unmerged || c == unknown) {
            return Change.Type.MODIFICATION;
        } else if (c == deleted) {
            return Change.Type.DELETED;
        } else if (c == renamed) {
            return Change.Type.MOVED;
        } else {
            throw new IllegalStateException("Unknown git change type: " + s);
        }
    }

    private static Date parseDate(String s) {
        return new Date(Long.parseLong(s) * 1000);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitLog gitLog = (GitLog) o;

        if (folder != null ? !folder.equals(gitLog.folder) : gitLog.folder != null) return false;
        if (fromDate != null ? !fromDate.equals(gitLog.fromDate) : gitLog.fromDate != null) return false;
        if (toDate != null ? !toDate.equals(gitLog.toDate) : gitLog.toDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = folder != null ? folder.hashCode() : 0;
        result = 31 * result + (fromDate != null ? fromDate.hashCode() : 0);
        result = 31 * result + (toDate != null ? toDate.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitLog{" +
                "folder='" + folder + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
