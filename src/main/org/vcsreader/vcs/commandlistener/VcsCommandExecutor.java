package org.vcsreader.vcs.commandlistener;

import java.util.ArrayList;
import java.util.List;

public class VcsCommandExecutor {
	private final List<VcsCommandListener> listeners = new ArrayList<>();

	public <T> T executeAndObserve(final VcsCommand<T> command) {
		for (VcsCommandListener listener : listeners) {
			listener.beforeCommand(command);
		}
		try {

			return command.execute();

		} catch (Exception e) {
			throw new RuntimeException("Failed to execute " + command.describe(), e);
		} finally {
			for (VcsCommandListener listener : listeners) {
				listener.afterCommand(command);
			}
		}
	}

	public VcsCommandExecutor add(VcsCommandListener listener) {
		listeners.add(listener);
		return this;
	}

	public VcsCommandExecutor remove(VcsCommandListener listener) {
		listeners.remove(listener);
		return this;
	}
}
