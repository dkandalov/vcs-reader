package vcsreader.vcs.git;

import vcsreader.VcsRoot;
import vcsreader.vcs.common.VcsCommandExecutor;

import java.util.Date;

import static vcsreader.VcsProject.*;

public class GitVcsRoot implements VcsRoot, VcsRoot.WithExecutor {
    public final String vcsRootPath;
    public final String repositoryUrl;
    public final GitSettings settings;
    private VcsCommandExecutor executor;

    public GitVcsRoot(String vcsRootPath, String repositoryUrl) {
        this(vcsRootPath, repositoryUrl, GitSettings.defaults());
    }

    public GitVcsRoot(String vcsRootPath, String repositoryUrl, GitSettings settings) {
        this.vcsRootPath = vcsRootPath;
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public CloneResult cloneToLocal() {
        return executor.execute(new GitClone(settings.gitPath, repositoryUrl, vcsRootPath));
    }

    @Override public UpdateResult update() {
        return executor.execute(new GitUpdate(settings.gitPath, vcsRootPath));
    }

    @Override public LogResult log(Date fromDate, Date toDate) {
        return executor.execute(new GitLog(settings.gitPath, vcsRootPath, fromDate, toDate));
    }

    @Override public LogContentResult contentOf(String filePath, String revision) {
        return executor.execute(new GitLogFileContent(settings.gitPath, vcsRootPath, filePath, revision, settings.filesCharset));
    }

    @Override public void setExecutor(VcsCommandExecutor commandExecutor) {
        this.executor = commandExecutor;
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
