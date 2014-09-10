package vcsreader.vcs.git;

import vcsreader.lang.VcsCommand;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.nio.charset.Charset;

import static vcsreader.VcsProject.LogContentResult;

/**
 * Note that this class is only compatible with git > 1.5 because "git show" seems to work only since 1.5.x
 * (http://stackoverflow.com/questions/610208/how-to-retrieve-a-single-file-from-specific-revision-in-git).
 * The assumption is that nobody uses it anymore.
 */
class GitLogFileContent implements VcsCommand<LogContentResult> {
    private final String gitPath;
    private final String folder;
    private final String filePath;
    private final String revision;
    private final Charset charset;
    private final ShellCommand shellCommand;

    GitLogFileContent(String gitPath, String folder, String filePath, String revision, Charset charset) {
        this.gitPath = gitPath;
        this.folder = folder;
        this.filePath = filePath;
        this.revision = revision;
        this.charset = charset;
        this.shellCommand = gitLogFileContent(gitPath, folder, filePath, revision, charset);
    }

    @Override public LogContentResult execute() {
        shellCommand.execute();
        // TODO use exit code
        return new LogContentResult(trimLastNewLine(shellCommand.stdout()), shellCommand.stderr());
    }

    static ShellCommand gitLogFileContent(String pathToGit, String folder, String filePath, String revision, Charset charset) {
        return new ShellCommand(pathToGit, "show", revision + ":" + filePath).withCharset(charset).workingDir(folder);
    }

    private static String trimLastNewLine(String s) {
        if (s.endsWith("\r\n")) return s.substring(0, s.length() - 2);
        else return s.endsWith("\n") || s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
    }

    @Override public String describe() {
        return shellCommand.describe();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitLogFileContent that = (GitLogFileContent) o;

        if (charset != null ? !charset.equals(that.charset) : that.charset != null) return false;
        if (filePath != null ? !filePath.equals(that.filePath) : that.filePath != null) return false;
        if (folder != null ? !folder.equals(that.folder) : that.folder != null) return false;
        if (gitPath != null ? !gitPath.equals(that.gitPath) : that.gitPath != null) return false;
        if (revision != null ? !revision.equals(that.revision) : that.revision != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = gitPath != null ? gitPath.hashCode() : 0;
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitLogFileContent{" +
                "gitPath='" + gitPath + '\'' +
                ", folder='" + folder + '\'' +
                ", filePath='" + filePath + '\'' +
                ", revision='" + revision + '\'' +
                ", charset=" + charset +
                '}';
    }
}