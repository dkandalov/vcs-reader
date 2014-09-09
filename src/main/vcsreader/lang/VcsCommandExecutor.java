package vcsreader.lang;

import static java.util.Arrays.asList;

public class VcsCommandExecutor {
    private final Listener listener;

    public VcsCommandExecutor() {
        this(dummyListener());
    }

    public VcsCommandExecutor(Listener listener) {
        this.listener = listener;
    }

    public <T> Async<T> execute(final VcsCommand<T> command) {
        Async<T> asyncResult = new Async<T>();
        try {
            listener.onFunctionCall(command);

            T result = command.execute();
            asyncResult.completeWith(result);

        } catch (Exception e) {
            asyncResult.completeWithFailure(asList(e));
        }
        return asyncResult;
    }

    private static Listener dummyListener() {
        return new Listener() {
            @Override public void onFunctionCall(VcsCommand command) {}
        };
    }

    public interface VcsCommand<R> {
        R execute();

        String describe();
    }

    public interface Listener {
        void onFunctionCall(VcsCommand command);
    }
}
