package vcsreader.vcs.git;

import vcsreader.vcs.infrastructure.ShellCommand;

class GitShellCommand {
    public static boolean isSuccessful(ShellCommand shellCommand) {
        // don't check stderr because git can use it for non-error information
        return shellCommand.exitCode() == 0;
    }
}
