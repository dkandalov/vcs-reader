package vcsreader.vcs;

import vcsreader.CommandExecutor;

import java.io.File;

public class GitUpdate implements CommandExecutor.Command {
    private final String pathToGit;
    private final String folder;

    public GitUpdate(String pathToGit, String folder) {
        this.pathToGit = pathToGit;
        this.folder = folder;
    }

    @Override public CommandExecutor.Result execute() {

        return null;
    }

    static ShellCommand gitUpdate(String pathToGit, String folder) {
        return new ShellCommand(pathToGit, "pull", "origin").executeIn(new File(folder));
    }
}
