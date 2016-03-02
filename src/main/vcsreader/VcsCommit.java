package vcsreader;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

/**
 * Contains data for single VCS commit.
 * @see VcsChange
 */
public interface VcsCommit {

	/**
	 * @return new instance of commit object with updated list of changes
	 */
	VcsCommit withChanges(List<? extends VcsChange> newChanges);

	/**
	 * Revision id as returned by VCS.
	 */
	@NotNull String getRevision();

	/**
	 * Previous revision id as return by VCS ({@link VcsChange#noRevision} for the first commit).
	 */
	@NotNull String getRevisionBefore();

	/**
	 * Time of commit.
	 */
	@NotNull Date getTime();

	/**
	 * Commit author (it might be different from committer).
	 * Note that it might also include email (e.g. for git commits).
	 */
	@NotNull String getAuthor();

	/**
	 * Commit message.
	 */
	@NotNull String getMessage();

	/**
	 * List of changes made in the commit.
	 * Only the changes made under current {@link VcsRoot} are included.
	 * The order of changes is specified by VCS (it may be unstable e.g. in case of svn).
	 * Note that list of changes can be empty (e.g. git, svn support commits with no changes).
	 */
	@NotNull List<? extends VcsChange> getChanges();


	interface WithRootReference {
		void setVcsRoot(VcsRoot vcsRoot);
	}
}
