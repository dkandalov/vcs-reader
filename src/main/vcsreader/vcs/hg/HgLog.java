package vcsreader.vcs.hg;

import vcsreader.lang.ShellCommand;

import java.util.Date;

public class HgLog {
    static ShellCommand hgLog(String hgPath, String folder, Date fromDate, Date toDate) {
        return new ShellCommand(hgPath, "log").workingDir(folder);
    }
}
