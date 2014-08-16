package vcsreader;

import org.junit.Test;
import vcsreader.lang.Async;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static vcsreader.CommandExecutor.Command;

public class CommandExecutorTest {

    @Test public void executeSuccessfulCommand() {
        CommandExecutor executor = new CommandExecutor();
        Async<String> async = executor.execute(successfulCommand);

        assertThat(async.awaitCompletion(), equalTo("completed"));
        assertThat(async.isComplete(), is(true));
        assertThat(async.hasException(), is(false));
    }

    @Test public void executeFailedCommand() {
        CommandExecutor executor = new CommandExecutor();
        Async<String> async = executor.execute(failedCommand);

        assertThat(async.awaitCompletion(), equalTo(null));
        assertThat(async.isComplete(), is(true));
        assertThat(async.hasException(), is(true));
    }

    private final Command<String> successfulCommand = new Command<String>() {
        @Override public String execute() {
            return "completed";
        }
    };

    private final Command<String> failedCommand = new Command<String>() {
        @Override public String execute() {
            throw new RuntimeException();
        }
    };
}