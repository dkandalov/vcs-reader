package vcsreader.vcs;

import vcsreader.vcs.ShellCommand;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GitCommands {
    public static ShellCommand gitLogFile(String folder, String filePath, String revision) {
        return new ShellCommand("/usr/bin/git", "show", revision + ":" + filePath).execute(new File(folder));
    }

    public static ShellCommand gitLog(String folder, Date fromDate, Date toDate) {
        String showFileStatus = "--name-status"; // see --diff-filter at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        return new ShellCommand("/usr/bin/git", "log", logFormat(), dateRange(fromDate, toDate), showFileStatus).execute(new File(folder));
    }

    private static String dateRange(Date fromDate, Date toDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String from = dateFormat.format(fromDate);
        String to = dateFormat.format(toDate);
        return "--after={" + from + "} --before={ " + to + "}";
    }

    private static String logFormat() {
        // see "PRETTY FORMATS" at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
        String committerDate = "%ct";
        String commitHash = "%H";
        String authorName = "%an"; // see http://stackoverflow.com/questions/18750808/difference-between-author-and-committer-in-git
        String committerName = "%cn";
        String subject = "%s";
        String body = "%b";
        String committedChangelistFormat =
                committerDate + "%n" +
                commitHash + "%n" +
                authorName + "%x20" + committerName + "%n" +
                subject + body + "%n";

        return "--pretty=format:" + committedChangelistFormat;
    }

    public static ShellCommand gitClone(String fromUrl, String toFolder) {
        return new ShellCommand("/usr/bin/git", "clone", "-v", fromUrl, toFolder).execute();
    }
}
