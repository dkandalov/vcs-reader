package vcsreader.vcs;

import vcsreader.Change;
import vcsreader.CommandExecutor;
import vcsreader.Commit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static vcsreader.lang.StringUtil.split;

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
            return new SuccessfulResult(parseAsCommits(shellCommand.stdout()));
        } else {
            return new FailedResult(shellCommand.stderr());
        }
    }

    private static List<Commit> parseAsCommits(String stdout) {
        System.out.println(stdout); // TODO remove
        List<Commit> commits = new ArrayList<Commit>();

        String logStart = "\u0011\u0012\u0013\n";
        String logSeparator = "\u0010\u0011\u0012\n";

        List<String> commitsAsString = split(stdout, logStart);
        for (String s : commitsAsString) {
            List<String> values = split(s, logSeparator);
            commits.add(new Commit(
                    values.get(0),
                    parseDate(values.get(1)),
                    values.get(2),
                    values.get(3),
                    new ArrayList<Change>()
            ));
        }
        return commits;
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

    public static class SuccessfulResult extends CommandExecutor.Result {
        public final List<Commit> commits;

        public SuccessfulResult(List<Commit> commits) {
            super(true);
            this.commits = commits;
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
