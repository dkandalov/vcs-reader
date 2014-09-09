package vcsreader.vcs.git;

import vcsreader.lang.VcsCommand;
import vcsreader.vcs.infrastructure.ShellCommand;

import static java.util.Arrays.asList;
import static vcsreader.VcsProject.UpdateResult;

class GitUpdate implements VcsCommand<UpdateResult> {
    private final String gitPath;
    private final String folder;

    public GitUpdate(String gitPath, String folder) {
        this.gitPath = gitPath;
        this.folder = folder;
    }

    @Override public UpdateResult execute() {
        ShellCommand shellCommand = gitUpdate(gitPath, folder);
        boolean successful = (shellCommand.exitValue() == 0);
        if (successful) {
            return new UpdateResult();
        } else {
            return new UpdateResult(asList(shellCommand.stderr()));
        }
    }

    static ShellCommand gitUpdate(String pathToGit, String folder) {
        return createCommand(pathToGit).executeIn(folder);
    }

    private static ShellCommand createCommand(String pathToGit) {
        return new ShellCommand(pathToGit, "pull", "origin");
    }

    @Override public String describe() {
        return createCommand(gitPath).describe();
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
