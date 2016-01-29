package vcsreader.vcs.hg;

import vcsreader.lang.CommandLine;

class HgCommandLine {
	public static boolean isSuccessful(CommandLine commandLine) {
		// don't check stderr because hg can use it for non-error information
		return commandLine.exitCode() == 0 && commandLine.hasNoExceptions();
	}
}
