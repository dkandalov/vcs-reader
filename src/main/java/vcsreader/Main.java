package vcsreader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Command command = new Command("echo", "aaa").execute();

        System.out.println("out " + command.stdout());
        System.out.println("err " + command.stderr());
        System.out.println(command.exitValue());
    }
}
