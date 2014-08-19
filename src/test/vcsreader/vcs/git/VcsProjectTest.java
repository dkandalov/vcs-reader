package vcsreader.vcs.git;

import org.junit.Test;
import vcsreader.VcsProject;
import vcsreader.VcsRoot;
import vcsreader.lang.Async;
import vcsreader.lang.CommandExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static vcsreader.VcsProject.InitResult;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.lang.CommandExecutor.Command;
import static vcsreader.lang.DateTimeUtil.date;

public class VcsProjectTest {
    private final GitSettings settings = GitSettings.defaults();
    private final FakeCommandExecutor fakeExecutor = new FakeCommandExecutor();

    @Test public void successfulProjectInitialization() {
        // given
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url", settings),
                new GitVcsRoot("/local/path2", "git://some/url", settings)
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);

        // when
        Async<InitResult> initResult = project.init();

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<Command>asList(
                new GitClone("/usr/bin/git", "git://some/url", "/local/path"),
                new GitClone("/usr/bin/git", "git://some/url", "/local/path2")
        )));
        assertThat(initResult.isComplete(), equalTo(false));

        // then
        fakeExecutor.completeAllCallsWith(mock(InitResult.class));
        assertThat(initResult.isComplete(), equalTo(true));
    }

    @Test public void successfulLogProjectHistory() {
        // given
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url", settings),
                new GitVcsRoot("/local/path2", "git://some/url", settings)
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);

        // when
        Async<LogResult> logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<Command>asList(
                new GitLog("/usr/bin/git", "/local/path", date("01/07/2014"), date("08/07/2014")),
                new GitLog("/usr/bin/git", "/local/path2", date("01/07/2014"), date("08/07/2014"))
        )));
        assertThat(logResult.isComplete(), equalTo(false));

        // then
        fakeExecutor.completeAllCallsWith(mock(LogResult.class));
        assertThat(logResult.isComplete(), equalTo(true));
    }

    @Test public void failedLogProjectHistory() {
        // given
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url", settings),
                new GitVcsRoot("/local/path2", "git://some/url", settings)
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);

        // when
        Async<LogResult> logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        fakeExecutor.failAllCallsWith(new Exception());
        assertThat(logResult.isComplete(), equalTo(true));
        assertThat(logResult.hasExceptions(), equalTo(true));
    }


    private static class FakeCommandExecutor extends CommandExecutor {
        private final List<Async<Object>> asyncResults = new ArrayList<Async<Object>>();
        public final List<Command> commands = new ArrayList<Command>();

        @SuppressWarnings("unchecked")
        @Override public Async<Object> execute(Command command) {
            commands.add(command);

            Async<Object> asyncResult = new Async<Object>();
            asyncResults.add(asyncResult);
            return asyncResult;
        }

        public void completeAllCallsWith(Object result) {
            for (Async<Object> asyncResult : asyncResults) {
                asyncResult.completeWith(result);
            }
        }

        public void failAllCallsWith(Exception e) {
            for (Async<Object> asyncResult : asyncResults) {
                asyncResult.completeWithFailure(asList(e));
            }
        }
    }
}
