package vcsreader.vcs.git;

import vcsreader.VcsProject;
import vcsreader.VcsRoot;
import vcsreader.lang.Async;
import vcsreader.lang.FunctionExecutor;

import java.util.Date;

import static vcsreader.VcsProject.InitResult;
import static vcsreader.VcsProject.UpdateResult;

public class GitVcsRoot implements VcsRoot, VcsRoot.WithExecutor {
    private final String pathToProject;
    private final String repositoryUrl;
    private final GitSettings settings;
    private transient FunctionExecutor functionExecutor;

    public GitVcsRoot(String pathToProject, String repositoryUrl, GitSettings settings) {
        this.pathToProject = pathToProject;
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public Async<InitResult> init() {
        return functionExecutor.execute(new GitClone(settings.pathToGit, repositoryUrl, pathToProject));
    }

    @Override public Async<UpdateResult> update() {
        return functionExecutor.execute(new GitUpdate(settings.pathToGit, pathToProject));
    }

    @Override public Async<VcsProject.LogResult> log(Date fromDate, Date toDate) {
        return functionExecutor.execute(new GitLog(settings.pathToGit, pathToProject, fromDate, toDate));
    }

    @Override public Async<VcsProject.LogContentResult> contentOf(String filePath, String revision) {
        return functionExecutor.execute(new GitLogFileContent(pathToProject, filePath, revision, settings.filesCharset));
    }

    @Override public void setFunctionExecutor(FunctionExecutor functionExecutor) {
        this.functionExecutor = functionExecutor;
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
