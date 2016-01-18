package vcsreader.vcs.git;

import vcsreader.lang.ShellCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import static vcsreader.VcsProject.UpdateResult;
import static vcsreader.vcs.git.GitShellCommand.isSuccessful;

class GitUpdate implements VcsCommand<UpdateResult> {
    private final String gitPath;
    private final String folder;
    private final ShellCommand shellCommand;

    public GitUpdate(String gitPath, String folder) {
        this.gitPath = gitPath;
        this.folder = folder;
        this.shellCommand = gitUpdate(gitPath, folder);
    }

    @Override public UpdateResult execute() {
        shellCommand.execute();
        if (isSuccessful(shellCommand)) {
            return new UpdateResult();
        } else {
            return new UpdateResult(shellCommand.stderr() + shellCommand.exceptionStacktrace());
        }
    }

    static ShellCommand gitUpdate(String pathToGit, String folder) {
        return new ShellCommand(pathToGit, "pull", "origin").workingDir(folder);
    }

    @Override public String describe() {
        return shellCommand.describe();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitUpdate gitUpdate = (GitUpdate) o;

        if (folder != null ? !folder.equals(gitUpdate.folder) : gitUpdate.folder != null) return false;
        if (gitPath != null ? !gitPath.equals(gitUpdate.gitPath) : gitUpdate.gitPath != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = gitPath != null ? gitPath.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitUpdate{" +
                "gitPath='" + gitPath + '\'' +
                ", folder='" + folder + '\'' +
                '}';
    }
}
