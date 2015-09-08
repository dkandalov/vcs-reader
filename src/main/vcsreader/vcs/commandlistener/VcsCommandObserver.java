package vcsreader.vcs.commandlistener;

import java.util.ArrayList;
import java.util.List;

public class VcsCommandObserver {
    private final List<VcsCommandListener> listeners = new ArrayList<VcsCommandListener>();

    public <T> T executeAndObserve(final VcsCommand<T> command) {
        for (VcsCommandListener listener : listeners) {
            listener.beforeCommand(command);
        }

        T result;
        try {
            result = command.execute();
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute " + command.describe(), e);
        }

        for (VcsCommandListener listener : listeners) {
            listener.afterCommand(command);
        }
        return result;
    }

    public VcsCommandObserver add(VcsCommandListener listener) {
        listeners.add(listener);
        return this;
    }

    public VcsCommandObserver remove(VcsCommandListener listener) {
        listeners.remove(listener);
        return this;
    }
}
