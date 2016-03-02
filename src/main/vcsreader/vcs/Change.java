package vcsreader.vcs;

import org.jetbrains.annotations.NotNull;
import vcsreader.VcsChange;
import vcsreader.VcsRoot;

import java.util.concurrent.atomic.AtomicReference;

import static vcsreader.VcsProject.LogFileContentResult;

public class Change implements VcsChange, VcsChange.WithRootReference {
	@NotNull private final Type type;
	@NotNull private final String filePath;
	@NotNull private final String filePathBefore;
	private final String revision;
	private final String revisionBefore;

	private final AtomicReference<VcsRoot> vcsRoot = new AtomicReference<VcsRoot>();


	public Change(Change change) {
		this(change.getType(), change.getFilePath(), change.getFilePathBefore(), change.getRevision(), change.getRevisionBefore());
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

	@NotNull @Override public Type getType() {
		return type;
	}

	@NotNull @Override public String getFilePath() {
		return filePath;
	}

	@NotNull @Override public String getFilePathBefore() {
		return filePathBefore;
	}

	@Override public String getRevision() {
		return revision;
	}

	@Override public String getRevisionBefore() {
		return revisionBefore;
	}

	@NotNull @Override public FileContent fileContent() {
		if (filePath.equals(noFilePath)) return FileContent.none;
		LogFileContentResult logFileContentResult = vcsRoot.get().logFileContent(filePath, revision);
		return logFileContentResult.isSuccessful() ? new FileContent(logFileContentResult.text()) : FileContent.failedToLoad;
	}

	@NotNull @Override public FileContent fileContentBefore() {
		if (filePathBefore.equals(noFilePath)) return FileContent.none;
		LogFileContentResult logFileContentResult = vcsRoot.get().logFileContent(filePathBefore, revisionBefore);
		return logFileContentResult.isSuccessful() ? new FileContent(logFileContentResult.text()) : FileContent.failedToLoad;
	}

	public Change withTypeAndPaths(Type type, String filePath, String filePathBefore) {
		return new Change(type, filePath, filePathBefore, revision, revisionBefore);
	}

	@Override public void setVcsRoot(VcsRoot vcsRoot) {
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
}
