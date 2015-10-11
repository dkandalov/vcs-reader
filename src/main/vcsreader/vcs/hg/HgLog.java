package vcsreader.vcs.hg;

import vcsreader.lang.ShellCommand;

import java.util.Date;

import static java.nio.charset.Charset.forName;

class HgLog {
    static ShellCommand hgLog(String hgPath, String folder, Date fromDate, Date toDate) {
        ShellCommand command = new ShellCommand(hgPath, "log", "--encoding", "UTF-8");
        return command.workingDir(folder).withCharset(forName("UTF-8"));
    }
}
