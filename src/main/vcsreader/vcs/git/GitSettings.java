package vcsreader.vcs.git;

import java.nio.charset.Charset;

public class GitSettings {
    public final String gitPath;
    public final Charset filesCharset;

	/**
	 * @param gitPath path to git executable
	 * @param filesCharset default charset of files in repository
	 *                     (it will be used if charset could not be determined from file content)
	 */
    public GitSettings(String gitPath, Charset filesCharset) {
        this.gitPath = gitPath;
        this.filesCharset = filesCharset;
    }

    public static GitSettings defaults() {
        return new GitSettings("git", Charset.forName("UTF-8"));
    }

    public GitSettings withGitPath(String value) {
        return new GitSettings(value, filesCharset);
    }

    public GitSettings withFilesCharset(Charset value) {
        return new GitSettings(gitPath, value);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitSettings that = (GitSettings) o;

        if (filesCharset != null ? !filesCharset.equals(that.filesCharset) : that.filesCharset != null) return false;
        if (gitPath != null ? !gitPath.equals(that.gitPath) : that.gitPath != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = gitPath != null ? gitPath.hashCode() : 0;
        result = 31 * result + (filesCharset != null ? filesCharset.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "GitSettings{" +
                "gitPath='" + gitPath + '\'' +
                ", filesCharset=" + filesCharset +
                '}';
    }
}
