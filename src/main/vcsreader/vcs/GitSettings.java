package vcsreader.vcs;

public class GitSettings {
    public final String pathToGit;

    public GitSettings(String pathToGit) {
        this.pathToGit = pathToGit;
    }

    public static GitSettings defaults() {
        return new GitSettings("/usr/bin/git");
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitSettings that = (GitSettings) o;

        return !(pathToGit != null ? !pathToGit.equals(that.pathToGit) : that.pathToGit != null);
    }

    @Override public int hashCode() {
        return pathToGit != null ? pathToGit.hashCode() : 0;
    }

    @Override public String toString() {
        return "GitSettings{" +
                "pathToGit='" + pathToGit + '\'' +
                '}';
    }
}
