package vcsreader;

import java.util.Date;
import java.util.List;

public class Commit {
    public final String revision;
    public final Date commitDate;
    public final String authorName;
    public final String comment;
    public final List<Change> changes;

    public Commit(String revision, Date commitDate, String authorName, String comment, List<Change> changes) {
        this.revision = revision;
        this.commitDate = commitDate;
        this.authorName = authorName;
        this.comment = comment;
        this.changes = changes;
    }

    @Override public String toString() {
        return "Commit{" +
                revision + ',' +
                commitDate + ',' +
                authorName + ',' +
                comment + ',' +
                changes +
        '}';
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        if (changes != null ? !changes.equals(commit.changes) : commit.changes != null) return false;
        if (comment != null ? !comment.equals(commit.comment) : commit.comment != null) return false;
        if (commitDate != null ? !commitDate.equals(commit.commitDate) : commit.commitDate != null) return false;
        if (authorName != null ? !authorName.equals(commit.authorName) : commit.authorName != null)
            return false;
        if (revision != null ? !revision.equals(commit.revision) : commit.revision != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = revision != null ? revision.hashCode() : 0;
        result = 31 * result + (commitDate != null ? commitDate.hashCode() : 0);
        result = 31 * result + (authorName != null ? authorName.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (changes != null ? changes.hashCode() : 0);
        return result;
    }
}
