package vcsreader;

import vcsreader.lang.Async;

import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.VcsProject.LogContentResult;

public class Change {
    public static final String noRevision = "noRevision";

    public final Type changeType;
    private final String fileName;
    private final String fileNameBefore;
    private final String revision;
    private final String revisionBefore;
    private final AtomicReference<VcsRoot> vcsRoot = new AtomicReference<VcsRoot>();

    public Change(Type changeType, String fileName, String revision, String revisionBefore) {
        this(changeType, fileName, fileName, revision, revisionBefore);
    }

    public Change(Type changeType, String fileName, String fileNameBefore, String revision, String revisionBefore) {
        this.changeType = changeType;
        this.fileName = fileName;
        this.fileNameBefore = fileNameBefore;
        this.revision = revision;
        this.revisionBefore = revisionBefore;
    }

    public String content() {
        LogContentResult result = contentAsync().awaitCompletion();
        return result.isSuccessful() ? result.text() : null;
    }

    public String contentBefore() {
        LogContentResult result = contentBeforeAsync().awaitCompletion();
        return result.isSuccessful() ? result.text() : null;
    }

    public Async<LogContentResult> contentAsync() {
        return vcsRoot.get().contentOf(fileName, revision);
    }

    public Async<LogContentResult> contentBeforeAsync() {
        if (noRevision.equals(revisionBefore))
            return new Async<LogContentResult>().completeWith(LogContentResult.none);
        return vcsRoot.get().contentOf(fileNameBefore, revisionBefore);
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
        return "Change{" + changeType + ',' + fileName + ',' + fileNameBefore + ',' + revision + ',' + revisionBefore + '}';
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change change = (Change) o;

        if (changeType != change.changeType) return false;
        if (fileName != null ? !fileName.equals(change.fileName) : change.fileName != null) return false;
        if (fileNameBefore != null ? !fileNameBefore.equals(change.fileNameBefore) : change.fileNameBefore != null)
            return false;
        if (revision != null ? !revision.equals(change.revision) : change.revision != null) return false;
        if (revisionBefore != null ? !revisionBefore.equals(change.revisionBefore) : change.revisionBefore != null)
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = changeType != null ? changeType.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (fileNameBefore != null ? fileNameBefore.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result + (revisionBefore != null ? revisionBefore.hashCode() : 0);
        return result;
    }
}
