package vcsreader.vcs;

import java.io.File;

public class GitShellCommands {
    public static ShellCommand gitLogFile(String folder, String filePath, String revision) {
        return new ShellCommand("/usr/bin/git", "show", revision + ":" + filePath).execute(new File(folder));
    }

}
