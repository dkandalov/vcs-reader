package org.vcsreader.vcs.commandlistener;

import java.util.ArrayList;
import java.util.List;

public class VcsCommandExecutor {
	private final List<Listener> listeners = new ArrayList<>();

	public <T> T executeAndObserve(final VcsCommand<T> command) {
		for (Listener listener : listeners) {
			listener.beforeCommand(command);
		}
		try {

			return command.execute();

		} catch (Exception e) {
			throw new RuntimeException("Failed to execute " + command.describe(), e);
		} finally {
			for (Listener listener : listeners) {
				listener.afterCommand(command);
			}
		}
	}

	public VcsCommandExecutor add(Listener listener) {
		listeners.add(listener);
		return this;
	}

	public VcsCommandExecutor remove(Listener listener) {
		listeners.remove(listener);
		return this;
	}


	public interface Listener {
		void beforeCommand(VcsCommand<?> command);

		void afterCommand(VcsCommand<?> command);
	}
}
