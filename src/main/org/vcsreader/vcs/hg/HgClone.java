package org.vcsreader.vcs.hg;

import org.vcsreader.CloneResult;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.VcsCommand;

import static java.util.Arrays.asList;
import static org.vcsreader.vcs.hg.HgUtil.isSuccessful;

class HgClone implements VcsCommand<CloneResult> {
	private final String pathToHg;
	private final String repositoryUrl;
	private final String localPath;
	private final CommandLine commandLine;

	public HgClone(String pathToHg, String repositoryUrl, String localPath) {
		this.pathToHg = pathToHg;
		this.repositoryUrl = repositoryUrl;
		this.localPath = localPath;
		this.commandLine = hgClone(pathToHg, repositoryUrl, localPath);
	}

	@Override public CloneResult execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			return new CloneResult();
		} else {
			return new CloneResult(asList(commandLine.stderr()));
		}
	}

	static CommandLine hgClone(String pathToHg, String repositoryUrl, String targetPath) {
		return new CommandLine(pathToHg, "clone", "-v", repositoryUrl, targetPath);
	}

	@Override public String describe() {
		return commandLine.describe();
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
