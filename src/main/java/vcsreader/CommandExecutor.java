package vcsreader;

import vcsreader.lang.Async;
import vcsreader.lang.NamedThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CommandExecutor {
    private final Executor executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("CommandExecutor-"));

    public Async<Result> execute(final Command command) {
        final Async<Result> asyncResult = new Async<Result>();
        executor.execute(new Runnable() {
            @Override public void run() {
                try {

                    Result result = command.execute();
                    asyncResult.completeWith(result);

                } catch (Exception e) {
                    e.printStackTrace(); // TODO
                }
            }
        });
        return asyncResult;
    }

    public interface Command {
        Result execute();
    }

    public static interface Result {
        Result mergeWith(Result result);
    }
}
