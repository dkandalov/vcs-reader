package vcsreader.vcs.svn;

public class SvnSettings {
    public final String svnPath;

    public SvnSettings(String svnPath) {
        this.svnPath = svnPath;
    }

    public static SvnSettings defaults() {
        return new SvnSettings("/usr/bin/svn");
    }
}
