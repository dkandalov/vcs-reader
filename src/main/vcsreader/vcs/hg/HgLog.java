package vcsreader.vcs.hg;

import vcsreader.Commit;
import vcsreader.VcsProject;
import vcsreader.lang.ShellCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.nio.charset.Charset.forName;
import static java.util.Arrays.asList;
import static vcsreader.vcs.hg.CommitParser.commitFieldSeparatorFormat;
import static vcsreader.vcs.hg.CommitParser.commitStartSeparatorFormat;
import static vcsreader.vcs.hg.CommitParser.fileSeparatorFormat;

@SuppressWarnings("Duplicates") // because it's similar to GitLog
    class HgLog implements VcsCommand<VcsProject.LogResult> {
    private final String hgPath;
    private final String folder;
    private final Date fromDate;
    private final Date toDate;

    private final ShellCommand shellCommand;

    public HgLog(String hgPath, String folder, Date fromDate, Date toDate) {
        this.hgPath = hgPath;
        this.folder = folder;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.shellCommand = hgLog(hgPath, folder, fromDate, toDate);
    }

    @Override public VcsProject.LogResult execute() {
        shellCommand.execute();

        if (HgShellCommand.isSuccessful(shellCommand)) {
            List<Commit> commits = CommitParser.parseListOfCommits(shellCommand.stdout());
            List<String> errors = (shellCommand.stderr().trim().isEmpty() ? new ArrayList<String>() : asList(shellCommand.stderr()));
            return new VcsProject.LogResult(commits, errors);
        } else {
            return new VcsProject.LogResult(new ArrayList<Commit>(), asList(shellCommand.stderr()));
        }
    }

    @Override public String describe() {
        return shellCommand.describe();
    }

    static ShellCommand hgLog(String hgPath, String folder, Date fromDate, Date toDate) {
        ShellCommand command = new ShellCommand(
                hgPath, "log",
                "--encoding", "UTF-8",
                "-r", "date(\"" + asHgDate(fromDate) + " to " + asHgDate(toDate) + "\")",
                "--template", logTemplate()
        );
        return command.workingDir(folder).withCharset(forName("UTF-8"));
    }

    private static String asHgDate(Date date) {
        return Long.toString(date.getTime() / 1000) + " 0";
    }

    private static String logTemplate() {
        // see https://www.selenic.com/mercurial/hg.1.html#templates
        String commitNode = "{node}";
        String commitParentNode = "{p1node}";
        String commitDate = "{date}";
        String author = "{person(author)}"; // use person() because author can also include email
        String description = "{desc}";
        String filesAdded = "{join(file_adds,'" + fileSeparatorFormat + "')}";
        String filesDeleted = "{join(file_dels,'" + fileSeparatorFormat + "')}";
        String filesCopied = "{join(file_copies,'" + fileSeparatorFormat + "')}";
        String filesModified = "{join(file_mods,'" + fileSeparatorFormat + "')}";

        return "" + commitStartSeparatorFormat +
                commitNode + commitFieldSeparatorFormat +
                commitParentNode + commitFieldSeparatorFormat +
                commitDate + commitFieldSeparatorFormat +
                author + commitFieldSeparatorFormat +
                description + commitFieldSeparatorFormat +
                filesAdded + commitFieldSeparatorFormat +
                filesDeleted + commitFieldSeparatorFormat +
                filesCopied + commitFieldSeparatorFormat +
                filesModified + commitFieldSeparatorFormat +
                "";
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HgLog hgLog = (HgLog) o;

        if (hgPath != null ? !hgPath.equals(hgLog.hgPath) : hgLog.hgPath != null) return false;
        if (folder != null ? !folder.equals(hgLog.folder) : hgLog.folder != null) return false;
        if (fromDate != null ? !fromDate.equals(hgLog.fromDate) : hgLog.fromDate != null) return false;
        return !(toDate != null ? !toDate.equals(hgLog.toDate) : hgLog.toDate != null);
    }

    @Override public int hashCode() {
        int result = hgPath != null ? hgPath.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        result = 31 * result + (fromDate != null ? fromDate.hashCode() : 0);
        result = 31 * result + (toDate != null ? toDate.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "HgLog{" +
                "hgPath='" + hgPath + '\'' +
                ", folder='" + folder + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
