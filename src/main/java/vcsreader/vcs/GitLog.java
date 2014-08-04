package vcsreader.vcs;

import vcsreader.CommandExecutor;

import java.util.Date;

public class GitLog implements CommandExecutor.Command {
    private final String folder;
    private final Date fromDate;
    private final Date toDate;

    public GitLog(String folder, Date fromDate, Date toDate) {
        this.folder = folder;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override public CommandExecutor.Result execute() {
        ShellCommand shellCommand = GitShellCommands.gitLog(folder, fromDate, toDate);
        if (shellCommand.exitValue() == 0) {
            return new SuccessfulResult();
        } else {
            return new FailedResult(shellCommand.stderr());
        }
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

    public static class SuccessfulResult extends CommandExecutor.Result {
        public SuccessfulResult() {
            super(true);
        }
    }

    public static class FailedResult extends CommandExecutor.Result {
        public final String stderr;

        public FailedResult(String stderr) {
            super(false);
            this.stderr = stderr;
        }
    }
}
