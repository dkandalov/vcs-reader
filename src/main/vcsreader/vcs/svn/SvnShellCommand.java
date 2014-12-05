package vcsreader.vcs.svn;

import vcsreader.vcs.infrastructure.ShellCommand;

class SvnShellCommand {

    public static ShellCommand createShellCommand(String svnPath, String... args) {
        String[] commandAndArgs = concat(new String[]{svnPath, "--non-interactive", "--trust-server-cert"}, args);
        return new ShellCommand(commandAndArgs);
    }

    public static boolean isSuccessful(ShellCommand shellCommand) {
        return shellCommand.stderr().trim().isEmpty() && shellCommand.exitCode() == 0;
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
