package vcsreader;

import vcsreader.lang.Async;

import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.Change.Type.DELETED;
import static vcsreader.Change.Type.NEW;
import static vcsreader.VcsProject.LogContentResult;

public class Change {
    public static final String noRevision = "noRevision";
    public static final String noFileName = "";

    public enum Type {
        MODIFICATION,
        NEW,
        DELETED,
        MOVED
    }

    public final Type type;
    public final String fileName;
    public final String fileNameBefore;
    public final String revision;
    public final String revisionBefore;
    private final AtomicReference<VcsRoot> vcsRoot = new AtomicReference<VcsRoot>();

    public Change(Type type, String fileName, String revision) {
        this(type, fileName, noFileName, revision, noRevision);
    }

    public Change(Type type, String fileName, String fileNameBefore, String revision, String revisionBefore) {
        this.type = type;
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
        if (type == DELETED) return new Async<LogContentResult>().completeWith(LogContentResult.none);
        return vcsRoot.get().contentOf(fileName, revision);
    }

    public Async<LogContentResult> contentBeforeAsync() {
        if (type == NEW) return new Async<LogContentResult>().completeWith(LogContentResult.none);
        return vcsRoot.get().contentOf(fileNameBefore, revisionBefore);
    }

    public void setVcsRoot(VcsRoot vcsRoot) {
        this.vcsRoot.set(vcsRoot);
    }

    @Override public String toString() {
        return "Change{" + type + ',' + fileName + ',' + fileNameBefore + ',' + revision + ',' + revisionBefore + '}';
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change change = (Change) o;

        if (type != change.type) return false;
        if (fileName != null ? !fileName.equals(change.fileName) : change.fileName != null) return false;
        if (fileNameBefore != null ? !fileNameBefore.equals(change.fileNameBefore) : change.fileNameBefore != null)
            return false;
        if (revision != null ? !revision.equals(change.revision) : change.revision != null) return false;
        if (revisionBefore != null ? !revisionBefore.equals(change.revisionBefore) : change.revisionBefore != null)
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (fileNameBefore != null ? fileNameBefore.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result + (revisionBefore != null ? revisionBefore.hashCode() : 0);
        return result;
    }
}
