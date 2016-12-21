package org.vcsreader.vcs.hg;

import org.vcsreader.UpdateResult;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.VcsCommand;

import static org.vcsreader.vcs.hg.HgUtil.isSuccessful;

class HgUpdate implements VcsCommand<UpdateResult> {
	private final String hgPath;
	private final String folder;
	private final CommandLine commandLine;

	public HgUpdate(String hgPath, String folder) {
		this.hgPath = hgPath;
		this.folder = folder;
		this.commandLine = hgUpdate(hgPath, folder);
	}

	@Override public UpdateResult execute() {
		commandLine.execute();
		if (isSuccessful(commandLine)) {
			return new UpdateResult();
		} else {
			return new UpdateResult(commandLine.stderr());
		}
	}

	static CommandLine hgUpdate(String pathToHg, String folder) {
		return new CommandLine(pathToHg, "pull").workingDir(folder);
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HgUpdate hgUpdate = (HgUpdate) o;

		return !(hgPath != null ? !hgPath.equals(hgUpdate.hgPath) : hgUpdate.hgPath != null) && !(folder != null ? !folder.equals(hgUpdate.folder) : hgUpdate.folder != null);
	}

	@Override public int hashCode() {
		int result = hgPath != null ? hgPath.hashCode() : 0;
		result = 31 * result + (folder != null ? folder.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "HgUpdate{" +
				"hgPath='" + hgPath + '\'' +
				", folder='" + folder + '\'' +
				'}';
	}
}
