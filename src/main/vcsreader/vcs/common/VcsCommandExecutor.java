package vcsreader.vcs.common;

import java.util.ArrayList;
import java.util.List;

public class VcsCommandExecutor {
    private final List<Listener> listeners = new ArrayList<Listener>();

    public VcsCommandExecutor add(Listener listener) {
        listeners.add(listener);
        return this;
    }

    public <T> T execute(final VcsCommand<T> command) {
        for (Listener listener : listeners) {
            listener.onCommand(command);
        }
        return command.execute();
    }

    public interface Listener {
        void onCommand(VcsCommand command);
    }
}
