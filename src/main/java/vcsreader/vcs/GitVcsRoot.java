package vcsreader.vcs;

import vcsreader.CommandExecutor;
import vcsreader.VcsRoot;
import vcsreader.lang.Async;

import java.util.Date;

import static vcsreader.CommandExecutor.Result;

public class GitVcsRoot implements VcsRoot {
    private final String pathToProject;
    private final String repositoryUrl;
    private final GitSettings settings;
    private CommandExecutor commandExecutor;

    public GitVcsRoot(String pathToProject, String repositoryUrl, GitSettings settings) {
        this.pathToProject = pathToProject;
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public Async<Result> init() {
        return commandExecutor.execute(new GitClone(settings.pathToGit, repositoryUrl, pathToProject));
    }

    @Override public Async<Result> log(Date fromDate, Date toDate) {
        return commandExecutor.execute(new GitLog(settings.pathToGit, pathToProject, fromDate, toDate));
    }

    @Override public Async<Result> contentOf(String filePath, String revision) {
        return commandExecutor.execute(new GitLogFileContent(pathToProject, filePath, revision));
    }

    @Override public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }
}
