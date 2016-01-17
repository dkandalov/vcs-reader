package vcsreader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.VcsProject.LogContentResult;

/**
 * Contains data about file modification in a {@link Commit}.
 */
public class Change {
    public enum Type {
        MODIFICATION,
        NEW,
        DELETED,
        MOVED
    }
    public static final String noRevision = "noRevision";
    public static final String noFilePath = "";

	/**
	 * Type of file change.
	 */
    @NotNull public final Type type;
	/**
	 * File path after commit relative to {@link VcsRoot} path.
	 * {@link #noFilePath} if file was deleted.
	 */
    @NotNull public final String filePath;
	/**
	 * File path before commit relative to {@link VcsRoot} path.
	 * {@link #noFilePath} if file didn't exist.
	 */
    @NotNull public final String filePathBefore;

	private final String revision;
    private final String revisionBefore;
    private final AtomicReference<VcsRoot> vcsRoot = new AtomicReference<VcsRoot>();


    public Change(Change change) {
        this(change.type, change.filePath, change.filePathBefore, change.revision, change.revisionBefore);
        setVcsRoot(change.vcsRoot.get());
    }

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

	/**
	 * @return content of modified file after the change.
	 */
    @Nullable public Content content() {
        if (filePath.equals(noFilePath)) return Content.none;
        LogContentResult logContentResult = vcsRoot.get().contentOf(filePath, revision);
        return logContentResult.isSuccessful() ? new Content(logContentResult.text()) : Content.failedToLoad;
    }

	/**
	 * @return content of modified file before the change.
	 */
    @Nullable public Content contentBefore() {
        if (filePathBefore.equals(noFilePath)) return Content.none;
        LogContentResult logContentResult = vcsRoot.get().contentOf(filePathBefore, revisionBefore);
        return logContentResult.isSuccessful() ? new Content(logContentResult.text()) : Content.failedToLoad;
    }

	public Change withTypeAndPaths(Type type, String filePath, String filePathBefore) {
		return new Change(type, filePath, filePathBefore, revision, revisionBefore);
	}

    public void setVcsRoot(VcsRoot vcsRoot) {
        this.vcsRoot.set(vcsRoot);
    }

    @Override public String toString() {
        return "Change(" + type + ',' + filePath + ',' + filePathBefore + ',' + revision + ',' + revisionBefore + ')';
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

    public static class Content {
        public final static Content failedToLoad = new Content(null) {
            @Override public String toString() {
                return "ContentFailedToLoad";
            }
        };
        public final static Content none = new Content(null) {
            @Override public String toString() {
                return "NoContent";
            }
        };

        public final String text;

        public Content(String text) {
            this.text = text;
        }
    }
}
