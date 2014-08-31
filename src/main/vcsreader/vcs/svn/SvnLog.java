package vcsreader.vcs.svn;

import vcsreader.Commit;
import vcsreader.lang.FunctionExecutor;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static vcsreader.VcsProject.LogResult;

class SvnLog implements FunctionExecutor.Function<LogResult> {
    private final String pathToSvn;
    private final String repositoryUrl;
    private final Date fromDate;
    private final Date toDate;

    public SvnLog(String pathToSvn, String repositoryUrl, Date fromDate, Date toDate) {
        this.pathToSvn = pathToSvn;
        this.repositoryUrl = repositoryUrl;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override public LogResult execute() {
        ShellCommand command = svnLog(pathToSvn, repositoryUrl, fromDate, toDate);

        List<Commit> commits = CommitParser.parseCommits(command.stdout());
        List<String> errors = command.stderr().trim().isEmpty() ? Collections.<String>emptyList() : asList(command.stderr());

        return new LogResult(commits, errors);
    }

    static ShellCommand svnLog(String pathToSvn, String repositoryUrl, Date fromDate, Date toDate) {
        ShellCommand shellCommand = new ShellCommand(
                pathToSvn, "log",
                repositoryUrl,
                "-r", dateRange(fromDate, toDate),
                "--use-merge-history", "--verbose", "--xml"
        );
        return shellCommand.execute();
    }

    private static String dateRange(Date fromDate, Date toDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return "{" + dateFormat.format(fromDate) + "}:{" + dateFormat.format(toDate) + "}";
    }

}
