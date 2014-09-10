package vcsreader.vcs.git;

import vcsreader.VcsRoot;
import vcsreader.lang.VcsCommandExecutor;

import java.util.Date;

import static vcsreader.VcsProject.*;

public class GitVcsRoot implements VcsRoot, VcsRoot.WithExecutor {
    private final String pathToProject;
    private final String repositoryUrl;
    private final GitSettings settings;
    private transient VcsCommandExecutor commandExecutor;

    public GitVcsRoot(String pathToProject, String repositoryUrl, GitSettings settings) {
        this.pathToProject = pathToProject;
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public InitResult init() {
        return commandExecutor.execute(new GitClone(settings.gitPath, repositoryUrl, pathToProject));
    }

    @Override public UpdateResult update() {
        return commandExecutor.execute(new GitUpdate(settings.gitPath, pathToProject));
    }

    @Override public LogResult log(Date fromDate, Date toDate) {
        return commandExecutor.execute(new GitLog(settings.gitPath, pathToProject, fromDate, toDate));
    }

    @Override public LogContentResult contentOf(String filePath, String revision) {
        return commandExecutor.execute(new GitLogFileContent(settings.gitPath, pathToProject, filePath, revision, settings.filesCharset));
    }

    @Override public void setExecutor(VcsCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitVcsRoot that = (GitVcsRoot) o;

        if (pathToProject != null ? !pathToProject.equals(that.pathToProject) : that.pathToProject != null)
            return false;
        if (repositoryUrl != null ? !repositoryUrl.equals(that.repositoryUrl) : that.repositoryUrl != null)
            return false;
        if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = pathToProject != null ? pathToProject.hashCode() : 0;
        result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
        result = 31 * result + (settings != null ? settings.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitVcsRoot{" +
                "settings=" + settings +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", pathToProject='" + pathToProject + '\'' +
                '}';
    }
}
