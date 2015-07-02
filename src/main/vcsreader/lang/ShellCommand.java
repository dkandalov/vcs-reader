package vcsreader.lang;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ShellCommand {
    private static final File currentDirectory = null;
    private static final int exitCodeBeforeFinished = -123;
    private static final int exitCodeOnException = -1;

    private final String[] commandAndArgs;

    private final StringBuilder stdout = new StringBuilder();
    private final StringBuilder stderr = new StringBuilder();
    private final AtomicReference<Process> processRef = new AtomicReference<Process>();
    private int exitCode = exitCodeBeforeFinished;
    private File workingDirectory = currentDirectory;
    private Charset outputCharset = Charset.defaultCharset();


    public ShellCommand(String... commandAndArgs) {
        checkForNulls(commandAndArgs);
        this.commandAndArgs = commandAndArgs;
    }

    public ShellCommand workingDir(String path) {
        workingDirectory = new File(path);
        return this;
    }

    public ShellCommand withCharset(Charset charset) {
        outputCharset = charset;
        return this;
    }

    public ShellCommand execute() {
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;
        try {

            Process process = new ProcessBuilder(commandAndArgs).directory(workingDirectory).start();
            processRef.set(process);
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
            exitCode = process.exitValue();

        } catch (IOException e) {
            if (stderr.length() > 0) stderr.append("\n");
            stderr.append(e.getMessage());
            exitCode = exitCodeOnException;
        } catch (InterruptedException e) {
            if (stderr.length() > 0) stderr.append("\n");
            stderr.append(e.getMessage());
            exitCode = exitCodeOnException;
        } finally {
            close(stdoutReader);
            close(stderrReader);
        }

        return this;
    }

    public void kill() {
        if (processRef.get() != null) {
            processRef.get().destroy();
        }
    }

    @NotNull public String stdout() {
        return stdout.toString();
    }

    @NotNull public String stderr() {
        return stderr.toString();
    }

    public int exitCode() {
        return exitCode;
    }

    public String describe() {
        String result = "";
        for (int i = 0; i < commandAndArgs.length; i++) {
            result += commandAndArgs[i];
            if (i < commandAndArgs.length - 1) result += " ";
        }
        if (workingDirectory != null) {
            result += " (running in " + workingDirectory + ")";
        }
        return result;
    }

    private static void checkForNulls(String[] command) {
        for (String arg : command) {
            if (arg == null) {
                throw new IllegalStateException("Shell command cannot have null as inputs, but was: " + Arrays.toString(command));
            }
        }
    }

    private static void close(Reader reader) {
        if (reader == null) return;
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }
}
