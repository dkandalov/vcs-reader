package vcsreader.vcs.svn;

import org.jetbrains.annotations.NotNull;
import vcsreader.lang.Described;
import vcsreader.lang.FunctionExecutor;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.nio.charset.Charset;

import static vcsreader.VcsProject.LogContentResult;

class SvnLogFileContent implements FunctionExecutor.Function<LogContentResult>, Described {
    private final String pathToSvn;
    private final String repositoryRoot;
    private final String fileName;
    private final String revision;
    private final Charset charset;

    SvnLogFileContent(String pathToSvn, String repositoryRoot, String fileName, String revision, Charset charset) {
        this.pathToSvn = pathToSvn;
        this.repositoryRoot = repositoryRoot;
        this.fileName = fileName;
        this.revision = revision;
        this.charset = charset;
    }

    @Override public LogContentResult execute() {
        ShellCommand command = svnLogFileContent(pathToSvn, repositoryRoot, fileName, revision, charset);
        return new LogContentResult(trimLastNewLine(command.stdout()), command.stderr());
    }

    static ShellCommand svnLogFileContent(String pathToSvn, String repositoryRoot, String fileName, String revision, Charset charset) {
        return createCommand(pathToSvn, repositoryRoot, fileName, revision, charset).execute();
    }

    private static ShellCommand createCommand(String pathToSvn, String repositoryRoot, String fileName, String revision, Charset charset) {
        String filePath = repositoryRoot + "/" + fileName + "@" + revision;
        return new ShellCommand(pathToSvn, "cat", filePath).withCharset(charset);
    }

    @NotNull private static String trimLastNewLine(String s) {
        if (s.endsWith("\r\n")) return s.substring(0, s.length() - 2);
        else return s.endsWith("\n") || s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
    }

    @Override public String describe() {
        return createCommand(pathToSvn, repositoryRoot, fileName, revision, charset).describe();
    }
}
