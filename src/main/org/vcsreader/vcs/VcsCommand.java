package org.vcsreader.vcs;

import static org.vcsreader.vcs.VcsCommand.Listener.executeWith;

public interface VcsCommand<R> {

	String describe();

	R execute();


	interface Listener {
		Listener none = new Listener() {
			@Override public void beforeCommand(VcsCommand<?> command) {}
			@Override public void afterCommand(VcsCommand<?> command) {}
			@Override public String toString() { return "Listener.none"; }
		};

		static <T> T executeWith(Listener listener, VcsCommand<T> command) {
			listener.beforeCommand(command);
			try {

				return command.execute();

			} finally {
				listener.afterCommand(command);
			}
		}

		void beforeCommand(VcsCommand<?> command);
		void afterCommand(VcsCommand<?> command);
	}


	interface Owner {
		Owner withListener(Listener listener);
	}


	static <T> T execute(VcsCommand<T> vcsCommand, ResultAdapter<T> resultAdapter, VcsCommand.Listener listener, boolean isFailFast) {
		T result;
		try {
			result = executeWith(listener, vcsCommand);
		} catch (Exception e) {
			if (isFailFast) {
				throw e;
			} else {
				return resultAdapter.wrapAsResult(e);
			}
		}
		return result;
	}

	interface ResultAdapter<T> {
		T wrapAsResult(Exception e);

		boolean isSuccessful(T result);
	}
}
