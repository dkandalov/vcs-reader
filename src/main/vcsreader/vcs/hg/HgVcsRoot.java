package vcsreader.vcs.hg;

import vcsreader.VcsProject.CloneResult;
import vcsreader.VcsProject.LogFileContentResult;
import vcsreader.VcsProject.LogResult;
import vcsreader.VcsProject.UpdateResult;
import vcsreader.VcsRoot;
import vcsreader.vcs.commandlistener.VcsCommandObserver;

import java.util.Date;

public class HgVcsRoot implements VcsRoot, VcsRoot.WithCommandObserver {
    public final String localPath;
    public final String repositoryUrl;
    public final HgSettings settings;
    private VcsCommandObserver observer;


	public HgVcsRoot(String repositoryUrl, String localPath) {
		this(repositoryUrl, localPath, HgSettings.defaults());
	}

	/**
	 * @param localPath path to folder with repository clone
	 *                  (if folder doesn't exit, it will be created on {@link #cloneToLocal()}.
	 * @param repositoryUrl url to remote repository (only required for {@link #cloneToLocal()})
	 */
	public HgVcsRoot(String localPath, String repositoryUrl, HgSettings settings) {
        this.localPath = localPath;
        this.repositoryUrl = repositoryUrl;
        this.settings = settings;
    }

    @Override public CloneResult cloneToLocal() {
        return observer.executeAndObserve(new HgClone(settings.hgPath, repositoryUrl, localPath));
    }

    @Override public UpdateResult update() {
        return observer.executeAndObserve(new HgUpdate(settings.hgPath, localPath));
    }

    @Override public LogResult log(Date fromDate, Date toDate) {
        return observer.executeAndObserve(new HgLog(settings.hgPath, localPath, fromDate, toDate));
    }

    @Override public LogFileContentResult logFileContent(String filePath, String revision) {
        return observer.executeAndObserve(new HgLogFileContent(settings.hgPath, localPath, filePath, revision, settings.filesCharset));
    }

    @Override public void setObserver(VcsCommandObserver observer) {
        this.observer = observer;
    }

	@SuppressWarnings("RedundantIfStatement")
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
