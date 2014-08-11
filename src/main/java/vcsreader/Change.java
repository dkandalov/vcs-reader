package vcsreader;

import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.VcsProject.LogContentResult;

public class Change {
    public static final String noRevision = null;

    private final Type changeType;
    private final String fileName;
    private final String revision;
    private final String revisionBefore;
    private final AtomicReference<VcsRoot> vcsRoot = new AtomicReference<VcsRoot>();

    public Change(Type changeType, String fileName, String revision, String revisionBefore) {
        this.changeType = changeType;
        this.fileName = fileName;
        this.revision = revision;
        this.revisionBefore = revisionBefore;
    }

    public String content() {
        LogContentResult result = (LogContentResult) vcsRoot.get().contentOf(fileName, revision).awaitCompletion();
        return result.isSuccessful() ? result.text() : null;
    }

    public void setVcsRoot(VcsRoot vcsRoot) {
        this.vcsRoot.set(vcsRoot);
    }

    public enum Type {
        MODIFICATION,
        NEW,
        DELETED,
        MOVED
    }

    @Override public String toString() {
        return "Change{" + changeType + ',' + fileName + ',' + revision + ',' + revisionBefore + '}';
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change change = (Change) o;

        if (changeType != change.changeType) return false;
        if (fileName != null ? !fileName.equals(change.fileName) : change.fileName != null) return false;
        if (revision != null ? !revision.equals(change.revision) : change.revision != null) return false;
        if (revisionBefore != null ? !revisionBefore.equals(change.revisionBefore) : change.revisionBefore != null)
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = changeType != null ? changeType.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result + (revisionBefore != null ? revisionBefore.hashCode() : 0);
        return result;
    }
}
