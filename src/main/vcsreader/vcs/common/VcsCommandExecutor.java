package vcsreader.vcs.common;

public class VcsCommandExecutor {
    public static final Listener dummyListener = new Listener() {
        @Override public void onCommand(VcsCommand command) {}
    };
    private final Listener listener;

    public VcsCommandExecutor() {
        this(dummyListener);
    }

    public VcsCommandExecutor(Listener listener) {
        this.listener = listener;
    }

    public <T> T execute(final VcsCommand<T> command) {
        listener.onCommand(command);
        return command.execute();
    }

    public interface Listener {
        void onCommand(VcsCommand command);
    }
}
