package vcsreader.vcs.hg;

import vcsreader.lang.Charsets;

import java.nio.charset.Charset;

public class HgSettings {
    public final String hgPath;
    public final Charset filesCharset;

	/**
	 * @param hgPath path to hg executable
	 * @param filesCharset default charset of files in repository
	 *                     (it will be used if charset could not be determined from file content)
	 */
    public HgSettings(String hgPath, Charset filesCharset) {
        this.hgPath = hgPath;
        this.filesCharset = filesCharset;
    }

    public static HgSettings defaults() {
        return new HgSettings("hg", Charsets.UTF8);
    }

    public HgSettings withHgPath(String value) {
        return new HgSettings(value, filesCharset);
    }

    public HgSettings withFilesCharset(Charset value) {
        return new HgSettings(hgPath, value);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HgSettings that = (HgSettings) o;

        if (hgPath != null ? !hgPath.equals(that.hgPath) : that.hgPath != null) return false;
        return !(filesCharset != null ? !filesCharset.equals(that.filesCharset) : that.filesCharset != null);
    }

    @Override public int hashCode() {
        int result = hgPath != null ? hgPath.hashCode() : 0;
        result = 31 * result + (filesCharset != null ? filesCharset.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "HgSettings{" +
                "hgPath='" + hgPath + '\'' +
                ", filesCharset=" + filesCharset +
                '}';
    }
}
