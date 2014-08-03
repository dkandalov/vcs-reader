package vcsreader.vcs;

import vcsreader.CommandExecutor;
import vcsreader.VcsRoot;

public class GitVcsRoot implements VcsRoot {
    private final String pathToProject;
    private final String repositoryUrl;

    public GitVcsRoot(String pathToProject, String repositoryUrl) {
        this.pathToProject = pathToProject;
        this.repositoryUrl = repositoryUrl;
    }

    @Override public CommandExecutor.AsyncResult init(CommandExecutor commandExecutor) {
        return commandExecutor.execute(new GitClone(repositoryUrl, pathToProject));
    }
}
