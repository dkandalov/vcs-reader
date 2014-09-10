package vcsreader;

import vcsreader.lang.VcsCommandExecutor;

import java.util.Date;

import static vcsreader.VcsProject.*;

public interface VcsRoot {
    InitResult init();

    UpdateResult update();

    LogResult log(Date fromDate, Date toDate);

    LogContentResult contentOf(String filePath, String revision);

    public interface WithExecutor {
        void setExecutor(VcsCommandExecutor commandExecutor);
    }
}
