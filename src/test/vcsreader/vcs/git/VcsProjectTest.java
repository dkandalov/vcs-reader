package vcsreader.vcs.git;

import org.junit.Test;
import vcsreader.VcsProject;
import vcsreader.VcsRoot;
import vcsreader.vcs.common.VcsCommand;
import vcsreader.vcs.common.VcsCommandExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.lang.DateTimeUtil.date;

public class VcsProjectTest {
    private final GitSettings settings = GitSettings.defaults();
    private final FakeVcsCommandExecutor fakeExecutor = new FakeVcsCommandExecutor();

    @Test public void successfulProjectClone() {
        // given
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url", settings),
                new GitVcsRoot("/local/path2", "git://some/url", settings)
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);
        fakeExecutor.completeAllCallsWith(new VcsProject.CloneResult());

        // when
        VcsProject.CloneResult cloneResult = project.cloneToLocal();

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<VcsCommand>asList(
                new GitClone("/usr/bin/git", "git://some/url", "/local/path"),
                new GitClone("/usr/bin/git", "git://some/url", "/local/path2")
        )));
        assertThat(cloneResult.errors().size(), equalTo(0));
        assertThat(cloneResult.exceptions().size(), equalTo(0));
    }

    @Test public void successfulProjectCloneWithNoVcsRoots() {
        // given
        List<VcsRoot> vcsRoots = emptyList();
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);
        fakeExecutor.completeAllCallsWith(new VcsProject.CloneResult());

        // when
        VcsProject.CloneResult cloneResult = project.cloneToLocal();

        // then
        assertThat(fakeExecutor.commands, equalTo(Collections.<VcsCommand>emptyList()));
        assertThat(cloneResult.errors().size(), equalTo(0));
        assertThat(cloneResult.exceptions().size(), equalTo(0));
    }

    @Test public void successfulLogProjectHistory() {
        // given
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url", settings),
                new GitVcsRoot("/local/path2", "git://some/url", settings)
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);
        fakeExecutor.completeAllCallsWith(new LogResult());

        // when
        LogResult logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        assertThat(fakeExecutor.commands, equalTo(Arrays.<VcsCommand>asList(
                new GitLog("/usr/bin/git", "/local/path", date("01/07/2014"), date("08/07/2014")),
                new GitLog("/usr/bin/git", "/local/path2", date("01/07/2014"), date("08/07/2014"))
        )));
        assertThat(logResult.errors().size(), equalTo(0));
        assertThat(logResult.exceptions().size(), equalTo(0));
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
        assertThat(logResult.exceptions().size(), equalTo(2));
    }


    private static class FakeVcsCommandExecutor extends VcsCommandExecutor {
        public final List<VcsCommand> commands = new ArrayList<VcsCommand>();
        private Object completeWithResult;
        private Exception failWithException;

        @SuppressWarnings("unchecked")
        @Override public Object execute(VcsCommand command) {
            commands.add(command);

            if (completeWithResult != null) return completeWithResult;
            if (failWithException != null) return failWithException;
            throw new IllegalStateException();
        }

        public void completeAllCallsWith(Object result) {
            completeWithResult = result;
        }

        public void failAllCallsWith(Exception e) {
            failWithException = e;
        }
    }
}
