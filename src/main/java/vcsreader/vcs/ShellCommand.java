package vcsreader.vcs;

import java.io.*;

import static java.nio.charset.Charset.defaultCharset;

class ShellCommand {
    private static final File CURRENT_DIRECTORY = null;

    private final String[] command;

    private String stdout = "";
    private String stderr = "";
    private int exitValue;

    public ShellCommand(String... command) {
        this.command = command;
    }

    public ShellCommand execute() {
        return execute(CURRENT_DIRECTORY);
    }

    public ShellCommand execute(File directory) {
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;
        try {
            Process process = new ProcessBuilder(command).directory(directory).start();
            stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), defaultCharset()));
            stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), defaultCharset()));

            String s;
            while ((s = stdoutReader.readLine()) != null) {
                stdout += s + "\n";
            }
            while ((s = stderrReader.readLine()) != null) {
                stderr += s + "\n";
            }

            process.waitFor();
            stdoutReader.close();
            stderrReader.close();

            process.destroy();
            exitValue = process.exitValue();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            close(stdoutReader);
            close(stderrReader);
        }

        return this;
    }

    private static void close(Reader reader) {
        if (reader == null) return;
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }

    public String stdout() {
        return stdout;
    }

    public String stderr() {
        return stderr;
    }

    public int exitValue() {
        return exitValue;
    }
}
