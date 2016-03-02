package vcsreader.vcs;

import org.jetbrains.annotations.NotNull;
import vcsreader.VcsChange;
import vcsreader.VcsCommit;
import vcsreader.VcsRoot;

import java.util.Date;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * This class is effectively immutable (even though some fields are modifiable).
 */
public class Commit implements VcsCommit, VcsCommit.WithRootReference {
	@NotNull private final String revision;
	@NotNull private final String revisionBefore;
	@NotNull private final Date time;
	@NotNull private final String author;
	@NotNull private final String message;
	@NotNull private final List<? extends VcsChange> changes;


	public Commit(@NotNull String revision, @NotNull String revisionBefore, @NotNull Date time,
	              @NotNull String author, @NotNull String message, @NotNull List<? extends VcsChange> changes) {
		this.revision = revision;
		this.revisionBefore = revisionBefore;
		this.time = time;
		this.author = author;
		this.message = message;
		this.changes = unmodifiableList(changes);
	}

	@Override public void setVcsRoot(VcsRoot vcsRoot) {
		for (VcsChange change : changes) {
			if (change instanceof VcsChange.WithRootReference) {
				((VcsChange.WithRootReference) change).setVcsRoot(vcsRoot);
			}
		}
	}

	@Override public Commit withChanges(List<? extends VcsChange> newChanges) {
		return new Commit(revision, revisionBefore, time, author, message, newChanges);
	}

	@Override @NotNull public String getRevision() {
		return revision;
	}

	@Override @NotNull public String getRevisionBefore() {
		return revisionBefore;
	}

	@Override @NotNull public Date getTime() {
		return time;
	}

	@Override @NotNull public String getAuthor() {
		return author;
	}

	@Override @NotNull public String getMessage() {
		return message;
	}

	@Override @NotNull public List<? extends VcsChange> getChanges() {
		return changes;
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
