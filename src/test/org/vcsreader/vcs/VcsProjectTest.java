package org.vcsreader.vcs;

import org.junit.Before;
import org.junit.Test;
import org.vcsreader.VcsCommit;
import org.vcsreader.VcsProject;
import org.vcsreader.VcsProject.CloneResult;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.git.GitVcsRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.vcsreader.VcsProject.LogResult;
import static org.vcsreader.lang.DateTimeUtil.timeRange;

public class VcsProjectTest {
	private final GitVcsRoot root1 = mock(GitVcsRoot.class);
	private final GitVcsRoot root2 = mock(GitVcsRoot.class);

	@Before public void setup() {
		when(root1.withListener(any())).thenReturn(root1);
		when(root2.withListener(any())).thenReturn(root2);
	}

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
		VcsProject project = new VcsProject(Collections.emptyList());

		// when
		CloneResult cloneResult = project.cloneToLocal();

		// then
		assertThat(cloneResult.vcsErrors().size(), equalTo(0));
		assertThat(cloneResult.exceptions().size(), equalTo(0));
	}

	@Test public void successfulLogProjectHistory() {
		// given
		VcsCommit commit1 = new Commit("1", "", Instant.ofEpochMilli(0), "", "", new ArrayList<>());
		VcsCommit commit2 = new Commit("2", "", Instant.ofEpochMilli(0), "", "", new ArrayList<>());
		when(root1.log(anyTimeRange())).thenReturn(new LogResult(asList(commit1), asList("some error")));
		when(root2.log(anyTimeRange())).thenReturn(new LogResult(asList(commit2), new ArrayList<>()));
		VcsProject project = new VcsProject(asList(root1, root2));

		// when
		LogResult logResult = project.log(timeRange("01/07/2014", "08/07/2014"));

		// then
		assertThat(logResult.commits(), equalTo(asList(commit1, commit2)));
		assertThat(logResult.vcsErrors(), equalTo(asList("some error")));
		assertThat(logResult.exceptions().size(), equalTo(0));
	}

	@Test(expected = IllegalStateException.class)
	public void failedLogProjectHistory() {
		// given
		when(root1.log(anyTimeRange())).thenThrow(new IllegalStateException());
		when(root2.log(anyTimeRange())).thenThrow(new IllegalStateException());
		VcsProject project = new VcsProject(asList(root1, root2));

		// when / then
		project.log(timeRange("01/07/2014", "08/07/2014"));
	}

	private static TimeRange anyTimeRange() {
		return any(TimeRange.class);
	}
}
