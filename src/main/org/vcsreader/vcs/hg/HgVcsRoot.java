package org.vcsreader.vcs.hg;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vcsreader.CloneResult;
import org.vcsreader.LogFileContentResult;
import org.vcsreader.LogResult;
import org.vcsreader.UpdateResult;
import org.vcsreader.VcsRoot;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsCommand.ResultAdapter;

public class HgVcsRoot implements VcsRoot, VcsCommand.Owner {
	@NotNull private final String repoLocalPath;
	@Nullable private final String repositoryUrl;
	@NotNull private final HgSettings settings;
	private final VcsCommand.Listener listener;


	public HgVcsRoot(@NotNull String repoLocalPath) {
		this(repoLocalPath, null);
	}

	public HgVcsRoot(@NotNull String repoLocalPath, @Nullable String repositoryUrl) {
		this(repoLocalPath, repositoryUrl, HgSettings.defaults(), VcsCommand.Listener.none);
	}

	/**
	 * @param repoLocalPath local path to folder with repository from which history will be read.
	 *                      If there is no local clone of repository, you can call {@link #cloneToLocal()} to clone it.
	 * @param repositoryUrl url to remote repository (only required for {@link #cloneToLocal()})
	 */
	public HgVcsRoot(@NotNull String repoLocalPath, @Nullable String repositoryUrl, @NotNull HgSettings settings) {
		this(repoLocalPath, repositoryUrl, settings, VcsCommand.Listener.none);
	}

	private HgVcsRoot(@NotNull String repoLocalPath, @Nullable String repositoryUrl,
	                  @NotNull HgSettings settings, VcsCommand.Listener listener) {
		this.repoLocalPath = repoLocalPath;
		this.repositoryUrl = repositoryUrl;
		this.settings = settings;
		this.listener = listener;
	}

	@Override public HgVcsRoot withListener(VcsCommand.Listener listener) {
		return new HgVcsRoot(repoLocalPath, repositoryUrl, settings, listener);
	}

	@Override public CloneResult cloneToLocal() {
		return execute(new HgClone(settings.hgPath(), repositoryUrl, repoLocalPath), CloneResult.adapter);
	}

	@Override public UpdateResult update() {
		return execute(new HgUpdate(settings.hgPath(), repoLocalPath), UpdateResult.adapter);
	}

	@Override public LogResult log(TimeRange timeRange) {
		return execute(new HgLog(settings.hgPath(), repoLocalPath, timeRange), LogResult.adapter);
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		HgLogFileContent logFileContent = new HgLogFileContent(settings.hgPath(), repoLocalPath, filePath, revision, settings.defaultFileCharset());
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

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HgVcsRoot hgVcsRoot = (HgVcsRoot) o;

		if (!repoLocalPath.equals(hgVcsRoot.repoLocalPath)) return false;
		if (repositoryUrl != null ? !repositoryUrl.equals(hgVcsRoot.repositoryUrl) : hgVcsRoot.repositoryUrl != null)
			return false;
		return settings.equals(hgVcsRoot.settings);
	}

	@Override public int hashCode() {
		int result = repoLocalPath.hashCode();
		result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
		result = 31 * result + settings.hashCode();
		return result;
	}

	@Override public String toString() {
		return "HgVcsRoot{" +
				"repoLocalPath='" + repoLocalPath + '\'' +
				", repositoryUrl='" + repositoryUrl + '\'' +
				", settings=" + settings +
				'}';
	}
}
