package vcsreader.vcs.svn;

import vcsreader.VcsProject;
import vcsreader.lang.FunctionExecutor;
import vcsreader.vcs.infrastructure.ShellCommand;

import java.nio.charset.Charset;

class SvnLogFileContent implements FunctionExecutor.Function<VcsProject.LogContentResult> {
    @Override public VcsProject.LogContentResult execute() {
        return null;
    }

    static ShellCommand svnLogFileContent(String pathToSvn, String repositoryUrl, String fileName, String revision, Charset charset) {
        return new ShellCommand(pathToSvn, "cat", repositoryUrl + "/" + fileName + "@" + revision)
                .withCharset(charset).execute();
    }
}
