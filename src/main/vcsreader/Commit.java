package vcsreader;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Contains data for single VCS commit.
 * See also {@link Change}.
 *
 * This class is effectively immutable (even though some fields are modifiable).
 */
public class Commit {
	/**
	 * Revision id as returned by VCS.
	 */
    @NotNull public final String revision;
	/**
	 * Previous revision id as return by VCS ({@link Change#noRevision} for the first commit).
	 */
    @NotNull public final String revisionBefore;
	/**
	 * Time of commit.
	 */
    @NotNull public final Date time;
	/**
	 * Commit author (it might be different from committer).
	 * Note that it might also include email (e.g. for git commits).
	 */
    @NotNull public final String author;
	/**
	 * Commit message.
	 */
    @NotNull public final String message;
	/**
	 * List of changes made in the commit.
	 * Only the changes made under current {@link VcsRoot} are included.
	 * The order of changes is specified by VCS (it may be unstable e.g. in case of svn).
	 * Note that list of changes can be empty (e.g. git, svn support commits with no changes).
	 */
    @NotNull public final List<Change> changes;


    public Commit(@NotNull String revision, @NotNull String revisionBefore, @NotNull Date time,
                  @NotNull String author, @NotNull String message, @NotNull List<Change> changes) {
        this.revision = revision;
        this.revisionBefore = revisionBefore;
        this.time = time;
        this.author = author;
        this.message = message;
        this.changes = unmodifiableList(changes);
    }

    public void setVcsRoot(VcsRoot vcsRoot) {
        for (Change change : changes) {
            change.setVcsRoot(vcsRoot);
        }
    }

    public Commit withChanges(List<Change> newChanges) {
        return new Commit(revision, revisionBefore, time, author, message, newChanges);
    }

    @Override public String toString() {
        return "Commit(" +
                revision + ',' +
                revisionBefore + ',' +
		        time + ',' +
		        author + ',' +
		        message + ',' +
                changes +
        ')';
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        if (!author.equals(commit.author)) return false;
        if (!changes.equals(commit.changes)) return false;
        if (!message.equals(commit.message)) return false;
        if (!time.equals(commit.time)) return false;
        if (!revision.equals(commit.revision)) return false;
        if (!revisionBefore.equals(commit.revisionBefore))
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = revision.hashCode();
        result = 31 * result + (revisionBefore.hashCode());
        result = 31 * result + (time.hashCode());
        result = 31 * result + (author.hashCode());
        result = 31 * result + (message.hashCode());
        result = 31 * result + (changes.hashCode());
        return result;
    }
}
