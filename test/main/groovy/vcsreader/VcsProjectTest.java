package vcsreader;

import org.junit.Test;
import vcsreader.vcs.GitVcsRoot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static vcsreader.CommandExecutor.Result;
import static vcsreader.VcsProject.InitResult;

public class VcsProjectTest {
    private final Result successfulResult = new Result(true);

    @Test public void successfulProjectInitialization() {
        FakeCommandExecutor fakeExecutor = new FakeCommandExecutor();
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(
                new GitVcsRoot("/local/path", "git://some/url"),
                new GitVcsRoot("/local/path2", "git://some/url")
        );
        VcsProject project = new VcsProject(vcsRoots, fakeExecutor);

        InitResult initResult = project.init();
        assertThat(initResult.isComplete(), equalTo(false));

        fakeExecutor.completeAllCallsWith(successfulResult);
        assertThat(initResult.isComplete(), equalTo(true));
    }


    private static class FakeCommandExecutor extends CommandExecutor {
        private final List<AsyncResult> asyncResults = new ArrayList<AsyncResult>();

        @Override public AsyncResult execute(Command command) {
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
