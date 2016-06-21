package org.vcsreader.vcs.git;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vcsreader.VcsRoot;
import org.vcsreader.vcs.commandlistener.VcsCommandObserver;

import java.util.Date;

import static org.vcsreader.VcsProject.*;

public class GitVcsRoot implements VcsRoot, VcsRoot.WithCommandObserver {
	public final String localPath;
	public final String repositoryUrl;
	public final GitSettings settings;
	private VcsCommandObserver observer;


	public GitVcsRoot(@NotNull String localPath) {
		this(localPath, null, GitSettings.defaults());
	}

	public GitVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl) {
		this(localPath, repositoryUrl, GitSettings.defaults());
	}

	/**
	 * @param localPath     path to folder with repository
	 *                      (if folder doesn't exist, it will be created by {@link #cloneToLocal()}.
	 * @param repositoryUrl url to remote repository (only required for {@link #cloneToLocal()})
	 */
	public GitVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl, GitSettings settings) {
		this.localPath = localPath;
		this.repositoryUrl = repositoryUrl;
		this.settings = settings;
	}

	@Override public CloneResult cloneToLocal() {
		if (repositoryUrl == null) {
			throw new IllegalStateException("Cannot clone repository because remote url is not specified");
		}
		return observer.executeAndObserve(new GitClone(settings.gitPath, repositoryUrl, localPath));
	}

	@Override public UpdateResult update() {
		return observer.executeAndObserve(new GitUpdate(settings.gitPath, localPath));
	}

	@Override public LogResult log(Date fromDate, Date toDate) {
		return observer.executeAndObserve(new GitLog(settings.gitPath, localPath, fromDate, toDate));
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		return observer.executeAndObserve(new GitLogFileContent(settings.gitPath, localPath, filePath, revision, settings.defaultFileCharset));
	}

	@Override public void setObserver(VcsCommandObserver observer) {
		this.observer = observer;
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitVcsRoot that = (GitVcsRoot) o;

		if (localPath != null ? !localPath.equals(that.localPath) : that.localPath != null)
			return false;
		if (repositoryUrl != null ? !repositoryUrl.equals(that.repositoryUrl) : that.repositoryUrl != null)
			return false;
		if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = localPath != null ? localPath.hashCode() : 0;
		result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
		result = 31 * result + (settings != null ? settings.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitVcsRoot{" +
				"localPath='" + localPath + '\'' +
				", repositoryUrl='" + repositoryUrl + '\'' +
				", settings=" + settings +
				'}';
	}
}
