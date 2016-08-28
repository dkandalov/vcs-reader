package org.vcsreader.vcs.commandlistener;

import java.util.List;

import static org.vcsreader.vcs.commandlistener.VcsCommand.Listener.executeWith;

// TODO move to a different package
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


	class Failure extends RuntimeException {
		private final List<String> vcsErrors;

		public Failure(List<String> vcsErrors) {
			this.vcsErrors = vcsErrors;
		}

		@Override public String getMessage() {
			return String.join("\n", vcsErrors);
		}
	}

	static <T> T execute(VcsCommand<T> vcsCommand, ResultAdapter<T> resultAdapter, VcsCommand.Listener listener, boolean isFailFast) {
		T result;
		try {
			result = executeWith(listener, vcsCommand);
		} catch (Exception e) {
			if (isFailFast) {
				throw e;
			} else {
				return resultAdapter.wrapException(e);
			}
		}
		if (isFailFast && !resultAdapter.isSuccessful(result)) {
			throw new VcsCommand.Failure(resultAdapter.vcsErrorsIn(result));
		}
		return result;
	}

	interface ResultAdapter<T> {
		T wrapException(Exception e);

		boolean isSuccessful(T result);

		List<String> vcsErrorsIn(T result);
	}
}
