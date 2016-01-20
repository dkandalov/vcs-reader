package vcsreader.vcs.git;

import vcsreader.lang.ExternalCommand;

class GitExternalCommand {
	public static boolean isSuccessful(ExternalCommand command) {
		// don't check stderr because git can use it for non-error information
		return command.exitCode() == 0 && command.hasNoExceptions();
	}
}
