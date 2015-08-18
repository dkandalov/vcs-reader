package vcsreader.vcs.commandlistener;

import java.util.ArrayList;
import java.util.List;

public class VcsCommandObserver {
    private final List<VcsCommandListener> listeners = new ArrayList<VcsCommandListener>();

    public VcsCommandObserver add(VcsCommandListener listener) {
        listeners.add(listener);
        return this;
    }

    public <T> T executeAndObserve(final VcsCommand<T> command) {
        for (VcsCommandListener listener : listeners) {
            listener.beforeCommand(command);
        }

        T result = command.execute();

        for (VcsCommandListener listener : listeners) {
            listener.afterCommand(command);
        }
        return result;
    }
}
