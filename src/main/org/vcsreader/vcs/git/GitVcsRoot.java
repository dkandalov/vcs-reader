package org.vcsreader.vcs.git;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vcsreader.VcsRoot;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsCommand.ResultAdapter;

import static org.vcsreader.VcsProject.*;

public class GitVcsRoot implements VcsRoot, VcsCommand.Owner {
	@NotNull private final String repoLocalPath;
	@Nullable private final String repositoryUrl;
	@NotNull private final GitSettings settings;
	private final VcsCommand.Listener listener;


	public GitVcsRoot(@NotNull String repoLocalPath) {
		this(repoLocalPath, null);
	}

	public GitVcsRoot(@NotNull String repoLocalPath, @Nullable String repositoryUrl) {
		this(repoLocalPath, repositoryUrl, GitSettings.defaults(), VcsCommand.Listener.none);
	}

	/**
	 * @param repoLocalPath local path to folder with repository from which history will be read.
	 *                      If there is no local clone of repository, you can call {@link #cloneToLocal()} to clone it.
	 * @param repositoryUrl url to remote repository (only required for {@link #cloneToLocal()})
	 */
	public GitVcsRoot(@NotNull String repoLocalPath, @Nullable String repositoryUrl, GitSettings settings) {
		this(repoLocalPath, repositoryUrl, settings, VcsCommand.Listener.none);
	}

	private GitVcsRoot(@NotNull String repoLocalPath, @Nullable String repositoryUrl,
	                   @NotNull GitSettings settings, VcsCommand.Listener listener) {
		this.repoLocalPath = repoLocalPath;
		this.repositoryUrl = repositoryUrl;
		this.settings = settings;
		this.listener = listener;
	}

	@Override public GitVcsRoot withListener(VcsCommand.Listener listener) {
		return new GitVcsRoot(repoLocalPath, repositoryUrl, settings, listener);
	}

	@Override public CloneResult cloneToLocal() {
		if (repositoryUrl == null && settings.failFast()) {
			throw new IllegalStateException("Cannot clone repository because remote url is not specified for root: " + this);
		}
		return execute(new GitClone(settings.gitPath(), repositoryUrl, repoLocalPath), CloneResult.adapter);
	}

	@Override public UpdateResult update() {
		return execute(new GitUpdate(settings.gitPath(), repoLocalPath), UpdateResult.adapter);
	}

	@Override public LogResult log(TimeRange timeRange) {
		return execute(new GitLog(settings.gitPath(), repoLocalPath, timeRange), LogResult.adapter);
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		GitLogFileContent logFileContent = new GitLogFileContent(settings.gitPath(), repoLocalPath, filePath, revision, settings.defaultFileCharset());
		return execute(logFileContent, LogFileContentResult.adapter);
	}

	private <T> T execute(VcsCommand<T> vcsCommand, ResultAdapter<T> resultAdapter) {
		return VcsCommand.execute(vcsCommand, resultAdapter, listener, settings.failFast());
	}

	@NotNull public String localPath() {
		return repoLocalPath;
	}

	@Nullable public String repositoryUrl() {
		return repositoryUrl;
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitVcsRoot that = (GitVcsRoot) o;

		if (!repoLocalPath.equals(that.repoLocalPath))
			return false;
		if (repositoryUrl != null ? !repositoryUrl.equals(that.repositoryUrl) : that.repositoryUrl != null)
			return false;
		if (!settings.equals(that.settings)) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = repoLocalPath.hashCode();
		result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
		result = 31 * result + settings.hashCode();
		return result;
	}

	@Override public String toString() {
		return "GitVcsRoot{" +
				"repoLocalPath='" + repoLocalPath + '\'' +
				", repositoryUrl='" + repositoryUrl + '\'' +
				", settings=" + settings +
				'}';
	}
}
