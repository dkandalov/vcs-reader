package vcsreader.vcs.hg;

import vcsreader.lang.ShellCommand;

class HgClone {
    static ShellCommand hgClone(String pathToHg, String repositoryUrl, String targetPath) {
        return new ShellCommand(pathToHg, "clone", "-v", repositoryUrl, targetPath);
    }
}
