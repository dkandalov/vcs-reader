package vcsreader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vcsreader.lang.Async;

import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.VcsProject.LogContentResult;

public class Change {
    public static final String noRevision = "noRevision";
    public static final String noFileName = "";
    public static final String failedToLoadContent = null;

    public enum Type {
        MODIFICATION,
        NEW,
        DELETED,
        MOVED
    }

    public final Type type;
    @NotNull public final String fileName; // TODO rename to filePath
    @NotNull public final String fileNameBefore;
    @NotNull public final String revision;
    @NotNull public final String revisionBefore;
    private final AtomicReference<VcsRoot> vcsRoot = new AtomicReference<VcsRoot>();

    public Change(Type type, @NotNull String fileName, @NotNull String revision) {
        this(type, fileName, noFileName, revision, noRevision);
    }

    public Change(Type type, @NotNull String fileName, @NotNull String fileNameBefore,
                             @NotNull String revision, @NotNull String revisionBefore) {
        this.type = type;
        this.fileName = fileName;
        this.fileNameBefore = fileNameBefore;
        this.revision = revision;
        this.revisionBefore = revisionBefore;
    }

    @Nullable public String content() {
        LogContentResult result = contentAsync().awaitResult();
        return result.isSuccessful() ? result.text() : failedToLoadContent;
    }

    @Nullable public String contentBefore() {
        LogContentResult result = contentBeforeAsync().awaitResult();
        return result.isSuccessful() ? result.text() : failedToLoadContent;
    }

    public Async<LogContentResult> contentAsync() {
        if (fileName.equals(noFileName)) return new Async<LogContentResult>().completeWith(LogContentResult.none);
        else return vcsRoot.get().contentOf(fileName, revision);
    }

    public Async<LogContentResult> contentBeforeAsync() {
        if (fileNameBefore.equals(noFileName)) return new Async<LogContentResult>().completeWith(LogContentResult.none);
        else return vcsRoot.get().contentOf(fileNameBefore, revisionBefore);
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
        if (!fileName.equals(change.fileName)) return false;
        if (!fileNameBefore.equals(change.fileNameBefore)) return false;
        if (!revision.equals(change.revision)) return false;
        if (!revisionBefore.equals(change.revisionBefore)) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (fileName.hashCode());
        result = 31 * result + (fileNameBefore.hashCode());
        result = 31 * result + (revision.hashCode());
        result = 31 * result + (revisionBefore.hashCode());
        return result;
    }
}
