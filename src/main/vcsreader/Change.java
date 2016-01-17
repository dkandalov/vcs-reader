package vcsreader;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.VcsProject.LogFileContentResult;

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
	 * Requests content of modified file after the change.
	 */
	@NotNull public FileContent fileContent() {
        if (filePath.equals(noFilePath)) return FileContent.none;
        LogFileContentResult logFileContentResult = vcsRoot.get().contentOf(filePath, revision);
        return logFileContentResult.isSuccessful() ? new FileContent(logFileContentResult.text()) : FileContent.failedToLoad;
    }

	/**
	 * Requests content of modified file before the change.
	 */
    @NotNull public FileContent fileContentBefore() {
        if (filePathBefore.equals(noFilePath)) return FileContent.none;
        LogFileContentResult logFileContentResult = vcsRoot.get().contentOf(filePathBefore, revisionBefore);
        return logFileContentResult.isSuccessful() ? new FileContent(logFileContentResult.text()) : FileContent.failedToLoad;
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


    public static class FileContent {
        public final static FileContent failedToLoad = new FileContent("") {
            @Override public String toString() {
                return "ContentFailedToLoad";
            }
	        @Override public boolean equals(Object o) {
		        return this == o;
	        }
        };
        public final static FileContent none = new FileContent("") {
            @Override public String toString() {
                return "NoContent";
            }
	        @Override public boolean equals(Object o) {
		        return this == o;
	        }
        };

	    @NotNull public final String value;


        public FileContent(@NotNull String value) {
            this.value = value;
        }

	    @Override public String toString() {
		    return "Content{text='" + value.substring(0, Math.min(value.length(), 100)) + "'}";
	    }

	    @Override public boolean equals(Object o) {
		    if (this == o) return true;
		    if (o == null || getClass() != o.getClass()) return false;

		    FileContent fileContent = (FileContent) o;

		    return value.equals(fileContent.value);
	    }

	    @Override public int hashCode() {
		    return value.hashCode();
	    }
    }
}
