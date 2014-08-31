package vcsreader.vcs.svn;

public class SvnSettings {
    public final String svnPath;

    public SvnSettings(String svnPath) {
        this.svnPath = svnPath;
    }

    public static SvnSettings defaults() {
        return new SvnSettings("/usr/bin/svn");
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SvnSettings that = (SvnSettings) o;

        return !(svnPath != null ? !svnPath.equals(that.svnPath) : that.svnPath != null);
    }

    @Override public int hashCode() {
        return svnPath != null ? svnPath.hashCode() : 0;
    }
}
