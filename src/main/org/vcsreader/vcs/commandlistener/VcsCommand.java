package org.vcsreader.vcs.commandlistener;

public interface VcsCommand<R> {
	String describe();

	R execute();


	interface Listener {
		Listener none = new Listener() {
			@Override public void beforeCommand(VcsCommand<?> command) {}
			@Override public void afterCommand(VcsCommand<?> command) {}
		};

		static <T> T executeWith(Listener listener, VcsCommand<T> command) {
			listener.beforeCommand(command);
			try {

				return command.execute();

			} catch (Exception e) {
				throw new RuntimeException("Failed to execute " + command.describe(), e);
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
}
