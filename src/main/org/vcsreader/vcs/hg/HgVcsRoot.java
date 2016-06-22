package org.vcsreader.vcs.hg;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vcsreader.VcsProject.CloneResult;
import org.vcsreader.VcsProject.LogFileContentResult;
import org.vcsreader.VcsProject.LogResult;
import org.vcsreader.VcsProject.UpdateResult;
import org.vcsreader.VcsRoot;
import org.vcsreader.vcs.commandlistener.VcsCommand;

import java.util.Date;

import static org.vcsreader.vcs.commandlistener.VcsCommand.Listener.executeWith;

public class HgVcsRoot implements VcsRoot, VcsCommand.Owner {
	public final String localPath;
	public final String repositoryUrl;
	public final HgSettings settings;
	private final VcsCommand.Listener listener;


	public HgVcsRoot(@NotNull String localPath) {
		this(localPath, null);
	}

	public HgVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl) {
		this(localPath, repositoryUrl, HgSettings.defaults(), VcsCommand.Listener.none);
	}

	/**
	 * @param localPath     path to folder with repository clone
	 *                      (if folder doesn't exit, it will be created on {@link #cloneToLocal()}.
	 * @param repositoryUrl url to remote repository (only required for {@link #cloneToLocal()})
	 */
	public HgVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl, HgSettings settings) {
		this(localPath, repositoryUrl, settings, VcsCommand.Listener.none);
	}

	private HgVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl,
	                 HgSettings settings, VcsCommand.Listener listener) {
		this.localPath = localPath;
		this.repositoryUrl = repositoryUrl;
		this.settings = settings;
		this.listener = listener;
	}

	@Override public HgVcsRoot withListener(VcsCommand.Listener listener) {
		return new HgVcsRoot(localPath, repositoryUrl, settings, listener);
	}

	@Override public CloneResult cloneToLocal() {
		return executeWith(listener, new HgClone(settings.hgPath, repositoryUrl, localPath));
	}

	@Override public UpdateResult update() {
		return executeWith(listener, new HgUpdate(settings.hgPath, localPath));
	}

	@Override public LogResult log(Date fromDate, Date toDate) {
		return executeWith(listener, new HgLog(settings.hgPath, localPath, fromDate, toDate));
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		return executeWith(listener, new HgLogFileContent(settings.hgPath, localPath, filePath, revision, settings.defaultFileCharset));
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HgVcsRoot hgVcsRoot = (HgVcsRoot) o;

		if (localPath != null ? !localPath.equals(hgVcsRoot.localPath) : hgVcsRoot.localPath != null) return false;
		if (repositoryUrl != null ? !repositoryUrl.equals(hgVcsRoot.repositoryUrl) : hgVcsRoot.repositoryUrl != null)
			return false;
		return settings != null ? settings.equals(hgVcsRoot.settings) : hgVcsRoot.settings == null;
	}

	@Override public int hashCode() {
		int result = localPath != null ? localPath.hashCode() : 0;
		result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
		result = 31 * result + (settings != null ? settings.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "HgVcsRoot{" +
				"localPath='" + localPath + '\'' +
				", repositoryUrl='" + repositoryUrl + '\'' +
				", settings=" + settings +
				'}';
	}
}
