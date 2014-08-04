package vcsreader;

import org.junit.Test;
import vcsreader.vcs.GitClone;
import vcsreader.vcs.GitLog;
import vcsreader.vcs.GitVcsRoot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static vcsreader.CommandExecutor.Command;
import static vcsreader.CommandExecutor.Result;
import static vcsreader.VcsProject.InitResult;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.lang.DateTimeUtil.date;

public class VcsProjectTest {
    private final Result successfulResult = new Result(true);

    @Test public void successfulProjectInitialization() {
        // given
        FakeCommandExecutor fakeExecutor = new FakeCommandExecutor();
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url"),
                new GitVcsRoot("/local/path2", "git://some/url")
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);

        // when
        InitResult initResult = project.init();

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<Command>asList(
                new GitClone("git://some/url", "/local/path"),
                new GitClone("git://some/url", "/local/path2")
        )));
        assertThat(initResult.isComplete(), equalTo(false));

        // then
        fakeExecutor.completeAllCallsWith(successfulResult);
        assertThat(initResult.isComplete(), equalTo(true));
    }

    @Test public void successfullyLogProjectHistory() {
        // given
        FakeCommandExecutor fakeExecutor = new FakeCommandExecutor();
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url"),
                new GitVcsRoot("/local/path2", "git://some/url")
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);

        // when
        LogResult logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<Command>asList(
                new GitLog("/local/path", date("01/07/2014"), date("08/07/2014")),
                new GitLog("/local/path2", date("01/07/2014"), date("08/07/2014"))
        )));
        assertThat(logResult.isComplete(), equalTo(false));

        // then
        fakeExecutor.completeAllCallsWith(successfulResult);
        assertThat(logResult.isComplete(), equalTo(true));
    }


    private static class FakeCommandExecutor extends CommandExecutor {
        private final List<AsyncResult> asyncResults = new ArrayList<AsyncResult>();
        public final List<Command> commands = new ArrayList<Command>();

        @Override public AsyncResult execute(Command command) {
            commands.add(command);

            AsyncResult asyncResult = new AsyncResult();
            asyncResults.add(asyncResult);
            return asyncResult;
        }

        public void completeAllCallsWith(Result result) {
            for (AsyncResult asyncResult : asyncResults) {
                asyncResult.completeWith(result);
            }
        }
    }
}
