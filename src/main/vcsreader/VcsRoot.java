package vcsreader;

import vcsreader.lang.Async;
import vcsreader.lang.CommandExecutor;

import java.util.Date;

import static vcsreader.VcsProject.InitResult;
import static vcsreader.VcsProject.UpdateResult;

public interface VcsRoot {
    Async<InitResult> init();

    Async<UpdateResult> update();

    Async<VcsProject.LogResult> log(Date fromDate, Date toDate);

    Async<VcsProject.LogContentResult> contentOf(String fileName, String revision);

    void setCommandExecutor(CommandExecutor commandExecutor);
}
