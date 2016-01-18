package vcsreader.vcs.git;

import vcsreader.Change;
import vcsreader.Commit;
import vcsreader.lang.ExternalCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.nio.charset.Charset.forName;
import static java.util.Arrays.asList;
import static vcsreader.Change.Type.DELETED;
import static vcsreader.Change.Type.NEW;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.vcs.git.GitCommitParser.*;
import static vcsreader.vcs.git.GitExternalCommand.isSuccessful;

/**
 * See https://git-scm.com/docs/git-log
 */
@SuppressWarnings("Duplicates") // because it's similar to HgLog
class GitLog implements VcsCommand<LogResult> {
    private final String gitPath;
    private final String folder;
    private final Date fromDate;
    private final Date toDate;

    private final ExternalCommand externalCommand;
    private final List<ExternalCommand> externalSubCommands = new ArrayList<ExternalCommand>();


    public GitLog(String gitPath, String folder, Date fromDate, Date toDate) {
        this.gitPath = gitPath;
        this.folder = folder;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.externalCommand = gitLog(gitPath, folder, fromDate, toDate);
    }

    @Override public LogResult execute() {
        externalCommand.execute();

        if (isSuccessful(externalCommand)) {
            List<Commit> commits = parseListOfCommits(externalCommand.stdout());
            commits = handleFileRenamesIn(commits);

            List<String> errors = (externalCommand.stderr().trim().isEmpty() ? new ArrayList<String>() : asList(externalCommand.stderr()));
            return new LogResult(commits, errors);
        } else {
            return new LogResult(new ArrayList<Commit>(), asList(externalCommand.stderr()));
        }
    }

    static ExternalCommand gitLog(String gitPath, String folder, Date fromDate, Date toDate) {
        String from = "--after=" + Long.toString(fromDate.getTime() / 1000);
        String to = "--before=" + Long.toString((toDate.getTime() / 1000) - 1);
        String showFileStatus = "--name-status"; // see --diff-filter at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        String forceUTF8ForCommitMessages = "--encoding=UTF-8";
        ExternalCommand command = new ExternalCommand(gitPath, "log", logFormat(), from, to, showFileStatus, forceUTF8ForCommitMessages);
        return command.workingDir(folder).outputCharset(forName("UTF-8"));
    }

    static ExternalCommand gitLogRenames(String gitPath, String folder, String revision) {
        // based on git4idea.history.GitHistoryUtils#getFirstCommitRenamePath
        return new ExternalCommand(gitPath, "show", "-M", "--pretty=format:", "--name-status", revision).workingDir(folder);
    }

    private List<Commit> handleFileRenamesIn(List<Commit> commits) {
        List<Commit> result = new ArrayList<Commit>();
        for (Commit commit : commits) {
            if (hasPotentialRenames(commit)) {
                ExternalCommand command = gitLogRenames(gitPath, folder, commit.revision);
                externalSubCommands.add(command);
                command.execute();

                if (isSuccessful(command)) {
                    List<Change> updatedChanges = parseListOfChanges(command.stdout(), commit.revision, commit.revisionBefore);
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
            if (change.type == DELETED) hasDeletions = true;
            else if (change.type == NEW) hasAdditions = true;
        }
        return hasDeletions && hasAdditions;
    }

    @Override public String describe() {
        String result = externalCommand.describe();
        for (ExternalCommand command : externalSubCommands) {
            result += "\n" + command.describe();
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
