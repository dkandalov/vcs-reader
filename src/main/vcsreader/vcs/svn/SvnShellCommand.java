package vcsreader.vcs.svn;

import vcsreader.vcs.infrastructure.ShellCommand;

class SvnShellCommand {
    public static boolean isSuccessful(ShellCommand shellCommand) {
        return shellCommand.stderr().trim().isEmpty() && shellCommand.exitValue() == 0;
    }
}
