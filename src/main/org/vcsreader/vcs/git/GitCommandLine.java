package org.vcsreader.vcs.git;

import org.vcsreader.lang.CommandLine;

class GitCommandLine {
	public static boolean isSuccessful(CommandLine commandLine) {
		// don't check stderr because git can use it for non-error information
		return commandLine.exitCode() == 0 && commandLine.hasNoExceptions();
	}
}
