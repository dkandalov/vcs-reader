package org.vcsreader.vcs.hg;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.lang.CharsetUtil;

import java.nio.charset.Charset;

public class HgSettings {
	@NotNull public final String hgPath;
	@NotNull public final Charset defaultFileCharset;
	public final boolean failFast;


	public HgSettings(@NotNull String hgPath, @NotNull Charset defaultFileCharset) {
		this(hgPath, defaultFileCharset, false);
	}

	/**
	 * @param hgPath             path to hg executable
	 * @param defaultFileCharset default charset of files in repository
	 *                           (it will be used if charset could not be determined from file content)
	 * @param failFast           if true, will throw an exception command line execution failure;
	 *                           otherwise will aggregate all exceptions/errors into result object
	 */
	public HgSettings(@NotNull String hgPath, @NotNull Charset defaultFileCharset, boolean failFast) {
		this.hgPath = hgPath;
		this.defaultFileCharset = defaultFileCharset;
		this.failFast = failFast;
	}

	public static HgSettings defaults() {
		return new HgSettings("hg", CharsetUtil.UTF8);
	}

	public HgSettings withHgPath(String value) {
		return new HgSettings(value, defaultFileCharset);
	}

	public HgSettings withDefaultFileCharset(Charset value) {
		return new HgSettings(hgPath, value);
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HgSettings that = (HgSettings) o;

		return failFast == that.failFast &&
				hgPath.equals(that.hgPath) &&
				defaultFileCharset.equals(that.defaultFileCharset);
	}

	@Override public int hashCode() {
		int result = hgPath.hashCode();
		result = 31 * result + defaultFileCharset.hashCode();
		result = 31 * result + (failFast ? 1 : 0);
		return result;
	}

	@Override public String toString() {
		return "HgSettings{" +
				"hgPath='" + hgPath + '\'' +
				", defaultFileCharset=" + defaultFileCharset +
				", failFast=" + failFast +
				'}';
	}
}
