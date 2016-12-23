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
import org.vcsreader.vcs.VcsCommand.ExceptionWrapper;

public class HgVcsRoot implements VcsRoot, VcsCommand.Observer {
	@NotNull private final String repoFolder;
	@Nullable private final String repoUrl;
	@NotNull private final HgSettings settings;
	private final VcsCommand.Listener listener;


	public HgVcsRoot(@NotNull String repoFolder) {
		this(repoFolder, null);
	}

	public HgVcsRoot(@NotNull String repoFolder, @Nullable String repoUrl) {
		this(repoFolder, repoUrl, HgSettings.defaults(), VcsCommand.Listener.none);
	}

	/**
	 * @param repoFolder path to folder with repository from which history will be read.
	 *                   If there is no local clone of repository, you can call {@link #cloneToLocal()} to clone it.
	 * @param repoUrl    URL to remote repository. This can be any form of URL supported by hg command line.
	 * @param settings   settings which will be used by VCS commands executed on this root
	 */
	public HgVcsRoot(@NotNull String repoFolder, @Nullable String repoUrl, @NotNull HgSettings settings) {
		this(repoFolder, repoUrl, settings, VcsCommand.Listener.none);
	}

	private HgVcsRoot(@NotNull String repoFolder, @Nullable String repoUrl,
	                  @NotNull HgSettings settings, VcsCommand.Listener listener) {
		this.repoFolder = repoFolder;
		this.repoUrl = repoUrl;
		this.settings = settings;
		this.listener = listener;
	}

	@Override public HgVcsRoot withListener(VcsCommand.Listener listener) {
		return new HgVcsRoot(repoFolder, repoUrl, settings, listener);
	}

	@Override public CloneResult cloneToLocal() {
		return execute(new HgClone(settings.hgPath(), repoUrl, repoFolder), CloneResult.adapter);
	}

	@Override public UpdateResult update() {
		return execute(new HgUpdate(settings.hgPath(), repoFolder), UpdateResult.adapter);
	}

	@Override public LogResult log(TimeRange timeRange) {
		return execute(new HgLog(settings.hgPath(), repoFolder, timeRange), LogResult.adapter);
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		HgLogFileContent logFileContent = new HgLogFileContent(settings.hgPath(), repoFolder, filePath, revision, settings.defaultFileCharset());
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

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HgVcsRoot hgVcsRoot = (HgVcsRoot) o;

		if (!repoFolder.equals(hgVcsRoot.repoFolder)) return false;
		if (repoUrl != null ? !repoUrl.equals(hgVcsRoot.repoUrl) : hgVcsRoot.repoUrl != null)
			return false;
		return settings.equals(hgVcsRoot.settings);
	}

	@Override public int hashCode() {
		int result = repoFolder.hashCode();
		result = 31 * result + (repoUrl != null ? repoUrl.hashCode() : 0);
		result = 31 * result + settings.hashCode();
		return result;
	}

	@Override public String toString() {
		return "HgVcsRoot{" +
				"repoFolder='" + repoFolder + '\'' +
				", repoUrl='" + repoUrl + '\'' +
				", settings=" + settings +
				'}';
	}
}
