package vcsreader.vcs;

import vcsreader.CommandExecutor;

public class GitClone implements CommandExecutor.Command {
    private final String repositoryUrl;
    private final String localPath;

    public GitClone(String repositoryUrl, String localPath) {
        this.repositoryUrl = repositoryUrl;
        this.localPath = localPath;
    }

    @Override public CommandExecutor.Result execute() {
        ShellCommand shellCommand = new ShellCommand("/usr/bin/git", "clone", "-v", repositoryUrl, localPath).execute();
        return new CommandExecutor.Result(shellCommand.stdout(), shellCommand.stderr(), shellCommand.exitValue());
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
