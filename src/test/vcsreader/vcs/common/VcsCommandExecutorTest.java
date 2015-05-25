package vcsreader.vcs.common;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static vcsreader.vcs.common.VcsCommandExecutor.Listener;

public class VcsCommandExecutorTest {
    @Test public void notifyListenerAboutFunctions() {
        final VcsCommand[] vcsCommand = new VcsCommand[1];
        VcsCommandExecutor executor = new VcsCommandExecutor().add(new Listener() {
            @Override public void onCommand(VcsCommand function) {
                vcsCommand[0] = function;
            }
        });
        executor.execute(new SuccessfulVcsCommand());

        assertThat(vcsCommand[0], instanceOf(SuccessfulVcsCommand.class));
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