package org.vcsreader.vcs.svn;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SvnSettings {
	@NotNull private final String svnPath;
	@NotNull private final Charset defaultFileCharset;
	private final boolean useMergeHistory;
	private final boolean failFast;

	/**
	 * @param svnPath            path to svn executable
	 * @param defaultFileCharset default charset of files in repository
	 *                           (it will be used if charset could not be determined from file content)
	 * @param useMergeHistory    allows to see merge commits by using "--use-merge-history" flag,
	 *                           should be enabled for svn 1.7 and above
	 * @param failFast           if true, will throw an exception command line execution failure;
	 *                           otherwise will aggregate all exceptions/errors into result object
	 */
	public SvnSettings(@NotNull String svnPath, @NotNull Charset defaultFileCharset, boolean useMergeHistory, boolean failFast) {
		this.svnPath = svnPath;
		this.defaultFileCharset = defaultFileCharset;
		this.useMergeHistory = useMergeHistory;
		this.failFast = failFast;
	}

	public static SvnSettings defaults() {
		return new SvnSettings("svn", UTF_8, true, true);
	}

	public SvnSettings withSvnPath(String value) {
		return new SvnSettings(value, defaultFileCharset, useMergeHistory, failFast);
	}

	public SvnSettings withDefaultFileCharset(Charset value) {
		return new SvnSettings(svnPath, value, useMergeHistory, failFast);
	}

	public SvnSettings withMergeHistory(boolean value) {
		return new SvnSettings(svnPath, defaultFileCharset, value, failFast);
	}

	public SvnSettings withFailFast(boolean value) {
		return new SvnSettings(svnPath, defaultFileCharset, useMergeHistory, value);
	}

	@NotNull public String svnPath() {
		return svnPath;
	}

	@NotNull public Charset defaultFileCharset() {
		return defaultFileCharset;
	}

	public boolean useMergeHistory() {
		return useMergeHistory;
	}

	public boolean failFast() {
		return failFast;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnSettings that = (SvnSettings) o;

		return useMergeHistory == that.useMergeHistory &&
				failFast == that.failFast &&
				svnPath.equals(that.svnPath) &&
				defaultFileCharset.equals(that.defaultFileCharset);
	}

	@Override public int hashCode() {
		int result = svnPath.hashCode();
		result = 31 * result + defaultFileCharset.hashCode();
		result = 31 * result + (useMergeHistory ? 1 : 0);
		result = 31 * result + (failFast ? 1 : 0);
		return result;
	}

	@Override public String toString() {
		return "SvnSettings{" +
				"svnPath='" + svnPath + '\'' +
				", defaultFileCharset=" + defaultFileCharset +
				", useMergeHistory=" + useMergeHistory +
				", failFast=" + failFast +
				'}';
	}
}
