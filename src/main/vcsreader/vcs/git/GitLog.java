package vcsreader.vcs.git;

import vcsreader.Change;
import vcsreader.Commit;
import vcsreader.lang.VcsCommand;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static vcsreader.Change.Type.DELETED;
import static vcsreader.Change.Type.NEW;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.vcs.git.CommitParser.parseListOfChanges;
import static vcsreader.vcs.git.CommitParser.parseListOfCommits;
import static vcsreader.vcs.git.GitShellCommand.isSuccessful;

class GitLog implements VcsCommand<LogResult> {
    private final String gitPath;
    private final String folder;
    private final Date fromDate;
    private final Date toDate;

    private final ShellCommand shellCommand;
    private final List<ShellCommand> shellSubCommands = new ArrayList<ShellCommand>();


    public GitLog(String gitPath, String folder, Date fromDate, Date toDate) {
        this.gitPath = gitPath;
        this.folder = folder;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.shellCommand = gitLog(gitPath, folder, fromDate, toDate);
    }

    @Override public LogResult execute() {
        shellCommand.execute();

        if (isSuccessful(shellCommand)) {
            List<Commit> commits = parseListOfCommits(shellCommand.stdout());
            commits = handleFileRenamesIn(commits);

            List<String> errors = (shellCommand.stderr().trim().isEmpty() ? new ArrayList<String>() : asList(shellCommand.stderr()));
            return new LogResult(commits, errors);
        } else {
            return new LogResult(new ArrayList<Commit>(), asList(shellCommand.stderr()));
        }
    }

    static ShellCommand gitLog(String gitPath, String folder, Date fromDate, Date toDate) {
        String from = "--after=" + Long.toString(fromDate.getTime() / 1000);
        String to = "--before=" + Long.toString(toDate.getTime() / 1000);
        String showFileStatus = "--name-status"; // see --diff-filter at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        return new ShellCommand(gitPath, "log", logFormat(), from, to, showFileStatus, "--encoding=UTF-8").workingDir(folder);
    }

    static ShellCommand gitLogRenames(String gitPath, String folder, String revision) {
        // based on git4idea.history.GitHistoryUtils#getFirstCommitRenamePath
        return new ShellCommand(gitPath, "show", "-M", "--pretty=format:", "--name-status", revision).workingDir(folder);
    }

    private List<Commit> handleFileRenamesIn(List<Commit> commits) {
        List<Commit> result = new ArrayList<Commit>();
        for (Commit commit : commits) {
            if (hasPotentialRenames(commit)) {
                ShellCommand shellCommand = gitLogRenames(gitPath, folder, commit.revision);
                shellSubCommands.add(shellCommand);
                shellCommand.execute();

                if (isSuccessful(shellCommand)) {
                    List<Change> updatedChanges = parseListOfChanges(shellCommand.stdout(), commit.revision, commit.revisionBefore);
                    commit = new Commit(commit.revision, commit.revisionBefore, commit.commitDate, commit.authorName, commit.comment, updatedChanges);
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
            if (change.type == DELETED) hasDeletions = true;
            else if (change.type == NEW) hasAdditions = true;
        }
        return hasDeletions && hasAdditions;
    }

    private static String logFormat() {
        // see "PRETTY FORMATS" at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        String commitHash = "%H";
        String parentHashes = "%P";
        String commitDate = "%ct";
        String authorName = "%an"; // see http://stackoverflow.com/questions/18750808/difference-between-author-and-committer-in-git
        String rawBody = "%s%n%n%-b"; // based on git4idea.checkin.GitCheckinEnvironment.GitCheckinOptions.getLastCommitMessage()

        String commitStartSeparator = "%x11%x12%x13%n";
        String fieldSeparator = "%x10%x11%x12%n";

        return "--pretty=format:" +
                commitStartSeparator +
                commitHash + fieldSeparator +
                parentHashes + fieldSeparator +
                commitDate + fieldSeparator +
                authorName + fieldSeparator +
                rawBody + fieldSeparator;
    }

    @Override public String describe() {
        String result = shellCommand.describe();
        for (ShellCommand shellSubCommand : shellSubCommands) {
            result += "\n" + shellSubCommand.describe();
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