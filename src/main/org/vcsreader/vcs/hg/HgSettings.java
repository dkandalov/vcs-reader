package org.vcsreader.vcs.hg;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HgSettings {
	private final String hgPath;
	private final Charset defaultFileCharset;
	private final boolean failFast;


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
		return new HgSettings("hg", UTF_8, true);
	}

	public HgSettings withHgPath(String value) {
		return new HgSettings(value, defaultFileCharset, failFast);
	}

	public HgSettings withDefaultFileCharset(Charset value) {
		return new HgSettings(hgPath, value, failFast);
	}

	public HgSettings withFailFast(boolean value) {
		return new HgSettings(hgPath, defaultFileCharset, value);
	}

	@NotNull public String hgPath() {
		return hgPath;
	}

	@NotNull public Charset defaultFileCharset() {
		return defaultFileCharset;
	}

	public boolean failFast() {
		return failFast;
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
