package vcsreader.vcs.hg;

import vcsreader.lang.ExternalCommand;

class HgExternalCommand {
	public static boolean isSuccessful(ExternalCommand command) {
		// don't check stderr because hg can use it for non-error information
		return command.exitCode() == 0 && command.hasNoExceptions();
	}
}
