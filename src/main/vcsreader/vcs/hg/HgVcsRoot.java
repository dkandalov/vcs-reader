package vcsreader.vcs.hg;

import vcsreader.VcsProject;
import vcsreader.VcsRoot;
import vcsreader.vcs.commandlistener.VcsCommandObserver;

import java.util.Date;

public class HgVcsRoot implements VcsRoot, VcsRoot.WithCommandObserver {
    public final String vcsRootPath;
    public final String repositoryUrl;
    public final HgSettings settings;
    private VcsCommandObserver observer;

    public HgVcsRoot(String vcsRootPath, String repositoryUrl, HgSettings settings) {
        this.vcsRootPath = vcsRootPath;
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public VcsProject.CloneResult cloneToLocal() {
        return observer.executeAndObserve(new HgClone(settings.hgPath, repositoryUrl, vcsRootPath));
    }

    @Override public VcsProject.UpdateResult update() {
        return observer.executeAndObserve(new HgUpdate(settings.hgPath, vcsRootPath));
    }

    @Override public VcsProject.LogResult log(Date fromDate, Date toDate) {
        return observer.executeAndObserve(new HgLog(settings.hgPath, vcsRootPath, fromDate, toDate));
    }

    @Override public VcsProject.LogFileContentResult logFileContent(String filePath, String revision) {
        return observer.executeAndObserve(new HgLogFileContent(settings.hgPath, vcsRootPath, filePath, revision, settings.filesCharset));
    }

    @Override public void setObserver(VcsCommandObserver observer) {
        this.observer = observer;
    }
}
