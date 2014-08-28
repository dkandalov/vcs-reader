package vcsreader.vcs.svn;

import vcsreader.lang.FunctionExecutor;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.text.SimpleDateFormat;
import java.util.Date;

import static vcsreader.VcsProject.LogResult;

public class SvnLog implements FunctionExecutor.Function<LogResult> {
    @Override public LogResult execute() {
        return null;
    }

    static ShellCommand svnLog(String pathToSvn, String repositoryUrl, Date fromDate, Date toDate) {
        ShellCommand shellCommand = new ShellCommand(
                pathToSvn, "log",
                repositoryUrl,
                "-r" + dateRange(fromDate, toDate),
                "--use-merge-history", "--stop-on-copy", "--verbose", "--xml"
        );
        return shellCommand.execute();
    }

    private static String dateRange(Date fromDate, Date toDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return "{" + dateFormat.format(fromDate) + "}:{" + dateFormat.format(toDate) + "}";
    }
}
