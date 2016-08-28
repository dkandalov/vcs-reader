package org.vcsreader.vcs.git;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vcsreader.VcsRoot;
import org.vcsreader.vcs.commandlistener.VcsCommand;
import org.vcsreader.vcs.commandlistener.VcsCommand.ResultAdapter;

import java.util.Date;

import static org.vcsreader.VcsProject.*;

public class GitVcsRoot implements VcsRoot, VcsCommand.Owner {
	public final String localPath;
	public final String repositoryUrl;
	public final GitSettings settings;
	private final VcsCommand.Listener listener;


	public GitVcsRoot(@NotNull String localPath) {
		this(localPath, null);
	}

	public GitVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl) {
		this(localPath, repositoryUrl, GitSettings.defaults(), VcsCommand.Listener.none);
	}

	/**
	 * @param localPath     path to folder with repository
	 *                      (if folder doesn't exist, it will be created by {@link #cloneToLocal()}.
	 * @param repositoryUrl url to remote repository (only required for {@link #cloneToLocal()})
	 */
	public GitVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl, GitSettings settings) {
		this(localPath, repositoryUrl, settings, VcsCommand.Listener.none);
	}

	private GitVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl,
	                  GitSettings settings, VcsCommand.Listener listener) {
		this.localPath = localPath;
		this.repositoryUrl = repositoryUrl;
		this.settings = settings;
		this.listener = listener;
	}

	@Override public GitVcsRoot withListener(VcsCommand.Listener listener) {
		return new GitVcsRoot(localPath, repositoryUrl, settings, listener);
	}

	@Override public CloneResult cloneToLocal() {
		if (repositoryUrl == null && settings.failFast) {
			throw new IllegalStateException("Cannot clone repository because remote url is not specified for root: " + this);
		}
		return execute(new GitClone(settings.gitPath, repositoryUrl, localPath), CloneResult.adapter);
	}

	@Override public UpdateResult update() {
		return execute(new GitUpdate(settings.gitPath, localPath), UpdateResult.adapter);
	}

	@Override public LogResult log(Date fromDate, Date toDate) {
		return execute(new GitLog(settings.gitPath, localPath, fromDate, toDate), LogResult.adapter);
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		GitLogFileContent logFileContent = new GitLogFileContent(settings.gitPath, localPath, filePath, revision, settings.defaultFileCharset);
		return execute(logFileContent, LogFileContentResult.adapter);
	}

	private <T> T execute(VcsCommand<T> vcsCommand, ResultAdapter<T> resultAdapter) {
		return VcsCommand.execute(vcsCommand, resultAdapter, listener, settings.failFast);
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
