package vcsreader;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class Commit {
    public final String revision;
    public final String revisionBefore;
    public final Date commitDate;
    public final String authorName;
    public final String comment;
    public final List<Change> changes;

    public Commit(String revision, String revisionBefore, Date commitDate, String authorName,
                  String comment, @NotNull List<Change> changes) {
        this.revision = revision;
        this.revisionBefore = revisionBefore;
        this.commitDate = commitDate;
        this.authorName = authorName;
        this.comment = comment;
        this.changes = changes;
    }

    public void setVcsRoot(VcsRoot vcsRoot) {
        for (Change change : changes) {
            change.setVcsRoot(vcsRoot);
        }
    }

    @Override public String toString() {
        return "Commit{" +
                revision + ',' +
                revisionBefore + ',' +
                commitDate + ',' +
                authorName + ',' +
                comment + ',' +
                changes +
        '}';
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        if (authorName != null ? !authorName.equals(commit.authorName) : commit.authorName != null) return false;
        if (!changes.equals(commit.changes)) return false;
        if (comment != null ? !comment.equals(commit.comment) : commit.comment != null) return false;
        if (commitDate != null ? !commitDate.equals(commit.commitDate) : commit.commitDate != null) return false;
        if (revision != null ? !revision.equals(commit.revision) : commit.revision != null) return false;
        if (revisionBefore != null ? !revisionBefore.equals(commit.revisionBefore) : commit.revisionBefore != null)
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = revision != null ? revision.hashCode() : 0;
        result = 31 * result + (revisionBefore != null ? revisionBefore.hashCode() : 0);
        result = 31 * result + (commitDate != null ? commitDate.hashCode() : 0);
        result = 31 * result + (authorName != null ? authorName.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (changes.hashCode());
        return result;
    }
}
