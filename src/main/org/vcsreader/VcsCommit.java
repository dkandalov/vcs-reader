package org.vcsreader;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

/**
 * Contains data for single VCS commit.
 * See also {@link VcsChange}.
 */
public interface VcsCommit {

	/**
	 * @return revision of this commit as returned by VCS
	 */
	@NotNull String getRevision();

	/**
	 * @return revision of previous commit as returned by VCS or {@link VcsChange#noRevision} for the first commit
	 */
	@NotNull String getRevisionBefore();

	/**
	 * @return date and time of commit
	 */
	@NotNull Instant getTime(); // TODO rename to getDateTime()

	/**
	 * @return author of the commit.
	 * Note that for merge commits in git and mercurial author and committer can be different.
	 * Also author name can include email (e.g. for git commits).
	 */
	@NotNull String getAuthor();

	/**
	 * @return commit message
	 */
	@NotNull String getMessage();

	/**
	 * @return list of changes made in the commit.
	 * The order of changes is specified by VCS (it may be unstable e.g. in case of svn).
	 *
	 * Note that for svn only the changes made under the current {@link VcsRoot} path are included.
	 * Note that git, svn support commits with no changes so list of changes can be empty.
	 */
	@NotNull List<? extends VcsChange> getChanges();

	/**
	 * @return new instance of commit object with updated list of changes
	 */
	VcsCommit withChanges(List<? extends VcsChange> newChanges);


	interface WithRootReference {
		void setVcsRoot(VcsRoot vcsRoot);
	}
}
