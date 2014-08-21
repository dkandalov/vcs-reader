package vcsreader.lang;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;

public class FunctionExecutor {
    private final Executor executor;
    private final Listener listener;

    public FunctionExecutor() {
        this("FunctionExecutor-", dummyListener());
    }

    public FunctionExecutor(Listener listener) {
        this("FunctionExecutor-", listener);
    }

    public FunctionExecutor(String threadNamePrefix, Listener listener) {
        this.listener = listener;
        executor = Executors.newFixedThreadPool(10, new NamedThreadFactory(threadNamePrefix));
    }

    public <T> Async<T> execute(final Function<T> function) {
        final Async<T> asyncResult = new Async<T>();
        executor.execute(new Runnable() {
            @Override public void run() {
                try {
                    listener.onFunctionCall(function);

                    T result = function.execute();
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
            @Override public void onFunctionCall(Function function) {}
        };
    }

    public interface Function<R> {
        R execute();
    }

    public interface Listener {
        void onFunctionCall(Function function);
    }
}
