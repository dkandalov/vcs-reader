package vcsreader;

import org.jetbrains.annotations.NotNull;

import static vcsreader.lang.StringUtil.shortened;

public interface VcsChange {
	String noRevision = "noRevision";
	String noFilePath = "";

	/**
	 * Type of file change.
	 */
	@NotNull Type getType();

	/**
	 * File path after commit relative to {@link VcsRoot} path.
	 * {@link #noFilePath} if file was deleted.
	 */
	@NotNull String getFilePath();

	/**
	 * File path before commit relative to {@link VcsRoot} path.
	 * {@link #noFilePath} if file didn't exist.
	 */
	@NotNull String getFilePathBefore();

	String getRevision();

	String getRevisionBefore();

	/**
	 * Requests content of modified file after the change.
	 */
	@NotNull FileContent fileContent();

	/**
	 * Requests content of modified file before the change.
	 */
	@NotNull FileContent fileContentBefore();


	interface WithRootReference {
		void setVcsRoot(VcsRoot vcsRoot);
	}


	enum Type {
		ADDED,
		MODIFIED,
		DELETED,
		/**
		 * Note that MOVED does not imply there were no change in file content.
		 */
		MOVED
	}


	class FileContent {
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

		/**
		 * Content of file as logged by VCS.
		 * Encoding of the file is auto-detected or looked up in {@code VcsRoot} configuration
		 * and then decoded into java string UTF-16.
		 */
		@NotNull public final String value;


		public FileContent(@NotNull String value) {
			this.value = value;
		}

		@Override public String toString() {
			return "Content{value='" + shortened(value, 100) + "'}";
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
