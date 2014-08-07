package vcsreader.vcs;

import org.junit.Test;
import vcsreader.CommandExecutor;
import vcsreader.VcsProject;
import vcsreader.VcsRoot;
import vcsreader.lang.Async;
import vcsreader.vcs.GitClone;
import vcsreader.vcs.GitLog;
import vcsreader.vcs.GitVcsRoot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static vcsreader.CommandExecutor.*;
import static vcsreader.VcsProject.InitResult;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.lang.DateTimeUtil.date;

public class VcsProjectTest {
    @Test public void successfulProjectInitialization() {
        // given
        FakeCommandExecutor fakeExecutor = new FakeCommandExecutor();
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url"),
                new GitVcsRoot("/local/path2", "git://some/url")
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);

        // when
        Async<InitResult> initResult = project.init();

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<Command>asList(
                new GitClone("git://some/url", "/local/path"),
                new GitClone("git://some/url", "/local/path2")
        )));
        assertThat(initResult.isComplete(), equalTo(false));

        // then
        fakeExecutor.completeAllCallsWith(mock(InitResult.class));
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
        Async<LogResult> logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<Command>asList(
                new GitLog("/local/path", date("01/07/2014"), date("08/07/2014")),
                new GitLog("/local/path2", date("01/07/2014"), date("08/07/2014"))
        )));
        assertThat(logResult.isComplete(), equalTo(false));

        // then
        fakeExecutor.completeAllCallsWith(mock(LogResult.class));
        assertThat(logResult.isComplete(), equalTo(true));
    }


    private static class FakeCommandExecutor extends CommandExecutor {
        private final List<Async<Result>> asyncResults = new ArrayList<Async<Result>>();
        public final List<Command> commands = new ArrayList<Command>();

        @Override public Async<Result> execute(Command command) {
            commands.add(command);

            Async<Result> asyncResult = new Async<Result>();
            asyncResults.add(asyncResult);
            return asyncResult;
        }

        public void completeAllCallsWith(Result result) {
            for (Async<Result> asyncResult : asyncResults) {
                asyncResult.completeWith(result);
            }
        }
    }
}
