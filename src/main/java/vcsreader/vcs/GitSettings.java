package vcsreader.vcs;

public class GitSettings {
    public final String pathToGit;

    public GitSettings(String pathToGit) {
        this.pathToGit = pathToGit;
    }

    public static GitSettings defaults() {
        return new GitSettings("/usr/bin/git");
    }
}
