package org.vcsreader.vcs.hg;

import org.vcsreader.lang.CommandLine;

import java.io.File;

class HgUtil {
	public static boolean isSuccessful(CommandLine commandLine) {
		// don't check stderr because hg can use it for non-error information
		return commandLine.exitCode() == 0;
	}

	public static boolean containsHgRepo(String path) {
		File file = new File(path);
		if (!file.exists()) return false;
		String[] children = file.list((dir, name) -> name.equals(".hg"));
		return children != null && children.length > 0;
	}
}
