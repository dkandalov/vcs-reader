package vcsreader.vcs.hg;

import vcsreader.VcsProject;
import vcsreader.lang.ShellCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

class HgUpdate implements VcsCommand<VcsProject.UpdateResult> {
    private final String hgPath;
    private final String folder;
    private final ShellCommand shellCommand;

    public HgUpdate(String hgPath, String folder) {
        this.hgPath = hgPath;
        this.folder = folder;
        this.shellCommand = hgUpdate(hgPath, folder);
    }

    @Override public VcsProject.UpdateResult execute() {
        shellCommand.execute();
        if (HgShellCommand.isSuccessful(shellCommand)) {
            return new VcsProject.UpdateResult();
        } else {
            return new VcsProject.UpdateResult(shellCommand.stderr() + shellCommand.exceptionStacktrace());
        }
    }

    static ShellCommand hgUpdate(String pathToHg, String folder) {
        return new ShellCommand(pathToHg, "pull").workingDir(folder);
    }

    @Override public String describe() {
        return shellCommand.describe();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HgUpdate hgUpdate = (HgUpdate) o;

        return !(hgPath != null ? !hgPath.equals(hgUpdate.hgPath) : hgUpdate.hgPath != null) && !(folder != null ? !folder.equals(hgUpdate.folder) : hgUpdate.folder != null);
    }

    @Override public int hashCode() {
        int result = hgPath != null ? hgPath.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "HgUpdate{" +
                "hgPath='" + hgPath + '\'' +
                ", folder='" + folder + '\'' +
                '}';
    }
}
