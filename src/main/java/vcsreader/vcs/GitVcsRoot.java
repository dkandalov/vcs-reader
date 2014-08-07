package vcsreader.vcs;

import vcsreader.CommandExecutor;
import vcsreader.VcsRoot;
import vcsreader.lang.Async;

import java.util.Date;

import static vcsreader.CommandExecutor.Result;

public class GitVcsRoot implements VcsRoot {
    private final String pathToProject;
    private final String repositoryUrl;

    public GitVcsRoot(String pathToProject, String repositoryUrl) {
        this.pathToProject = pathToProject;
        this.repositoryUrl = repositoryUrl;
    }

    @Override public Async<Result> init(CommandExecutor commandExecutor) {
        return commandExecutor.execute(new GitClone(repositoryUrl, pathToProject));
    }

    @Override public Async<Result> log(CommandExecutor commandExecutor, Date from, Date to) {
        return commandExecutor.execute(new GitLog(pathToProject, from, to));
    }
}
