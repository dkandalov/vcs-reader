package vcsreader;

import vcsreader.lang.Async;

import java.util.Date;

public interface VcsRoot {
    Async<CommandExecutor.Result> init();

    Async<CommandExecutor.Result> log(Date fromDate, Date toDate);

    Async<CommandExecutor.Result> contentOf(String fileName, String revision);

    void setCommandExecutor(CommandExecutor commandExecutor);
}
