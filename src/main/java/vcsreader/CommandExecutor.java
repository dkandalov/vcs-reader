package vcsreader;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CommandExecutor {
    private final Executor executor = Executors.newFixedThreadPool(10);

    public AsyncResult execute(final Command command) {
        final AsyncResult asyncResult = new AsyncResult();
        executor.execute(new Runnable() {
            @Override public void run() {

                Result result = command.execute();
                if (result.exitValue == 0) {
                    asyncResult.succeed(result);
                } else {
                    asyncResult.fail(result);
                }

            }
        });
        return asyncResult;
    }

    public interface Command {
        Result execute();
    }

    public static class Result {
        public final String stdout;
        public final String stderr;
        public final int exitValue;

        public Result(String stdout, String stderr, int exitValue) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitValue = exitValue;
        }
    }

    public static class AsyncResult {
        private Runnable whenReadyCallback;

        public void whenReady(Runnable runnable) {
            this.whenReadyCallback = runnable;
        }

        public void succeed(Result result) {
            whenReadyCallback.run();
        }

        public void fail(Result result) {
            // TODO implement

        }
    }
}
