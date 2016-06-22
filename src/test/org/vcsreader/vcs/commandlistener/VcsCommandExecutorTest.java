package org.vcsreader.vcs.commandlistener;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class VcsCommandExecutorTest {
	@Test public void notifyListenerAboutVcsCommands() {
		final VcsCommand[] onBeforeCommands = new VcsCommand[1];
		final VcsCommand[] onAfterCommands = new VcsCommand[1];
		VcsCommandExecutor commandExecutor = new VcsCommandExecutor().add(new VcsCommandListener() {
			@Override public void beforeCommand(VcsCommand<?> command) {
				onBeforeCommands[0] = command;
			}
			@Override public void afterCommand(VcsCommand<?> command) {
				onAfterCommands[0] = command;
			}
		});
		commandExecutor.executeAndObserve(new SuccessfulVcsCommand());

		assertThat(onBeforeCommands[0], instanceOf(SuccessfulVcsCommand.class));
		assertThat(onAfterCommands[0], instanceOf(SuccessfulVcsCommand.class));
	}

	private static class SuccessfulVcsCommand implements VcsCommand<String> {
		@Override public String execute() {
			return "completed";
		}

		@Override public String describe() {
			return "";
		}
	}
}