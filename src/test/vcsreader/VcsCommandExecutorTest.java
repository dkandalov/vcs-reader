package vcsreader;

import org.junit.Test;
import vcsreader.lang.Async;
import vcsreader.lang.VcsCommandExecutor;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static vcsreader.lang.VcsCommandExecutor.VcsCommand;

public class VcsCommandExecutorTest {

    @Test public void executeSuccessfulFunction() {
        VcsCommandExecutor executor = new VcsCommandExecutor();
        Async<String> async = executor.execute(new SuccessfulVcsCommand());

        assertThat(async.awaitResult(), equalTo("completed"));
        assertThat(async.isComplete(), is(true));
        assertThat(async.hasExceptions(), is(false));
    }

    @Test public void executeFailedFunction() {
        VcsCommandExecutor executor = new VcsCommandExecutor();
        Async<String> async = executor.execute(new FailedVcsCommand());

        async.awaitCompletion();
        assertThat(async.isComplete(), is(true));
        assertThat(async.hasExceptions(), is(true));
    }

    @Test public void notifyListenerAboutFunctions() {
        final AtomicReference<VcsCommandExecutor.VcsCommand<?>> reference = new AtomicReference<VcsCommandExecutor.VcsCommand<?>>();
        VcsCommandExecutor executor = new VcsCommandExecutor(new VcsCommandExecutor.Listener() {
            @Override public void onFunctionCall(VcsCommand function) {
                reference.set(function);
            }
        });
        Async<String> async = executor.execute(new SuccessfulVcsCommand());

        async.awaitCompletion();
        assertThat(reference.get(), instanceOf(SuccessfulVcsCommand.class));
    }

    private static class SuccessfulVcsCommand implements VcsCommandExecutor.VcsCommand<String> {
        @Override public String execute() {
            return "completed";
        }

        @Override public String describe() {
            return "";
        }
    }

    private static class FailedVcsCommand implements VcsCommandExecutor.VcsCommand<String> {
        @Override public String execute() {
            throw new RuntimeException();
        }

        @Override public String describe() {
            return "";
        }
    }
}