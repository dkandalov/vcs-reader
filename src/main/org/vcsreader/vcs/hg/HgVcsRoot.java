package org.vcsreader.vcs.hg;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vcsreader.VcsProject.CloneResult;
import org.vcsreader.VcsProject.LogFileContentResult;
import org.vcsreader.VcsProject.LogResult;
import org.vcsreader.VcsProject.UpdateResult;
import org.vcsreader.VcsRoot;
import org.vcsreader.vcs.commandlistener.VcsCommandExecutor;

import java.util.Date;

public class HgVcsRoot implements VcsRoot, VcsRoot.WithCommandExecutor {
	public final String localPath;
	public final String repositoryUrl;
	public final HgSettings settings;
	private VcsCommandExecutor commandExecutor;


	public HgVcsRoot(@NotNull String localPath) {
		this(localPath, null, HgSettings.defaults());
	}

	public HgVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl) {
		this(localPath, repositoryUrl, HgSettings.defaults());
	}

	/**
	 * @param localPath     path to folder with repository clone
	 *                      (if folder doesn't exit, it will be created on {@link #cloneToLocal()}.
	 * @param repositoryUrl url to remote repository (only required for {@link #cloneToLocal()})
	 */
	public HgVcsRoot(@NotNull String localPath, @Nullable String repositoryUrl, HgSettings settings) {
		this.localPath = localPath;
		this.repositoryUrl = repositoryUrl;
		this.settings = settings;
	}

	@Override public CloneResult cloneToLocal() {
		return commandExecutor.executeAndObserve(new HgClone(settings.hgPath, repositoryUrl, localPath));
	}

	@Override public UpdateResult update() {
		return commandExecutor.executeAndObserve(new HgUpdate(settings.hgPath, localPath));
	}

	@Override public LogResult log(Date fromDate, Date toDate) {
		return commandExecutor.executeAndObserve(new HgLog(settings.hgPath, localPath, fromDate, toDate));
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		return commandExecutor.executeAndObserve(new HgLogFileContent(settings.hgPath, localPath, filePath, revision, settings.defaultFileCharset));
	}

	public void setCommandExecutor(VcsCommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
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
