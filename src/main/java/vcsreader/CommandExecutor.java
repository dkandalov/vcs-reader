package vcsreader;

import vcsreader.lang.Async;
import vcsreader.lang.NamedThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;

public class CommandExecutor {
    private final Executor executor;

    public CommandExecutor() {
        this("CommandExecutor-");
    }

    public CommandExecutor(String threadNamePrefix) {
        executor = Executors.newFixedThreadPool(10, new NamedThreadFactory(threadNamePrefix));
    }

    public <T> Async<T> execute(final Command<T> command) {
        final Async<T> asyncResult = new Async<T>();
        executor.execute(new Runnable() {
            @Override public void run() {
                try {

                    T result = command.execute();
                    asyncResult.completeWith(result);

                } catch (Exception e) {
                    asyncResult.completeWithFailure(asList(e));
                }
            }
        });
        return asyncResult;
    }

    public interface Command<T> {
        T execute();
    }
}
