package org.vcsreader.vcs.git;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.lang.CharsetUtil;

import java.nio.charset.Charset;

public class GitSettings {
	@NotNull public final String gitPath;
	@NotNull public final Charset defaultFileCharset;
	public final boolean failFast;

	// TODO inline
	public GitSettings(@NotNull String gitPath, @NotNull Charset defaultFileCharset) {
		this(gitPath, defaultFileCharset, false);
	}

	/**
	 * @param gitPath            path to git executable
	 * @param defaultFileCharset default charset of files in repository
	 *                           (it will be used if charset could not be determined from file content)
	 * @param failFast           if true, will throw an exception command line execution failure;
	 *                           otherwise will aggregate all exceptions/errors into result object
	 */
	public GitSettings(@NotNull String gitPath, @NotNull Charset defaultFileCharset, boolean failFast) {
		this.gitPath = gitPath;
		this.defaultFileCharset = defaultFileCharset;
		this.failFast = failFast;
	}

	public static GitSettings defaults() {
		return new GitSettings("git", CharsetUtil.UTF8, false);
	}

	public GitSettings withGitPath(String value) {
		return new GitSettings(value, defaultFileCharset);
	}

	public GitSettings withDefaultFileCharset(Charset value) {
		return new GitSettings(gitPath, value);
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitSettings that = (GitSettings) o;

		return failFast == that.failFast &&
				gitPath.equals(that.gitPath) &&
				defaultFileCharset.equals(that.defaultFileCharset);
	}

	@Override public int hashCode() {
		int result = gitPath.hashCode();
		result = 31 * result + defaultFileCharset.hashCode();
		result = 31 * result + (failFast ? 1 : 0);
		return result;
	}

	@Override public String toString() {
		return "GitSettings{" +
				"gitPath='" + gitPath + '\'' +
				", defaultFileCharset=" + defaultFileCharset +
				", failFast=" + failFast +
				'}';
	}
}
