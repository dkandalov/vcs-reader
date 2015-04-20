package vcsreader.vcs

import vcsreader.Commit
import vcsreader.VcsProject.LogResult

import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

class TestUtil {
    static assertEqualCommits(LogResult logResult, List<Commit> expected) {
        assert logResult.errors() == []
        assert logResult.successful
        assertEqualCommits(withSortedChanges(logResult.commits), withSortedChanges(expected))
    }

    static assertEqualCommits(List<Commit> actual, List<Commit> expected) {
        assertThat(actual.join("\n\n"), equalTo(expected.join("\n\n")))
    }

	static List<Commit> withSortedChanges(List<Commit> commits) {
		commits.collect { commit ->
			commit.withChanges(commit.changes.toList().sort{it.filePath})
		}
	}
}
