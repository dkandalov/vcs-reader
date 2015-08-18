package vcsreader.vcs.git;

import vcsreader.VcsRoot;
import vcsreader.vcs.commandlistener.VcsCommandObserver;

import java.util.Date;

import static vcsreader.VcsProject.*;

public class GitVcsRoot implements VcsRoot, VcsRoot.WithCommandObserver {
    public final String vcsRootPath;
    public final String repositoryUrl;
    public final GitSettings settings;
    private VcsCommandObserver observer;

    public GitVcsRoot(String vcsRootPath, String repositoryUrl) {
        this(vcsRootPath, repositoryUrl, GitSettings.defaults());
    }

    public GitVcsRoot(String vcsRootPath, String repositoryUrl, GitSettings settings) {
        this.vcsRootPath = vcsRootPath;
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public CloneResult cloneToLocal() {
        return observer.executeAndObserve(new GitClone(settings.gitPath, repositoryUrl, vcsRootPath));
    }

    @Override public UpdateResult update() {
        return observer.executeAndObserve(new GitUpdate(settings.gitPath, vcsRootPath));
    }

    @Override public LogResult log(Date fromDate, Date toDate) {
        return observer.executeAndObserve(new GitLog(settings.gitPath, vcsRootPath, fromDate, toDate));
    }

    @Override public LogContentResult contentOf(String filePath, String revision) {
        return observer.executeAndObserve(new GitLogFileContent(settings.gitPath, vcsRootPath, filePath, revision, settings.filesCharset));
    }

    @Override public void setObserver(VcsCommandObserver observer) {
        this.observer = observer;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitVcsRoot that = (GitVcsRoot) o;

        if (vcsRootPath != null ? !vcsRootPath.equals(that.vcsRootPath) : that.vcsRootPath != null)
            return false;
        if (repositoryUrl != null ? !repositoryUrl.equals(that.repositoryUrl) : that.repositoryUrl != null)
            return false;
        if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = vcsRootPath != null ? vcsRootPath.hashCode() : 0;
        result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
        result = 31 * result + (settings != null ? settings.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitVcsRoot{" +
                "settings=" + settings +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", vcsRootPath='" + vcsRootPath + '\'' +
                '}';
    }
}
