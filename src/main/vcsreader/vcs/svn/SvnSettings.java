package vcsreader.vcs.svn;

import org.jetbrains.annotations.NotNull;
import vcsreader.lang.Charsets;

import java.nio.charset.Charset;

public class SvnSettings {
	@NotNull public final String svnPath;
	@NotNull public final Charset defaultFileCharset;
	public final boolean useMergeHistory;

	/**
	 * @param svnPath            path to svn executable
	 * @param defaultFileCharset default charset of files in repository
	 *                           (it will be used if charset could not be determined from file content)
	 * @param useMergeHistory    allows to see merge commits by using "--use-merge-history" flag,
	 *                           should be enabled for svn 1.7 and above
	 */
	public SvnSettings(@NotNull String svnPath, @NotNull Charset defaultFileCharset, boolean useMergeHistory) {
		this.svnPath = svnPath;
		this.defaultFileCharset = defaultFileCharset;
		this.useMergeHistory = useMergeHistory;
	}

	public static SvnSettings defaults() {
		return new SvnSettings("svn", Charsets.UTF8, true);
	}

	public SvnSettings withSvnPath(String value) {
		return new SvnSettings(value, defaultFileCharset, useMergeHistory);
	}

	public SvnSettings withDefaultFileCharset(Charset value) {
		return new SvnSettings(svnPath, value, useMergeHistory);
	}

	public SvnSettings withMergeHistory(boolean value) {
		return new SvnSettings(svnPath, defaultFileCharset, value);
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnSettings that = (SvnSettings) o;

		if (useMergeHistory != that.useMergeHistory) return false;
		if (!defaultFileCharset.equals(that.defaultFileCharset)) return false;
		if (!svnPath.equals(that.svnPath)) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = svnPath.hashCode();
		result = 31 * result + (defaultFileCharset.hashCode());
		result = 31 * result + (useMergeHistory ? 1 : 0);
		return result;
	}

	@Override public String toString() {
		return "SvnSettings{" +
				"svnPath='" + svnPath + '\'' +
				", defaultFileCharset=" + defaultFileCharset +
				", useMergeHistory=" + useMergeHistory +
				'}';
	}
}
