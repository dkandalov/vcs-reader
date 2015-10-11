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
        return null;
    }

    @Override public VcsProject.LogResult log(Date fromDate, Date toDate) {
        return null;
    }

    @Override public VcsProject.LogContentResult contentOf(String filePath, String revision) {
        return null;
    }

    @Override public void setObserver(VcsCommandObserver observer) {
        this.observer = observer;
    }
}
