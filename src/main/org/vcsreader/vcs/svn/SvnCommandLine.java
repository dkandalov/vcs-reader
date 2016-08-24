package org.vcsreader.vcs.svn;

import org.vcsreader.lang.CommandLine;

class SvnCommandLine {

	public static CommandLine newExternalCommand(String svnPath, String... args) {
		String[] commandAndArgs = concat(new String[]{svnPath, "--non-interactive", "--trust-server-cert"}, args);
		return new CommandLine(commandAndArgs);
	}

	public static boolean isSuccessful(CommandLine commandLine) {
		return commandLine.stderr().trim().isEmpty() && commandLine.exitCode() == 0;
	}

	private static String[] concat(String[] array1, String[] array2) {
		String[] result = new String[array1.length + array2.length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}
}
