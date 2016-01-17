package vcsreader;

import vcsreader.vcs.commandlistener.VcsCommandObserver;

import java.util.Date;

import static vcsreader.VcsProject.*;

public interface VcsRoot {
    CloneResult cloneToLocal();

    UpdateResult update();

    LogResult log(Date fromDate, Date toDate);

    LogFileContentResult contentOf(String filePath, String revision);

    interface WithCommandObserver {
        void setObserver(VcsCommandObserver commandExecutor);
    }
}
