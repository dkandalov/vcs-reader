package vcsreader.vcs.svn;

import vcsreader.VcsRoot;
import vcsreader.vcs.commandlistener.VcsCommandObserver;

import java.util.Date;

import static vcsreader.VcsProject.*;

public class SvnVcsRoot implements VcsRoot, VcsRoot.WithCommandObserver {
    public final String repositoryUrl;
    public final SvnSettings settings;
    private String repositoryRoot;
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
        return observer.executeAndObserve(new SvnLog(settings.svnPath, repositoryUrl, repositoryRoot, fromDate, toDate, settings.useMergeHistory));
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
