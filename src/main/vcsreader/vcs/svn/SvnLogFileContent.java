package vcsreader.vcs.svn;

import vcsreader.VcsProject;
import vcsreader.lang.FunctionExecutor;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.nio.charset.Charset;

class SvnLogFileContent implements FunctionExecutor.Function<VcsProject.LogContentResult> {
    private final String pathToSvn;
    private final String repositoryUrl;
    private final String fileName;
    private final String revision;
    private final Charset charset;

    SvnLogFileContent(String pathToSvn, String repositoryUrl, String fileName, String revision, Charset charset) {
        this.pathToSvn = pathToSvn;
        this.repositoryUrl = repositoryUrl;
        this.fileName = fileName;
        this.revision = revision;
        this.charset = charset;
    }

    @Override public VcsProject.LogContentResult execute() {
        ShellCommand command = svnLogFileContent(pathToSvn, repositoryUrl, fileName, revision, charset);
        return new VcsProject.LogContentResult(trimLastNewLine(command.stdout()), command.stderr());
    }

    static ShellCommand svnLogFileContent(String pathToSvn, String repositoryUrl, String fileName, String revision, Charset charset) {
        return new ShellCommand(pathToSvn, "cat", repositoryUrl + "/" + fileName + "@" + revision)
                .withCharset(charset).execute();
    }

    private static String trimLastNewLine(String s) {
        if (s.endsWith("\r\n")) return s.substring(0, s.length() - 2);
        else return s.endsWith("\n") || s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
    }
}
