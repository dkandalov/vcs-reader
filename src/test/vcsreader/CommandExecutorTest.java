package vcsreader;

import org.junit.Test;
import vcsreader.lang.Async;
import vcsreader.lang.CommandExecutor;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static vcsreader.lang.CommandExecutor.Command;

public class CommandExecutorTest {

    @Test public void executeSuccessfulCommand() {
        CommandExecutor executor = new CommandExecutor();
        Async<String> async = executor.execute(new SuccessfulCommand());

        assertThat(async.awaitResult(), equalTo("completed"));
        assertThat(async.isComplete(), is(true));
        assertThat(async.hasExceptions(), is(false));
    }

    @Test public void executeFailedCommand() {
        CommandExecutor executor = new CommandExecutor();
        Async<String> async = executor.execute(new FailedCommand());

        async.awaitCompletion();
        assertThat(async.isComplete(), is(true));
        assertThat(async.hasExceptions(), is(true));
    }

    @Test public void notifyListenerAboutCommands() {
        final AtomicReference<Command<?>> reference = new AtomicReference<Command<?>>();
        CommandExecutor executor = new CommandExecutor(new CommandExecutor.Listener() {
            @Override public void onCommand(Command command) {
                reference.set(command);
            }
        });
        Async<String> async = executor.execute(new SuccessfulCommand());

        async.awaitCompletion();
        assertThat(reference.get(), instanceOf(SuccessfulCommand.class));
    }

    private static class SuccessfulCommand implements Command<String> {
        @Override public String execute() {
            return "completed";
        }
    }

    private static class FailedCommand implements Command<String> {
        @Override public String execute() {
            throw new RuntimeException();
        }
    }
}