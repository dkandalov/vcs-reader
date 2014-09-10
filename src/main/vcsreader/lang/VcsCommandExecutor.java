package vcsreader.lang;

public class VcsCommandExecutor {
    private final Listener listener;

    public VcsCommandExecutor() {
        this(dummyListener());
    }

    public VcsCommandExecutor(Listener listener) {
        this.listener = listener;
    }

    public <T> T execute(final VcsCommand<T> command) {
        listener.onFunctionCall(command);
        return command.execute();
    }

    private static Listener dummyListener() {
        return new Listener() {
            @Override public void onFunctionCall(VcsCommand command) {}
        };
    }

    public interface Listener {
        void onFunctionCall(VcsCommand command);
    }
}
