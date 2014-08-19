package vcsreader.vcs.git;

import java.nio.charset.Charset;

public class GitSettings {
    public final String pathToGit;
    public final Charset filesCharset;

    public GitSettings(String pathToGit, Charset filesCharset) {
        this.pathToGit = pathToGit;
        this.filesCharset = filesCharset;
    }

    public static GitSettings defaults() {
        return new GitSettings("/usr/bin/git", Charset.forName("UTF-8"));
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitSettings that = (GitSettings) o;

        if (filesCharset != null ? !filesCharset.equals(that.filesCharset) : that.filesCharset != null) return false;
        if (pathToGit != null ? !pathToGit.equals(that.pathToGit) : that.pathToGit != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pathToGit != null ? pathToGit.hashCode() : 0;
        result = 31 * result + (filesCharset != null ? filesCharset.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitSettings{" +
                "pathToGit='" + pathToGit + '\'' +
                ", filesCharset=" + filesCharset +
                '}';
    }
}
