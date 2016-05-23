package org.vcsreader.vcs

import org.vcsreader.VcsCommit
import org.vcsreader.VcsProject.LogResult

import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

class TestUtil {
	static assertEqualCommits(LogResult logResult, List<VcsCommit> expected) {
		assert logResult.vcsErrors() == []
		assert logResult.exceptions() == []
		assert logResult.successful
		assertEqualCommits(withSortedChanges(logResult.commits()), withSortedChanges(expected))
	}

	static assertEqualCommits(List<VcsCommit> actual, List<VcsCommit> expected) {
		assertThat(actual.join("\n\n"), equalTo(expected.join("\n\n")))
	}

	static List<VcsCommit> withSortedChanges(List<VcsCommit> commits) {
		commits.collect { commit ->
			commit.withChanges(commit.changes.toList().sort { it.filePath })
		}
	}
}
