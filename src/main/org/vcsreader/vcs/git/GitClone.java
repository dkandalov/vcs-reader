package org.vcsreader.vcs.git;

import org.vcsreader.VcsProject;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.commandlistener.VcsCommand;

import static java.util.Arrays.asList;
import static org.vcsreader.vcs.git.GitCommandLine.isSuccessful;

class GitClone implements VcsCommand<VcsProject.CloneResult> {
	private final String pathToGit;
	private final String repositoryUrl;
	private final String localPath;
	private final CommandLine commandLine;

	public GitClone(String pathToGit, String repositoryUrl, String localPath) {
		this.pathToGit = pathToGit;
		this.repositoryUrl = repositoryUrl;
		this.localPath = localPath;
		this.commandLine = gitClone(pathToGit, repositoryUrl, localPath);
	}

	@Override public VcsProject.CloneResult execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			return new VcsProject.CloneResult();
		} else {
			return new VcsProject.CloneResult(asList(commandLine.stderr()));
		}
	}

	static CommandLine gitClone(String pathToGit, String repositoryUrl, String targetPath) {
		return new CommandLine(pathToGit, "clone", "-v", repositoryUrl, targetPath);
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitClone gitClone = (GitClone) o;

		if (localPath != null ? !localPath.equals(gitClone.localPath) : gitClone.localPath != null) return false;
		if (pathToGit != null ? !pathToGit.equals(gitClone.pathToGit) : gitClone.pathToGit != null) return false;
		if (repositoryUrl != null ? !repositoryUrl.equals(gitClone.repositoryUrl) : gitClone.repositoryUrl != null)
			return false;

		return true;
	}

	@Override public int hashCode() {
		int result = pathToGit != null ? pathToGit.hashCode() : 0;
		result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
		result = 31 * result + (localPath != null ? localPath.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitClone{" +
				"pathToGit='" + pathToGit + '\'' +
				", repositoryUrl='" + repositoryUrl + '\'' +
				", localPath='" + localPath + '\'' +
				'}';
	}
}
