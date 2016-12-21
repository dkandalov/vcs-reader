package org.vcsreader.vcs.git;

import org.vcsreader.CloneResult;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.VcsCommand;

import static java.util.Arrays.asList;
import static org.vcsreader.vcs.git.GitUtil.isSuccessful;

class GitClone implements VcsCommand<CloneResult> {
	private final String pathToGit;
	private final String repoUrl;
	private final String repoFolder;
	private final CommandLine commandLine;

	public GitClone(String pathToGit, String repoUrl, String repoFolder) {
		this.pathToGit = pathToGit;
		this.repoUrl = repoUrl;
		this.repoFolder = repoFolder;
		this.commandLine = gitClone(pathToGit, repoUrl, repoFolder);
	}

	@Override public CloneResult execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			return new CloneResult();
		} else {
			return new CloneResult(asList(commandLine.stderr()));
		}
	}

	static CommandLine gitClone(String pathToGit, String repoUrl, String targetPath) {
		return new CommandLine(pathToGit, "clone", "-v", repoUrl, targetPath);
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitClone gitClone = (GitClone) o;

		if (repoFolder != null ? !repoFolder.equals(gitClone.repoFolder) : gitClone.repoFolder != null) return false;
		if (pathToGit != null ? !pathToGit.equals(gitClone.pathToGit) : gitClone.pathToGit != null) return false;
		if (repoUrl != null ? !repoUrl.equals(gitClone.repoUrl) : gitClone.repoUrl != null)
			return false;

		return true;
	}

	@Override public int hashCode() {
		int result = pathToGit != null ? pathToGit.hashCode() : 0;
		result = 31 * result + (repoUrl != null ? repoUrl.hashCode() : 0);
		result = 31 * result + (repoFolder != null ? repoFolder.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitClone{" +
				"pathToGit='" + pathToGit + '\'' +
				", repoUrl='" + repoUrl + '\'' +
				", repoFolder='" + repoFolder + '\'' +
				'}';
	}
}
