package vcsreader.vcs;

import vcsreader.CommandExecutor;

public class GitClone implements CommandExecutor.Command {
    public GitClone(String repositoryUrl, String pathToProject) {
        // TODO implement

    }

    public static class Result implements CommandExecutor.AsyncResult{
        public void succeed() {
            // TODO implement

        }
    }
}
