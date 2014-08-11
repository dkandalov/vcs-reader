package vcsreader;

import vcsreader.lang.Async;

import java.util.Date;

public interface VcsRoot {
    Async<CommandExecutor.Result> init(CommandExecutor commandExecutor);

    Async<CommandExecutor.Result> log(CommandExecutor commandExecutor, Date fromDate, Date toDate);

    Async<CommandExecutor.Result> contentOf(String fileName, String revision);
}
