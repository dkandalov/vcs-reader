package org.vcsreader.vcs.git;

import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.VcsCommand;

import static org.vcsreader.VcsProject.UpdateResult;
import static org.vcsreader.vcs.git.GitCommandLine.isSuccessful;

class GitUpdate implements VcsCommand<UpdateResult> {
	private final String gitPath;
	private final String folder;
	private final CommandLine commandLine;

	public GitUpdate(String gitPath, String folder) {
		this.gitPath = gitPath;
		this.folder = folder;
		this.commandLine = gitUpdate(gitPath, folder);
	}

	@Override public UpdateResult execute() {
		commandLine.execute();
		if (isSuccessful(commandLine)) {
			return new UpdateResult();
		} else {
			return new UpdateResult(commandLine.stderr());
		}
	}

	static CommandLine gitUpdate(String pathToGit, String folder) {
		return new CommandLine(pathToGit, "pull", "origin").workingDir(folder);
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitUpdate gitUpdate = (GitUpdate) o;

		if (folder != null ? !folder.equals(gitUpdate.folder) : gitUpdate.folder != null) return false;
		if (gitPath != null ? !gitPath.equals(gitUpdate.gitPath) : gitUpdate.gitPath != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = gitPath != null ? gitPath.hashCode() : 0;
		result = 31 * result + (folder != null ? folder.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitUpdate{" +
				"gitPath='" + gitPath + '\'' +
				", folder='" + folder + '\'' +
				'}';
	}
}
