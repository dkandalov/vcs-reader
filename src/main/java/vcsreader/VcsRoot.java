package vcsreader;

import vcsreader.lang.Async;

import java.util.Date;

public interface VcsRoot {
    Async<CommandExecutor.Result> init(CommandExecutor commandExecutor);

    Async<CommandExecutor.Result> log(CommandExecutor commandExecutor, Date from, Date to);
}
