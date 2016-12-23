package org.vcsreader.vcs;

import static org.vcsreader.vcs.VcsCommand.Listener.executeWith;

public interface VcsCommand<R> {

	String describe();
	R execute();


	interface Observer {
		Observer withListener(Listener listener);
	}

	interface ExceptionWrapper<T> {
		T wrapAsResult(Exception e);
	}


	interface Listener {
		void beforeCommand(VcsCommand<?> command);
		void afterCommand(VcsCommand<?> command);

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
	}


	static <T> T execute(VcsCommand<T> vcsCommand, ExceptionWrapper<T> exceptionWrapper, VcsCommand.Listener listener, boolean isFailFast) {
		try {
			return executeWith(listener, vcsCommand);
		} catch (Exception e) {
			if (isFailFast) {
				throw e;
			} else {
				return exceptionWrapper.wrapAsResult(e);
			}
		}
	}
}
