package org.vcsreader.vcs.git;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vcsreader.*;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsCommand.ExceptionWrapper;

public class GitVcsRoot implements VcsRoot, VcsCommand.Observer {
	@NotNull private final String repoFolder;
	@Nullable private final String repoUrl;
	@NotNull private final GitSettings settings;
	private final VcsCommand.Listener listener;


	public GitVcsRoot(@NotNull String repoFolder) {
		this(repoFolder, null);
	}

	public GitVcsRoot(@NotNull String repoFolder, @Nullable String repoUrl) {
		this(repoFolder, repoUrl, GitSettings.defaults(), VcsCommand.Listener.none);
	}

	/**
	 * @param repoFolder path to folder with repository from which history will be read.
	 *                   If there is no local clone of repository, you can call {@link #cloneToLocal()} to clone it.
	 * @param repoUrl    remote repository URL. This can be any form of URL supported by git command line.
	 * @param settings   settings which will be used by VCS commands executed on this root
	 */
	public GitVcsRoot(@NotNull String repoFolder, @Nullable String repoUrl, GitSettings settings) {
		this(repoFolder, repoUrl, settings, VcsCommand.Listener.none);
	}

	private GitVcsRoot(@NotNull String repoFolder, @Nullable String repoUrl,
	                   @NotNull GitSettings settings, VcsCommand.Listener listener) {
		this.repoFolder = repoFolder;
		this.repoUrl = repoUrl;
		this.settings = settings;
		this.listener = listener;
	}

	@Override public GitVcsRoot withListener(VcsCommand.Listener listener) {
		return new GitVcsRoot(repoFolder, repoUrl, settings, listener);
	}

	@Override public CloneResult cloneToLocal() {
		if (repoUrl == null && settings.failFast()) {
			throw new IllegalStateException("Cannot clone repository because remote URL is not specified for root: " + this);
		}
		return execute(new GitClone(settings.gitPath(), repoUrl, repoFolder), CloneResult.adapter);
	}

	@Override public UpdateResult update() {
		return execute(new GitUpdate(settings.gitPath(), repoFolder), UpdateResult.adapter);
	}

	@Override public LogResult log(TimeRange timeRange) {
		return execute(new GitLog(settings.gitPath(), repoFolder, timeRange), LogResult.adapter);
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		GitLogFileContent logFileContent = new GitLogFileContent(settings.gitPath(), repoFolder, filePath, revision, settings.defaultFileCharset());
		return execute(logFileContent, LogFileContentResult.adapter);
	}

	private <T> T execute(VcsCommand<T> vcsCommand, ExceptionWrapper<T> exceptionWrapper) {
		return VcsCommand.execute(vcsCommand, exceptionWrapper, listener, settings.failFast());
	}

	@Override @NotNull public String repoFolder() {
		return repoFolder;
	}

	@Override @Nullable public String repoUrl() {
		return repoUrl;
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitVcsRoot that = (GitVcsRoot) o;

		if (!repoFolder.equals(that.repoFolder))
			return false;
		if (repoUrl != null ? !repoUrl.equals(that.repoUrl) : that.repoUrl != null)
			return false;
		if (!settings.equals(that.settings)) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = repoFolder.hashCode();
		result = 31 * result + (repoUrl != null ? repoUrl.hashCode() : 0);
		result = 31 * result + settings.hashCode();
		return result;
	}

	@Override public String toString() {
		return "GitVcsRoot{" +
				"repoFolder='" + repoFolder + '\'' +
				", repoUrl='" + repoUrl + '\'' +
				", settings=" + settings +
				'}';
	}
}
