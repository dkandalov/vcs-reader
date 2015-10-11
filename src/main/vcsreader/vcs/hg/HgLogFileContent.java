package vcsreader.vcs.hg;

import vcsreader.lang.ShellCommand;

import java.nio.charset.Charset;

class HgLogFileContent {
    static ShellCommand hgLogFileContent(String pathToHg, String folder, String filePath, String revision, Charset charset) {
        ShellCommand command = new ShellCommand(pathToHg, "cat", "-r " + revision, filePath);
        return command.workingDir(folder).withCharset(charset);
    }
}
