package vcsreader.vcs.svn;

import java.nio.charset.Charset;

public class SvnSettings {
    public final String svnPath;
    public final Charset filesCharset;

    public SvnSettings(String svnPath, Charset filesCharset) {
        this.svnPath = svnPath;
        this.filesCharset = filesCharset;
    }

    public static SvnSettings defaults() {
        return new SvnSettings("/usr/bin/svn", Charset.forName("UTF-8"));
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SvnSettings that = (SvnSettings) o;

        if (filesCharset != null ? !filesCharset.equals(that.filesCharset) : that.filesCharset != null) return false;
        if (svnPath != null ? !svnPath.equals(that.svnPath) : that.svnPath != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = svnPath != null ? svnPath.hashCode() : 0;
        result = 31 * result + (filesCharset != null ? filesCharset.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "SvnSettings{" +
                "svnPath='" + svnPath + '\'' +
                ", filesCharset=" + filesCharset +
                '}';
    }
}
