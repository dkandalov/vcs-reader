package org.vcsreader.vcs.git;

import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.VcsCommand;

import org.vcsreader.UpdateResult;
import static org.vcsreader.vcs.git.GitUtil.isSuccessful;

class GitUpdate implements VcsCommand<UpdateResult> {
	private final String gitPath;
	private final String repoFolder;
	private final CommandLine commandLine;

	public GitUpdate(String gitPath, String repoFolder) {
		this.gitPath = gitPath;
		this.repoFolder = repoFolder;
		this.commandLine = gitUpdate(gitPath, repoFolder);
	}

	@Override public UpdateResult execute() {
		commandLine.execute();
		if (isSuccessful(commandLine)) {
			return new UpdateResult();
		} else {
			return new UpdateResult(commandLine.stderr());
		}
	}

	static CommandLine gitUpdate(String pathToGit, String repoFolder) {
		return new CommandLine(pathToGit, "pull", "origin").workingDir(repoFolder);
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitUpdate gitUpdate = (GitUpdate) o;

		if (repoFolder != null ? !repoFolder.equals(gitUpdate.repoFolder) : gitUpdate.repoFolder != null) return false;
		if (gitPath != null ? !gitPath.equals(gitUpdate.gitPath) : gitUpdate.gitPath != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = gitPath != null ? gitPath.hashCode() : 0;
		result = 31 * result + (repoFolder != null ? repoFolder.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitUpdate{" +
				"gitPath='" + gitPath + '\'' +
				", repoFolder='" + repoFolder + '\'' +
				'}';
	}
}
