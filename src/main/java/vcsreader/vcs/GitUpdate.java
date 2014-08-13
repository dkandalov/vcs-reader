package vcsreader.vcs;

import vcsreader.CommandExecutor;

import java.io.File;

import static java.util.Arrays.asList;
import static vcsreader.VcsProject.UpdateResult;

class GitUpdate implements CommandExecutor.Command<UpdateResult> {
    private final String pathToGit;
    private final String folder;

    public GitUpdate(String pathToGit, String folder) {
        this.pathToGit = pathToGit;
        this.folder = folder;
    }

    @Override public UpdateResult execute() {
        ShellCommand shellCommand = gitUpdate(pathToGit, folder);
        boolean successful = (shellCommand.exitValue() == 0);
        if (successful) {
            return new UpdateResult();
        } else {
            return new UpdateResult(asList(shellCommand.stderr()));
        }
    }

    static ShellCommand gitUpdate(String pathToGit, String folder) {
        return new ShellCommand(pathToGit, "pull", "origin").executeIn(new File(folder));
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitUpdate gitUpdate = (GitUpdate) o;

        if (folder != null ? !folder.equals(gitUpdate.folder) : gitUpdate.folder != null) return false;
        if (pathToGit != null ? !pathToGit.equals(gitUpdate.pathToGit) : gitUpdate.pathToGit != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = pathToGit != null ? pathToGit.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        return result;
    }
}
