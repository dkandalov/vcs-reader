package vcsreader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vcsreader.lang.Async;

import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.VcsProject.LogContentResult;

public class Change {
    public static final String noRevision = "noRevision";
    public static final String noFilePath = "";
    public static final String failedToLoadContent = null;

    public enum Type {
        MODIFICATION,
        NEW,
        DELETED,
        MOVED
    }

    @NotNull public final Type type;
    @NotNull public final String filePath;
    @NotNull public final String filePathBefore;
    @NotNull public final String revision;
    @NotNull public final String revisionBefore;
    private final AtomicReference<VcsRoot> vcsRoot = new AtomicReference<VcsRoot>();

    public Change(@NotNull Type type, @NotNull String filePath, @NotNull String revision) {
        this(type, filePath, noFilePath, revision, noRevision);
    }

    public Change(@NotNull Type type, @NotNull String filePath, @NotNull String filePathBefore,
                  @NotNull String revision, @NotNull String revisionBefore) {
        this.type = type;
        this.filePath = filePath;
        this.filePathBefore = filePathBefore;
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
        if (filePath.equals(noFilePath)) return new Async<LogContentResult>().completeWith(LogContentResult.none);
        else return vcsRoot.get().contentOf(filePath, revision);
    }

    public Async<LogContentResult> contentBeforeAsync() {
        if (filePathBefore.equals(noFilePath)) return new Async<LogContentResult>().completeWith(LogContentResult.none);
        else return vcsRoot.get().contentOf(filePathBefore, revisionBefore);
    }

    public void setVcsRoot(VcsRoot vcsRoot) {
        this.vcsRoot.set(vcsRoot);
    }

    @Override public String toString() {
        return "Change{" + type + ',' + filePath + ',' + filePathBefore + ',' + revision + ',' + revisionBefore + '}';
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change change = (Change) o;

        if (type != change.type) return false;
        if (!filePath.equals(change.filePath)) return false;
        if (!filePathBefore.equals(change.filePathBefore)) return false;
        if (!revision.equals(change.revision)) return false;
        if (!revisionBefore.equals(change.revisionBefore)) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (filePath.hashCode());
        result = 31 * result + (filePathBefore.hashCode());
        result = 31 * result + (revision.hashCode());
        result = 31 * result + (revisionBefore.hashCode());
        return result;
    }
}
