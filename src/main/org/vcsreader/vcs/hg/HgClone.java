package org.vcsreader.vcs.hg;

import org.vcsreader.CloneResult;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.VcsCommand;

import static org.vcsreader.vcs.hg.HgUtil.isSuccessful;

class HgClone implements VcsCommand<CloneResult> {
	private final String pathToHg;
	private final String repoUrl;
	private final String repoFolder;
	private final CommandLine commandLine;

	public HgClone(String pathToHg, String repoUrl, String repoFolder) {
		this.pathToHg = pathToHg;
		this.repoUrl = repoUrl;
		this.repoFolder = repoFolder;
		this.commandLine = hgClone(pathToHg, repoUrl, repoFolder);
	}

	@Override public CloneResult execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			return new CloneResult();
		} else {
			return new CloneResult(commandLine.stderr());
		}
	}

	static CommandLine hgClone(String pathToHg, String repoUrl, String targetPath) {
		return new CommandLine(pathToHg, "clone", "-v", repoUrl, targetPath);
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@Override public boolean cancel() {
		return commandLine.kill();
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HgClone hgClone = (HgClone) o;

		if (pathToHg != null ? !pathToHg.equals(hgClone.pathToHg) : hgClone.pathToHg != null) return false;
		if (repoUrl != null ? !repoUrl.equals(hgClone.repoUrl) : hgClone.repoUrl != null)
			return false;
		return !(repoFolder != null ? !repoFolder.equals(hgClone.repoFolder) : hgClone.repoFolder != null);
	}

	@Override public int hashCode() {
		int result = pathToHg != null ? pathToHg.hashCode() : 0;
		result = 31 * result + (repoUrl != null ? repoUrl.hashCode() : 0);
		result = 31 * result + (repoFolder != null ? repoFolder.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "HgClone{" +
				"pathToHg='" + pathToHg + '\'' +
				", repoUrl='" + repoUrl + '\'' +
				", repoFolder='" + repoFolder + '\'' +
				'}';
	}
}
