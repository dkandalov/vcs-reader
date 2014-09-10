package vcsreader;

import org.junit.Test;
import vcsreader.lang.VcsCommand;
import vcsreader.lang.VcsCommandExecutor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static vcsreader.lang.VcsCommandExecutor.Listener;

public class VcsCommandExecutorTest {
    @Test public void notifyListenerAboutFunctions() {
        final VcsCommand[] vcsCommand = new VcsCommand[1];
        VcsCommandExecutor executor = new VcsCommandExecutor(new Listener() {
            @Override public void onFunctionCall(VcsCommand function) {
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