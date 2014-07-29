package vcsreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.Charset.defaultCharset;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        String fromUrl = "https://github.com/junit-team/junit";
        String toFolder = "/tmp/junit-test";
        new CommandRunner().runCommand("git", "clone", "-v", fromUrl, toFolder);
    }

    private static class CommandRunner {
        public void runCommand(String... command) throws IOException, InterruptedException {
            Process process = new ProcessBuilder(command).start();
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), defaultCharset()));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), defaultCharset()));

            String stdout = "";
            String stderr = "";

            String s;
            while ((s = stdoutReader.readLine()) != null) {
                stdout += s;
            }
            while ((s = stderrReader.readLine()) != null) {
                stderr += s;
            }

            System.out.println(stdout);
            System.out.println(stderr);

            process.waitFor();
            stdoutReader.close();
            stderrReader.close();

            process.destroy();
        }
    }
}
