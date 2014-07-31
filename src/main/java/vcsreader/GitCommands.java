package vcsreader;

public class GitCommands {
    public static Command gitClone(String fromUrl, String toFolder) {
        return new Command("git", "clone", "-v", fromUrl, toFolder).execute();
    }
}
