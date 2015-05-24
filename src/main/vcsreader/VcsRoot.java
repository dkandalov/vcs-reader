package vcsreader;

import vcsreader.vcs.common.VcsCommandExecutor;

import java.util.Date;

import static vcsreader.VcsProject.*;

public interface VcsRoot {
    CloneResult cloneToLocal();

    UpdateResult update();

    LogResult log(Date fromDate, Date toDate);

    LogContentResult contentOf(String filePath, String revision);

    interface WithExecutor {
        void setExecutor(VcsCommandExecutor commandExecutor);
    }
}
