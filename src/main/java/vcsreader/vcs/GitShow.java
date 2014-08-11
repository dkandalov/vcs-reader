package vcsreader.vcs;

import vcsreader.CommandExecutor;
import vcsreader.VcsProject;

import java.io.File;

import static vcsreader.CommandExecutor.Result;

class GitShow implements CommandExecutor.Command {
    private final String folder;
    private final String filePath;
    private final String revision;

    GitShow(String folder, String filePath, String revision) {
        this.folder = folder;
        this.filePath = filePath;
        this.revision = revision;
    }

    @Override public Result execute() {
        ShellCommand shellCommand = gitLogFile(folder, filePath, revision);
        return new VcsProject.LogContentResult(trimLastNewLine(shellCommand.stdout()), shellCommand.stderr());
    }

    private static String trimLastNewLine(String s) {
        return s.endsWith("\n") ? s.substring(0, s.length() - 1) : s;
    }

    static ShellCommand gitLogFile(String folder, String filePath, String revision) {
        return new ShellCommand("/usr/bin/git", "show", revision + ":" + filePath).execute(new File(folder));
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitShow gitShow = (GitShow) o;

        if (filePath != null ? !filePath.equals(gitShow.filePath) : gitShow.filePath != null) return false;
        if (folder != null ? !folder.equals(gitShow.folder) : gitShow.folder != null) return false;
        if (revision != null ? !revision.equals(gitShow.revision) : gitShow.revision != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = folder != null ? folder.hashCode() : 0;
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        return result;
    }
}
