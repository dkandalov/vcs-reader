package vcsreader.vcs;

import vcsreader.CommandExecutor;

public class GitClone implements CommandExecutor.Command {
    private final String repositoryUrl;
    private final String pathToProject;

    public GitClone(String repositoryUrl, String pathToProject) {
        this.repositoryUrl = repositoryUrl;
        this.pathToProject = pathToProject;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitClone gitClone = (GitClone) o;

        if (pathToProject != null ? !pathToProject.equals(gitClone.pathToProject) : gitClone.pathToProject != null)
            return false;
        if (repositoryUrl != null ? !repositoryUrl.equals(gitClone.repositoryUrl) : gitClone.repositoryUrl != null)
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = repositoryUrl != null ? repositoryUrl.hashCode() : 0;
        result = 31 * result + (pathToProject != null ? pathToProject.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitClone{" +
                "repositoryUrl='" + repositoryUrl + '\'' +
                ", pathToProject='" + pathToProject + '\'' +
                '}';
    }

    public static class Result implements CommandExecutor.AsyncResult {
        private Runnable whenReadyCallback;

        @Override public void whenReady(Runnable runnable) {
            this.whenReadyCallback = runnable;
        }

        public void succeed() {
            whenReadyCallback.run();
        }
    }
}
