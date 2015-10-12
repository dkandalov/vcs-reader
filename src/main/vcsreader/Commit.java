package vcsreader;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class Commit {
    @NotNull public final String revision;
    @NotNull public final String revisionBefore;
    @NotNull public final Date commitTime;
    @NotNull public final String authorName;
    @NotNull public final String comment;
    // note that order of changes is specified by VCS (and may be random e.g. in case of svn)
    @NotNull public final List<Change> changes;

    public Commit(@NotNull String revision, @NotNull String revisionBefore, @NotNull Date commitTime,
                  @NotNull String authorName, @NotNull String comment, @NotNull List<Change> changes) {
        this.revision = revision;
        this.revisionBefore = revisionBefore;
        this.commitTime = commitTime;
        this.authorName = authorName;
        this.comment = comment;
        this.changes = unmodifiableList(changes);
    }

    public void setVcsRoot(VcsRoot vcsRoot) {
        for (Change change : changes) {
            change.setVcsRoot(vcsRoot);
        }
    }

    public Commit withChanges(List<Change> newChanges) {
        return new Commit(revision, revisionBefore, commitTime, authorName, comment, newChanges);
    }

    @Override public String toString() {
        return "Commit(" +
                revision + ',' +
                revisionBefore + ',' +
                commitTime + ',' +
                authorName + ',' +
                comment + ',' +
                changes +
        ')';
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        if (!authorName.equals(commit.authorName)) return false;
        if (!changes.equals(commit.changes)) return false;
        if (!comment.equals(commit.comment)) return false;
        if (!commitTime.equals(commit.commitTime)) return false;
        if (!revision.equals(commit.revision)) return false;
        if (!revisionBefore.equals(commit.revisionBefore))
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = revision.hashCode();
        result = 31 * result + (revisionBefore.hashCode());
        result = 31 * result + (commitTime.hashCode());
        result = 31 * result + (authorName.hashCode());
        result = 31 * result + (comment.hashCode());
        result = 31 * result + (changes.hashCode());
        return result;
    }
}
