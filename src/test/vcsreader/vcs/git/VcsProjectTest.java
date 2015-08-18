package vcsreader.vcs.git;

import org.junit.Test;
import vcsreader.Change;
import vcsreader.Commit;
import vcsreader.VcsProject;
import vcsreader.VcsProject.CloneResult;
import vcsreader.VcsRoot;

import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static vcsreader.VcsProject.LogResult;
import static vcsreader.lang.DateTimeUtil.date;

public class VcsProjectTest {
    private final VcsRoot root1 = mock(GitVcsRoot.class);
    private final VcsRoot root2 = mock(GitVcsRoot.class);

    @Test public void mergeResultsOfProjectClone() {
        // given
        when(root1.cloneToLocal()).thenReturn(new CloneResult(asList("error1")));
        when(root2.cloneToLocal()).thenReturn(new CloneResult(asList("error2")));
        VcsProject project = new VcsProject(asList(root1, root2));

        // when
        CloneResult cloneResult = project.cloneToLocal();

        // then
        assertThat(cloneResult.vcsErrors(), equalTo(asList("error1", "error2")));
        assertThat(cloneResult.exceptions().size(), equalTo(0));
    }

    @Test public void successfulProjectCloneWithNoVcsRoots() {
        // given
        VcsProject project = new VcsProject(Collections.<VcsRoot>emptyList());

        // when
        CloneResult cloneResult = project.cloneToLocal();

        // then
        assertThat(cloneResult.vcsErrors().size(), equalTo(0));
        assertThat(cloneResult.exceptions().size(), equalTo(0));
    }

    @Test public void successfulLogProjectHistory() {
        // given
        final Commit commit1 = new Commit("1", "", new Date(0), "", "", new ArrayList<Change>());
        final Commit commit2 = new Commit("2", "", new Date(0), "", "", new ArrayList<Change>());
        when(root1.log(any(Date.class), any(Date.class))).thenReturn(new LogResult(asList(commit1), asList("some error")));
        when(root2.log(any(Date.class), any(Date.class))).thenReturn(new LogResult(asList(commit2), new ArrayList<String>()));
        VcsProject project = new VcsProject(asList(root1, root2));

        // when
        LogResult logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        assertThat(logResult.getCommits(), equalTo(asList(commit1, commit2)));
        assertThat(logResult.vcsErrors(), equalTo(asList("some error")));
        assertThat(logResult.exceptions().size(), equalTo(0));
    }

    @Test public void failedLogProjectHistory() {
        // given
        when(root1.log(any(Date.class), any(Date.class))).thenThrow(new IllegalStateException());
        when(root2.log(any(Date.class), any(Date.class))).thenThrow(new IllegalStateException());
        VcsProject project = new VcsProject(asList(root1, root2));

        // when
        LogResult logResult = project.log(date("01/07/2014"), date("08/07/2014"));

        // then
        assertThat(logResult.vcsErrors().size(), equalTo(0));
        assertThat(logResult.exceptions().size(), equalTo(2));
    }
}
