package vcsreader;

import vcsreader.lang.NamedThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CommandExecutor {
    private final Executor executor = Executors.newFixedThreadPool(10, new NamedThreadFactory());

    public AsyncResult execute(final Command command) {
        final AsyncResult asyncResult = new AsyncResult();
        executor.execute(new Runnable() {
            @Override public void run() {

                Result result = command.execute();
                asyncResult.completeWith(result);

            }
        });
        return asyncResult;
    }

    public interface Command {
        Result execute();
    }

    public static class Result {
        public final boolean successful;

        public Result(boolean successful) {
            this.successful = successful;
        }
    }

    public static class AsyncResult {
        private AsyncResultListener whenReadyCallback;

        public void whenCompleted(AsyncResultListener listener) {
            this.whenReadyCallback = listener;
        }

        public void completeWith(Result result) {
            whenReadyCallback.onComplete(result);
        }
    }

    public static interface AsyncResultListener {
        void onComplete(Result result);
    }

}
