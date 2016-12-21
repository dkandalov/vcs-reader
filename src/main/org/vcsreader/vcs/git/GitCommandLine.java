package org.vcsreader.vcs.git;

import org.vcsreader.lang.CommandLine;

import java.io.File;

class GitCommandLine {
	public static boolean isSuccessful(CommandLine commandLine) {
		// don't check stderr because git can use it for non-error information
		return commandLine.exitCode() == 0;
	}

	public static boolean containsGitRepo(String path) {
		File file = new File(path);
		if (!file.exists()) return false;
		String[] children = file.list((dir, name) -> name.equals(".git"));
		return children != null && children.length > 0;
	}
}
