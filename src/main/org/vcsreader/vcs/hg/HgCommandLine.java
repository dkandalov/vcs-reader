package org.vcsreader.vcs.hg;

import org.vcsreader.lang.CommandLine;

class HgCommandLine {
	public static boolean isSuccessful(CommandLine commandLine) {
		// don't check stderr because hg can use it for non-error information
		return commandLine.exitCode() == 0;
	}
}
