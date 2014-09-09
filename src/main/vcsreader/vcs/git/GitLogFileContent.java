package vcsreader.vcs.git;

import vcsreader.lang.Described;
import vcsreader.lang.FunctionExecutor;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.nio.charset.Charset;

import static vcsreader.VcsProject.LogContentResult;

/**
 * Note that this class is only compatible with git > 1.5 because "git show" seems to work only since 1.5.x
 * (http://stackoverflow.com/questions/610208/how-to-retrieve-a-single-file-from-specific-revision-in-git).
 * The assumption is that nobody uses it anymore.
 */
class GitLogFileContent implements FunctionExecutor.Function<LogContentResult>, Described {
    private final String gitPath;
    private final String folder;
    private final String filePath;
    private final String revision;
    private final Charset charset;

    GitLogFileContent(String gitPath, String folder, String filePath, String revision, Charset charset) {
        this.gitPath = gitPath;
        this.folder = folder;
        this.filePath = filePath;
        this.revision = revision;
        this.charset = charset;
    }

    @Override public LogContentResult execute() {
        ShellCommand shellCommand = gitLogFileContent(gitPath, folder, filePath, revision, charset);
        return new LogContentResult(trimLastNewLine(shellCommand.stdout()), shellCommand.stderr());
    }

    private static String trimLastNewLine(String s) {
        if (s.endsWith("\r\n")) return s.substring(0, s.length() - 2);
        else return s.endsWith("\n") || s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
    }

    static ShellCommand gitLogFileContent(String pathToGit, String folder, String filePath, String revision, Charset charset) {
        return createCommand(pathToGit, filePath, revision, charset).executeIn(folder);
    }

    private static ShellCommand createCommand(String pathToGit, String filePath, String revision, Charset charset) {
        return new ShellCommand(pathToGit, "show", revision + ":" + filePath).withCharset(charset);
    }

    @Override public String describe() {
        return createCommand(gitPath, filePath, revision, charset).describe();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitLogFileContent gitLogFileContent = (GitLogFileContent) o;

        if (filePath != null ? !filePath.equals(gitLogFileContent.filePath) : gitLogFileContent.filePath != null) return false;
        if (folder != null ? !folder.equals(gitLogFileContent.folder) : gitLogFileContent.folder != null) return false;
        if (revision != null ? !revision.equals(gitLogFileContent.revision) : gitLogFileContent.revision != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = folder != null ? folder.hashCode() : 0;
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitLogFileContent{" +
                "folder='" + folder + '\'' +
                ", filePath='" + filePath + '\'' +
                ", revision='" + revision + '\'' +
                '}';
    }
}
