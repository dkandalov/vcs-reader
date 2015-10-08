package vcsreader.vcs.hg;

import vcsreader.lang.ShellCommand;

class HgUpdate {
    static ShellCommand hgUpdate(String pathToHg, String folder) {
        return new ShellCommand(pathToHg, "pull").workingDir(folder);
    }
}
