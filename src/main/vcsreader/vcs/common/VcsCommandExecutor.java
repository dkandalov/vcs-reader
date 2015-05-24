package vcsreader.vcs.common;

public class VcsCommandExecutor {
    private static final Listener dummyListener = new Listener() {
        @Override public void onFunctionCall(VcsCommand command) {}
    };
    private final Listener listener;

    public VcsCommandExecutor() {
        this(dummyListener);
    }

    public VcsCommandExecutor(Listener listener) {
        this.listener = listener;
    }

    public <T> T execute(final VcsCommand<T> command) {
        listener.onFunctionCall(command);
        return command.execute();
    }

    public interface Listener {
        void onFunctionCall(VcsCommand command);
    }
}
