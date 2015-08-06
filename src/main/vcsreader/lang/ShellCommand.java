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
    private static final int defaultBufferSize = 8192;

    private final String[] commandAndArgs;
    private final StringBuilder stdout;
    private final StringBuilder stderr;
    private int exitCode = exitCodeBeforeFinished;

    private int inputBufferSize = defaultBufferSize;
    private File workingDirectory = currentDirectory;
    private Charset outputCharset = Charset.defaultCharset();

    private final AtomicReference<Process> processRef = new AtomicReference<Process>();


    public ShellCommand(String... commandAndArgs) {
        this(defaultBufferSize, commandAndArgs);
    }

    public ShellCommand(int inputBufferSize, String... commandAndArgs) {
        this.inputBufferSize = inputBufferSize;
        this.commandAndArgs = checkForNulls(commandAndArgs);
        this.stdout = new StringBuilder(inputBufferSize);
        this.stderr = new StringBuilder(inputBufferSize);
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
            stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), outputCharset), inputBufferSize);
            stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), outputCharset), inputBufferSize);

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

    private static String[] checkForNulls(String[] command) {
        for (String arg : command) {
            if (arg == null) {
                throw new IllegalStateException("Shell command cannot have null as inputs, but was: " + Arrays.toString(command));
            }
        }
        return command;
    }

    private static void close(Reader reader) {
        if (reader == null) return;
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }
}
