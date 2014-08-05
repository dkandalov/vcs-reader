package vcsreader.vcs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GitShellCommands {
    public static ShellCommand gitLogFile(String folder, String filePath, String revision) {
        return new ShellCommand("/usr/bin/git", "show", revision + ":" + filePath).execute(new File(folder));
    }

    public static ShellCommand gitLog(String folder, Date fromDate, Date toDate) {
        String gitPath = "/usr/bin/git";
        String dateRange = dateRange(fromDate, toDate);
        String showFileStatus = "--name-status"; // see --diff-filter at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        ShellCommand shellCommand = new ShellCommand(gitPath, "log", logFormat(), dateRange, showFileStatus);
        return shellCommand.execute(new File(folder));
    }

    private static String dateRange(Date fromDate, Date toDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String from = dateFormat.format(fromDate);
        String to = dateFormat.format(toDate);
        return "--after={" + from + "} --before={ " + to + "}";
    }

    private static String logFormat() {
        // see "PRETTY FORMATS" at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        String commitHash = "%H";
        String commitDate = "%ct";
        String authorName = "%an"; // see http://stackoverflow.com/questions/18750808/difference-between-author-and-committer-in-git
        String subject = "%s";
        String body = "%b";

        String logStart = "%x11%x12%x13%n";
        String logSeparator = "%x10%x11%x12%n";

        return "--pretty=format:" +
                logStart +
                commitHash + logSeparator +
                commitDate + logSeparator +
                authorName + logSeparator +
                subject + logSeparator +
                body;
    }

}
