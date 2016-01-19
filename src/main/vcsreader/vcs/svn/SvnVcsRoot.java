package vcsreader.vcs.svn;

import vcsreader.VcsRoot;
import vcsreader.vcs.commandlistener.VcsCommandObserver;

import java.util.Date;

import static vcsreader.VcsProject.*;

public class SvnVcsRoot implements VcsRoot, VcsRoot.WithCommandObserver {
    public final String repositoryUrl;
    public final SvnSettings settings;
    private String repositoryRoot;
	private boolean quoteDateRange = false;
    private VcsCommandObserver observer;

    public SvnVcsRoot(String repositoryUrl, SvnSettings settings) {
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public CloneResult cloneToLocal() {
        return new CloneResult();
    }

    @Override public UpdateResult update() {
        return new UpdateResult();
    }

    @Override public LogResult log(Date fromDate, Date toDate) {
        if (repositoryRoot == null) {
            SvnInfo.Result result = observer.executeAndObserve(new SvnInfo(settings.svnPath, repositoryUrl));
            repositoryRoot = result.repositoryRoot;
        }
	    LogResult logResult = observer.executeAndObserve(svnLog(fromDate, toDate));
	    if (hasRevisionArgumentError(logResult)) {
		    quoteDateRange = !quoteDateRange;
		    logResult = observer.executeAndObserve(svnLog(fromDate, toDate));
	    }
	    return logResult;
    }

	private SvnLog svnLog(Date fromDate, Date toDate) {
		return new SvnLog(
			settings.svnPath,
			repositoryUrl,
			repositoryRoot,
			fromDate, toDate,
			settings.useMergeHistory,
			quoteDateRange
		);
	}

	/**
	 * This is workaround for error in cygwin when "{}" in date range argument
	 * are interpreted by shell and, therefore, need quoting.
	 * At the same time quoting isn't required and doesn't work when running from other shells.
	 */
	private static boolean hasRevisionArgumentError(LogResult logResult) {
		if (logResult.isSuccessful()) return false;
		for (String error : logResult.vcsErrors()) {
			if (error.contains("E205000: Syntax error in revision argument")) {
				return true;
			}
		}
		return false;
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
        return observer.executeAndObserve(new SvnLogFileContent(
                settings.svnPath,
                repositoryUrl,
                filePath,
                revision,
                settings.filesCharset
        ));
    }

    @Override public void setObserver(VcsCommandObserver observer) {
        this.observer = observer;
    }

    @Override public String toString() {
        return "SvnVcsRoot{" +
                "settings=" + settings +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                '}';
    }
}
