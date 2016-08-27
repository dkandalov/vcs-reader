package org.vcsreader;

import org.jetbrains.annotations.NotNull;

import static org.vcsreader.lang.StringUtil.shortened;

/**
 * Contains data about file modification in a {@link VcsCommit}.
 * See also {@link FileContent}.
 */
public interface VcsChange {
	String noRevision = "noRevision";
	String noFilePath = "";

	/**
	 * @return type of file change.
	 */
	@NotNull Type getType();

	/**
	 * @return file path after commit relative to {@link VcsRoot} path.
	 * {@link #noFilePath} if file was deleted.
	 */
	@NotNull String getFilePath();

	/**
	 * @return file path before commit relative to {@link VcsRoot} path.
	 * {@link #noFilePath} if file didn't exist.
	 */
	@NotNull String getFilePathBefore();

	/**
	 * @return revision of the commit in which file was modified
	 */
	String getRevision();

	/**
	 * @return revision of the previous commit in which file was modified
	 * or {@link VcsChange#noRevision} if file wan't tracked by VCS before this commit.
	 */
	String getRevisionBefore();

	/**
	 * @return requests from VCS and returns file content after change.
	 */
	@NotNull FileContent fileContent();

	/**
	 * @return requests from VCS and returns file content before change.
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
		 * Note that MOVED does not imply there were no changes in file content.
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

		@NotNull private final String value;


		public FileContent(@NotNull String value) {
			this.value = value;
		}

		/**
		 * @return content of file as logged by VCS.
		 * Encoding of the file is auto-detected or looked up in {@code VcsRoot} configuration
		 * and then decoded into java string UTF-16.
		 */
		@NotNull public String getValue() {
			return value;
		}

		@Override public String toString() {
			return "FileContent{value='" + shortened(value, 100) + "'}";
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
