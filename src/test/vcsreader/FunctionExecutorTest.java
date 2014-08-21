package vcsreader;

import org.junit.Test;
import vcsreader.lang.Async;
import vcsreader.lang.FunctionExecutor;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static vcsreader.lang.FunctionExecutor.Function;

public class FunctionExecutorTest {

    @Test public void executeSuccessfulFunction() {
        FunctionExecutor executor = new FunctionExecutor();
        Async<String> async = executor.execute(new SuccessfulFunction());

        assertThat(async.awaitResult(), equalTo("completed"));
        assertThat(async.isComplete(), is(true));
        assertThat(async.hasExceptions(), is(false));
    }

    @Test public void executeFailedFunction() {
        FunctionExecutor executor = new FunctionExecutor();
        Async<String> async = executor.execute(new FailedFunction());

        async.awaitCompletion();
        assertThat(async.isComplete(), is(true));
        assertThat(async.hasExceptions(), is(true));
    }

    @Test public void notifyListenerAboutFunctions() {
        final AtomicReference<Function<?>> reference = new AtomicReference<Function<?>>();
        FunctionExecutor executor = new FunctionExecutor(new FunctionExecutor.Listener() {
            @Override public void onFunctionCall(Function function) {
                reference.set(function);
            }
        });
        Async<String> async = executor.execute(new SuccessfulFunction());

        async.awaitCompletion();
        assertThat(reference.get(), instanceOf(SuccessfulFunction.class));
    }

    private static class SuccessfulFunction implements Function<String> {
        @Override public String execute() {
            return "completed";
        }
    }

    private static class FailedFunction implements Function<String> {
        @Override public String execute() {
            throw new RuntimeException();
        }
    }
}