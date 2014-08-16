package vcsreader;

import vcsreader.lang.Async;
import vcsreader.lang.NamedThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;

public class CommandExecutor {
    private final Executor executor;
    private final Listener listener;

    public CommandExecutor() {
        this("CommandExecutor-", dummyListener());
    }

    public CommandExecutor(Listener listener) {
        this("CommandExecutor-", listener);
    }

    public CommandExecutor(String threadNamePrefix, Listener listener) {
        this.listener = listener;
        executor = Executors.newFixedThreadPool(10, new NamedThreadFactory(threadNamePrefix));
    }

    public <T> Async<T> execute(final Command<T> command) {
        final Async<T> asyncResult = new Async<T>();
        executor.execute(new Runnable() {
            @Override public void run() {
                try {
                    listener.onCommand(command);

                    T result = command.execute();
                    asyncResult.completeWith(result);

                } catch (Exception e) {
                    asyncResult.completeWithFailure(asList(e));
                }
            }
        });
        return asyncResult;
    }

    private static Listener dummyListener() {
        return new Listener() {
            @Override public void onCommand(Command command) {}
        };
    }

    public interface Command<T> {
        T execute();
    }

    public interface Listener {
        void onCommand(Command command);
    }
}
