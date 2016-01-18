package vcsreader.vcs.git;

import vcsreader.lang.ExternalCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import static vcsreader.VcsProject.UpdateResult;
import static vcsreader.vcs.git.GitExternalCommand.isSuccessful;

class GitUpdate implements VcsCommand<UpdateResult> {
    private final String gitPath;
    private final String folder;
    private final ExternalCommand command;

    public GitUpdate(String gitPath, String folder) {
        this.gitPath = gitPath;
        this.folder = folder;
        this.command = gitUpdate(gitPath, folder);
    }

    @Override public UpdateResult execute() {
        command.execute();
        if (isSuccessful(command)) {
            return new UpdateResult();
        } else {
            return new UpdateResult(command.stderr() + command.exceptionStacktrace());
        }
    }

    static ExternalCommand gitUpdate(String pathToGit, String folder) {
        return new ExternalCommand(pathToGit, "pull", "origin").workingDir(folder);
    }

    @Override public String describe() {
        return command.describe();
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
