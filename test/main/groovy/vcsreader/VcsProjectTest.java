package vcsreader;

import org.junit.Test;
import vcsreader.vcs.GitClone;
import vcsreader.vcs.GitVcsRoot;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static vcsreader.VcsProject.InitResult;

public class VcsProjectTest {
    @Test public void successfulProjectInitialization() {
        GitClone.Result gitCloneResult = new GitClone.Result();
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        stub(commandExecutor.execute(any(CommandExecutor.Command.class))).toReturn(gitCloneResult);
        List<VcsRoot> vcsRoots = Arrays.<VcsRoot>asList(new GitVcsRoot("/local/path", "git://some/url"));
        VcsProject project = new VcsProject(vcsRoots, commandExecutor);

        InitResult initResult = project.init();
        assertThat(initResult.isReady(), equalTo(false));
        verify(commandExecutor).execute(new GitClone("git://some/url", "/local/path"));

        gitCloneResult.succeed();
        assertThat(initResult.isReady(), equalTo(true));
    }
}
