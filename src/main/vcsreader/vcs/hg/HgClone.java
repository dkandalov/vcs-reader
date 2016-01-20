package vcsreader.vcs.hg;

import vcsreader.VcsProject;
import vcsreader.lang.ExternalCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import static java.util.Arrays.asList;
import static vcsreader.vcs.hg.HgExternalCommand.isSuccessful;

class HgClone implements VcsCommand<VcsProject.CloneResult> {
	private final String pathToHg;
	private final String repositoryUrl;
	private final String localPath;
	private final ExternalCommand externalCommand;

	public HgClone(String pathToHg, String repositoryUrl, String localPath) {
		this.pathToHg = pathToHg;
		this.repositoryUrl = repositoryUrl;
		this.localPath = localPath;
		this.externalCommand = hgClone(pathToHg, repositoryUrl, localPath);
	}

	@Override public VcsProject.CloneResult execute() {
		externalCommand.execute();

		if (isSuccessful(externalCommand)) {
			return new VcsProject.CloneResult();
		} else {
			return new VcsProject.CloneResult(asList(externalCommand.stderr() + externalCommand.exceptionStacktrace()));
		}
	}

	static ExternalCommand hgClone(String pathToHg, String repositoryUrl, String targetPath) {
		return new ExternalCommand(pathToHg, "clone", "-v", repositoryUrl, targetPath);
	}

	@Override public String describe() {
		return externalCommand.describe();
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HgClone hgClone = (HgClone) o;

		if (pathToHg != null ? !pathToHg.equals(hgClone.pathToHg) : hgClone.pathToHg != null) return false;
		if (repositoryUrl != null ? !repositoryUrl.equals(hgClone.repositoryUrl) : hgClone.repositoryUrl != null)
			return false;
		return !(localPath != null ? !localPath.equals(hgClone.localPath) : hgClone.localPath != null);
	}

	@Override public int hashCode() {
		int result = pathToHg != null ? pathToHg.hashCode() : 0;
		result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
		result = 31 * result + (localPath != null ? localPath.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "HgClone{" +
				"pathToHg='" + pathToHg + '\'' +
				", repositoryUrl='" + repositoryUrl + '\'' +
				", localPath='" + localPath + '\'' +
				'}';
	}
}
