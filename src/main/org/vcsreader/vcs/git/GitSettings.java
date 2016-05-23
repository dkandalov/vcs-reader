package org.vcsreader.vcs.git;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.lang.Charsets;

import java.nio.charset.Charset;

public class GitSettings {
	@NotNull public final String gitPath;
	@NotNull public final Charset defaultFileCharset;

	/**
	 * @param gitPath            path to git executable
	 * @param defaultFileCharset default charset of files in repository
	 *                           (it will be used if charset could not be determined from file content)
	 */
	public GitSettings(@NotNull String gitPath, @NotNull Charset defaultFileCharset) {
		this.gitPath = gitPath;
		this.defaultFileCharset = defaultFileCharset;
	}

	public static GitSettings defaults() {
		return new GitSettings("git", Charsets.UTF8);
	}

	public GitSettings withGitPath(String value) {
		return new GitSettings(value, defaultFileCharset);
	}

	public GitSettings withDefaultFileCharset(Charset value) {
		return new GitSettings(gitPath, value);
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitSettings that = (GitSettings) o;

		if (!defaultFileCharset.equals(that.defaultFileCharset)) return false;
		if (!gitPath.equals(that.gitPath)) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = gitPath.hashCode();
		result = 31 * result + (defaultFileCharset.hashCode());
		return result;
	}

	@Override public String toString() {
		return "GitSettings{" +
				"gitPath='" + gitPath + '\'' +
				", defaultFileCharset=" + defaultFileCharset +
				'}';
	}
}
