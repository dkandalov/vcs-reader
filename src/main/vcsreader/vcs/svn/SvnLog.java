package vcsreader.vcs.svn;

import vcsreader.Change;
import vcsreader.Commit;
import vcsreader.lang.ShellCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.charset.Charset.forName;
import static java.util.Arrays.asList;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.vcs.svn.SvnShellCommand.createShellCommand;
import static vcsreader.vcs.svn.SvnShellCommand.isSuccessful;

/**
 * See http://svnbook.red-bean.com/en/1.8/svn.ref.svn.c.log.html
 */
class SvnLog implements VcsCommand<LogResult> {
    private final String pathToSvn;
    private final String repositoryUrl;
    private final String repositoryRoot;
    private final Date fromDate;
    private final Date toDate;
    private final boolean useMergeHistory;
    private final ShellCommand shellCommand;

    public SvnLog(String pathToSvn, String repositoryUrl, String repositoryRoot, Date fromDate, Date toDate, boolean useMergeHistory) {
        this.pathToSvn = pathToSvn;
        this.repositoryUrl = repositoryUrl;
        this.repositoryRoot = repositoryRoot;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.useMergeHistory = useMergeHistory;
        this.shellCommand = svnLog(pathToSvn, repositoryUrl, fromDate, toDate, useMergeHistory);
    }

    @Override public LogResult execute() {
        shellCommand.execute();

        if (isSuccessful(shellCommand)) {
            List<Commit> allCommits = SvnCommitParser.parseCommits(shellCommand.stdout());
            List<Commit> commits = transformToSubPathCommits(deleteCommitsBefore(fromDate, allCommits));
            return new LogResult(commits, new ArrayList<String>());
        } else {
            return new LogResult(Collections.<Commit>emptyList(), asList(shellCommand.stderr()));
        }
    }

    static ShellCommand svnLog(String pathToSvn, String repositoryUrl, Date fromDate, Date toDate, boolean useMergeHistory) {
        String mergeHistory = (useMergeHistory ? "--use-merge-history" : "");
        Charset svnXmlCharset = forName("UTF-8"); // see http://subversion.tigris.org/issues/show_bug.cgi?id=2938
	    return createShellCommand(
                pathToSvn, "log",
                repositoryUrl,
                "-r", svnDateRange(fromDate, toDate),
                mergeHistory,
                "--verbose",
                "--xml"
        ).withCharset(svnXmlCharset);
    }

    private static String svnDateRange(Date fromDate, Date toDate) {
	    toDate = new Date(toDate.getTime() - 1000); // make toDate exclusive

	    // svn supports any ISO 8601 date format (https://en.wikipedia.org/wiki/ISO_8601)
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return "{" + dateFormat.format(fromDate) + "}:{" + dateFormat.format(toDate) + "}";
    }

    /**
     * Delete commits because "Subversion will find the most recent revision of the repository as of the date you give".
     * See http://svnbook.red-bean.com/en/1.8/svn.tour.revs.specifiers.html#svn.tour.revs.keywords
     */
    private static List<Commit> deleteCommitsBefore(Date date, List<Commit> commits) {
        Iterator<Commit> iterator = commits.iterator();
        while (iterator.hasNext()) {
            Commit commit = iterator.next();
            if (commit.time.before(date)) iterator.remove();
        }
        return commits;
    }

    private List<Commit> transformToSubPathCommits(List<Commit> commits) {
        String subPath = subPathOf(repositoryUrl, repositoryRoot);
        commits = removeChangesNotIn(subPath, commits);
        commits = modifyChanges(subPath, commits);
        return commits;
    }

    private static List<Commit> modifyChanges(String subPath, List<Commit> commits) {
        List<Commit> result = new ArrayList<Commit>();
        for (Commit commit : commits) {
            List<Change> modifiedChanges = new ArrayList<Change>();
            for (Change change : commit.changes) {
                modifiedChanges.add(new Change(
                        changeTypeConsideringSubPath(subPath, change),
                        useSubPathAsRoot(subPath, change.filePath),
                        useSubPathAsRoot(subPath, change.filePathBefore),
                        change.revision,
                        change.revisionBefore
                ));
            }
            result.add(commit.withChanges(modifiedChanges));
        }
        return result;
    }

    private static List<Commit> removeChangesNotIn(String subPath, List<Commit> commits) {
        List<Commit> result = new ArrayList<Commit>();
        for (Commit commit : commits) {
            List<Change> filteredChanges = new ArrayList<Change>();
            for (Change change : commit.changes) {

                if (change.filePath.startsWith(subPath) || change.filePathBefore.startsWith(subPath)) {
                    filteredChanges.add(change);
                }

            }
            result.add(commit.withChanges(filteredChanges));
        }
        return result;
    }

    private static String subPathOf(String repositoryUrl, String repositoryRoot) {
        String subPath = repositoryUrl.replace(repositoryRoot, "");
        if (subPath.startsWith("/")) subPath = subPath.substring(1);
        if (!subPath.isEmpty() && !subPath.endsWith("/")) subPath += "/";
        return subPath;
    }

    private static Change.Type changeTypeConsideringSubPath(String subPath, Change change) {
        if (change.type != Change.Type.MOVED) return change.type;
        else if (!change.filePath.startsWith(subPath)) return Change.Type.DELETED;
        else if (!change.filePathBefore.startsWith(subPath)) return Change.Type.NEW;
        else return change.type;
    }

    private static String useSubPathAsRoot(String subPath, String filePath) {
        int i = filePath.indexOf(subPath);
        if (i != 0) return Change.noFilePath;
        else return filePath.substring(subPath.length());
    }

    @Override public String describe() {
        return shellCommand.describe();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SvnLog svnLog = (SvnLog) o;

        if (useMergeHistory != svnLog.useMergeHistory) return false;
        if (!fromDate.equals(svnLog.fromDate)) return false;
        if (!pathToSvn.equals(svnLog.pathToSvn)) return false;
        if (!repositoryRoot.equals(svnLog.repositoryRoot)) return false;
        if (!repositoryUrl.equals(svnLog.repositoryUrl)) return false;
        if (!toDate.equals(svnLog.toDate)) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = pathToSvn.hashCode();
        result = 31 * result + repositoryUrl.hashCode();
        result = 31 * result + repositoryRoot.hashCode();
        result = 31 * result + fromDate.hashCode();
        result = 31 * result + toDate.hashCode();
        result = 31 * result + (useMergeHistory ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "SvnLog{" +
                "pathToSvn='" + pathToSvn + '\'' +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", repositoryRoot='" + repositoryRoot + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", useMergeHistory=" + useMergeHistory +
                '}';
    }
}
