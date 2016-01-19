package vcsreader.vcs.hg;

import vcsreader.Commit;
import vcsreader.VcsProject;
import vcsreader.lang.ExternalCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static vcsreader.lang.Charsets.UTF8;
import static vcsreader.vcs.hg.HgExternalCommand.isSuccessful;

@SuppressWarnings("Duplicates") // because it's similar to GitLog
class HgLog implements VcsCommand<VcsProject.LogResult> {
    private final String hgPath;
    private final String folder;
    private final Date fromDate;
    private final Date toDate;

    private final ExternalCommand externalCommand;

    public HgLog(String hgPath, String folder, Date fromDate, Date toDate) {
        this.hgPath = hgPath;
        this.folder = folder;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.externalCommand = hgLog(hgPath, folder, fromDate, toDate);
    }

    @Override public VcsProject.LogResult execute() {
        externalCommand.execute();

        if (isSuccessful(externalCommand)) {
            List<Commit> commits = HgCommitParser.parseListOfCommits(externalCommand.stdout());
            List<String> errors = (externalCommand.stderr().trim().isEmpty() ? new ArrayList<String>() : asList(externalCommand.stderr()));
            return new VcsProject.LogResult(commits, errors);
        } else {
            return new VcsProject.LogResult(new ArrayList<Commit>(), asList(externalCommand.stderr() + externalCommand.exceptionStacktrace()));
        }
    }

    @Override public String describe() {
        return externalCommand.describe();
    }

    static ExternalCommand hgLog(String hgPath, String folder, Date fromDate, Date toDate) {
        ExternalCommand command = new ExternalCommand(
                hgPath, "log",
                "--encoding", UTF8.name(),
                "-r", "date(\"" + asHgDate(fromDate) + " to " + asHgDate(toDate) + "\")",
                "--template", HgCommitParser.logTemplate()
        );
        return command.workingDir(folder).outputCharset(UTF8);
    }

    private static String asHgDate(Date date) {
        // see 'hg help dates'
        String secondsSinceEpoch = Long.toString((date.getTime() / 1000) - 1);
        String utc = "0";
        return secondsSinceEpoch + " " + utc;
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
