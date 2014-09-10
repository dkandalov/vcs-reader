package vcsreader.vcs.git;

import org.junit.Ignore;
import org.junit.Test;
import vcsreader.VcsProject;
import vcsreader.VcsRoot;
import vcsreader.lang.Async;
import vcsreader.lang.VcsCommand;
import vcsreader.lang.VcsCommandExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static vcsreader.VcsProject.InitResult;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.lang.DateTimeUtil.date;

@Ignore // TODO
public class VcsProjectTest {
    private final GitSettings settings = GitSettings.defaults();
    private final FakeVcsCommandExecutor fakeExecutor = new FakeVcsCommandExecutor();

    @Test public void successfulProjectInitialization() {
        // given
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url", settings),
                new GitVcsRoot("/local/path2", "git://some/url", settings)
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);
        fakeExecutor.completeAllCallsWith(mock(InitResult.class));

        // when
        InitResult initResult = project.init();

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<VcsCommand>asList(
                new GitClone("/usr/bin/git", "git://some/url", "/local/path"),
                new GitClone("/usr/bin/git", "git://some/url", "/local/path2")
        )));
        assertThat(initResult.errors().size(), equalTo(0));
    }

    @Test public void successfulLogProjectHistory() {
        // given
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url", settings),
                new GitVcsRoot("/local/path2", "git://some/url", settings)
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);
        fakeExecutor.completeAllCallsWith(mock(LogResult.class));

        // when
        LogResult logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<VcsCommand>asList(
                new GitLog("/usr/bin/git", "/local/path", date("01/07/2014"), date("08/07/2014")),
                new GitLog("/usr/bin/git", "/local/path2", date("01/07/2014"), date("08/07/2014"))
        )));
        assertThat(logResult.errors().size(), equalTo(0));
    }

    @Test public void failedLogProjectHistory() {
        // given
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url", settings),
                new GitVcsRoot("/local/path2", "git://some/url", settings)
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);
        fakeExecutor.failAllCallsWith(new Exception());

        // when
        LogResult logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        assertThat(logResult.errors().size(), equalTo(0));
    }


    private static class FakeVcsCommandExecutor extends VcsCommandExecutor {
        public final List<VcsCommand> commands = new ArrayList<VcsCommand>();
        private Object completeWithResult;
        private Exception failWithException;

        @SuppressWarnings("unchecked")
        @Override public Async<Object> execute(VcsCommand command) {
            Async<Object> asyncResult = new Async<Object>();

            commands.add(command);
            if (completeWithResult != null) asyncResult.completeWith(completeWithResult);
            else if (failWithException != null) asyncResult.completeWithFailure(asList(failWithException));

            return asyncResult;
        }

        public void completeAllCallsWith(Object result) {
            completeWithResult = result;
        }

        public void failAllCallsWith(Exception e) {
            failWithException = e;
        }
    }
}
