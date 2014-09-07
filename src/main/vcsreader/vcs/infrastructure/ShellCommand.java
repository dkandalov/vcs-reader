package vcsreader.vcs.infrastructure;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

import static java.nio.charset.Charset.forName;

public class ShellCommand {
    private static final File CURRENT_DIRECTORY = null;

    private final String[] command;

    private final StringBuilder stdout = new StringBuilder();
    private final StringBuilder stderr = new StringBuilder();
    private int exitValue;
    private Charset outputCharset = forName("UTF-8");

    public ShellCommand(String... command) {
        checkForNulls(command);
        this.command = command;
    }

    public ShellCommand withCharset(Charset charset) {
        outputCharset = charset;
        return this;
    }

    public ShellCommand execute() {
        return executeIn(CURRENT_DIRECTORY);
    }

    public ShellCommand executeIn(File directory) {
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;
        try {

            Process process = new ProcessBuilder(command).directory(directory).start();
            stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), outputCharset));
            stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), outputCharset));

            int i;
            while ((i = stdoutReader.read()) != -1) {
                stdout.append((char) i);
            }
            while ((i = stderrReader.read()) != -1) {
                stderr.append((char) i);
            }

            process.waitFor();
            stdoutReader.close();
            stderrReader.close();

            process.destroy();
            exitValue = process.exitValue();

        } catch (IOException e) {
            stderr.append("\n").append(asString(e));
            exitValue = -1;
        } catch (InterruptedException e) {
            stderr.append("\n").append(asString(e));
            exitValue = -1;
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

    @NotNull public String stdout() {
        return stdout.toString();
    }

    @NotNull public String stderr() {
        return stderr.toString();
    }

    public int exitValue() {
        return exitValue;
    }

    private static String asString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.getBuffer().toString();
    }

    private static void checkForNulls(String[] command) {
        for (String arg : command) {
            if (arg == null) {
                throw new IllegalStateException("Shell command cannot have null as inputs, but was: " + Arrays.toString(command));
            }
        }
    }

    public String describe() {
        String result = "";
        for (String s : command) {
            result += s + " ";
        }
        return result;
    }
}
