package vcsreader.vcs;

import vcsreader.CommandExecutor;
import vcsreader.VcsRoot;

import java.util.Date;

import static vcsreader.CommandExecutor.AsyncResult;

public class GitVcsRoot implements VcsRoot {
    private final String pathToProject;
    private final String repositoryUrl;

    public GitVcsRoot(String pathToProject, String repositoryUrl) {
        this.pathToProject = pathToProject;
        this.repositoryUrl = repositoryUrl;
    }

    @Override public AsyncResult init(CommandExecutor commandExecutor) {
        return commandExecutor.execute(new GitClone(repositoryUrl, pathToProject));
    }

    @Override public AsyncResult log(CommandExecutor commandExecutor, Date from, Date to) {
        return commandExecutor.execute(new GitLog(pathToProject, from, to));
    }
}
