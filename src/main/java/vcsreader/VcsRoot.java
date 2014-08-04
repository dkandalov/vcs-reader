package vcsreader;

import java.util.Date;

import static vcsreader.CommandExecutor.AsyncResult;

public interface VcsRoot {
    AsyncResult init(CommandExecutor commandExecutor);

    AsyncResult log(CommandExecutor commandExecutor, Date from, Date to);
}
