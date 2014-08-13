package vcsreader.vcs;

import vcsreader.CommandExecutor;
import vcsreader.VcsProject;

import static java.util.Arrays.asList;

class GitClone implements CommandExecutor.Command {
    private final String repositoryUrl;
    private final String localPath;
    private final String pathToGit;

    public GitClone(String pathToGit, String repositoryUrl, String localPath) {
        this.pathToGit = pathToGit;
        this.repositoryUrl = repositoryUrl;
        this.localPath = localPath;
    }

    @Override public CommandExecutor.Result execute() {
        ShellCommand shellCommand = gitClone(pathToGit, repositoryUrl, localPath);
        boolean successful = (shellCommand.exitValue() == 0);
        if (successful) {
            return new VcsProject.InitResult();
        } else {
            return new VcsProject.InitResult(asList(shellCommand.stderr()));
        }
    }

    static ShellCommand gitClone(String pathToGit, String repositoryUrl, String localPath) {
        return new ShellCommand(pathToGit, "clone", "-v", repositoryUrl, localPath).execute();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitClone gitClone = (GitClone) o;

        if (localPath != null ? !localPath.equals(gitClone.localPath) : gitClone.localPath != null)
            return false;
        if (repositoryUrl != null ? !repositoryUrl.equals(gitClone.repositoryUrl) : gitClone.repositoryUrl != null)
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = repositoryUrl != null ? repositoryUrl.hashCode() : 0;
        result = 31 * result + (localPath != null ? localPath.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitClone{" +
                "repositoryUrl='" + repositoryUrl + '\'' +
                ", localPath='" + localPath + '\'' +
                '}';
    }
}
