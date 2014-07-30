package vcsreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.Charset.defaultCharset;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        String fromUrl = "https://github.com/junit-team/junit";
        String toFolder = "/tmp/junit-test";
        Command command = new Command("git", "clone", "-v", fromUrl, toFolder).execute();

        System.out.println(command.stdout);
        System.out.println(command.stderr);
        System.out.println(command.exitValue);
    }

    private static class Command {
        private final String[] command;

        private String stdout = "";
        private String stderr = "";
        private int exitValue;

        private boolean finished;
        private Exception exception;

        private Command(String... command) {
            this.command = command;
        }

        public Command execute() {
            BufferedReader stdoutReader = null;
            BufferedReader stderrReader = null;
            try {
                Process process = new ProcessBuilder(command).start();
                stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), defaultCharset()));
                stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), defaultCharset()));

                String s;
                while ((s = stdoutReader.readLine()) != null) {
                    stdout += s;
                }
                while ((s = stderrReader.readLine()) != null) {
                    stderr += s;
                }

                process.waitFor();
                stdoutReader.close();
                stderrReader.close();

                process.destroy();
                exitValue = process.exitValue();
                finished = true;

            } catch (IOException e) {
                exception = e;
            } catch (InterruptedException e) {
                exception = e;
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
    }
}
