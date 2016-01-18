package vcsreader.vcs.hg;

import vcsreader.lang.ShellCommand;

class HgShellCommand {
    public static boolean isSuccessful(ShellCommand shellCommand) {
        // don't check stderr because hg can use it for non-error information
        return shellCommand.exitCode() == 0 && shellCommand.hasNoExceptions();
    }
}
