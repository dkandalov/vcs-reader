package vcsreader;

import java.io.IOException;

import static vcsreader.GitCommands.gitClone;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String fromUrl = "file:///Users/dima/IdeaProjects/junit/";
        String toFolder = "/tmp/junit-test";
        Command command = gitClone(fromUrl, toFolder);

        System.out.println(command.stdout());
        System.out.println(command.stderr());
        System.out.println(command.exitValue());
    }
}
