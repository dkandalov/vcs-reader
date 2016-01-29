package vcsreader.vcs.svn;

import vcsreader.lang.CommandLine;

class SvnCommandLine {

	public static CommandLine newExternalCommand(String svnPath, String... args) {
		String[] commandAndArgs = concat(new String[]{svnPath, "--non-interactive", "--trust-server-cert"}, args);
		return new CommandLine(commandAndArgs);
	}

	public static boolean isSuccessful(CommandLine commandLine) {
		return commandLine.stderr().trim().isEmpty() && commandLine.exitCode() == 0 && commandLine.hasNoExceptions();
	}

	private static String[] concat(String[] array1, String[] array2) {
		int length1 = array1.length;
		int length2 = array2.length;
		String[] result = new String[length1 + length2];
		System.arraycopy(array1, 0, result, 0, length1);
		System.arraycopy(array2, 0, result, length1, length2);
		return result;
	}
}
