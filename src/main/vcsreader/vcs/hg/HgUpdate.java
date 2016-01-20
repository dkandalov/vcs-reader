package vcsreader.vcs.hg;

import vcsreader.VcsProject;
import vcsreader.lang.ExternalCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import static vcsreader.vcs.hg.HgExternalCommand.isSuccessful;

class HgUpdate implements VcsCommand<VcsProject.UpdateResult> {
	private final String hgPath;
	private final String folder;
	private final ExternalCommand externalCommand;

	public HgUpdate(String hgPath, String folder) {
		this.hgPath = hgPath;
		this.folder = folder;
		this.externalCommand = hgUpdate(hgPath, folder);
	}

	@Override public VcsProject.UpdateResult execute() {
		externalCommand.execute();
		if (isSuccessful(externalCommand)) {
			return new VcsProject.UpdateResult();
		} else {
			return new VcsProject.UpdateResult(externalCommand.stderr() + externalCommand.exceptionStacktrace());
		}
	}

	static ExternalCommand hgUpdate(String pathToHg, String folder) {
		return new ExternalCommand(pathToHg, "pull").workingDir(folder);
	}

	@Override public String describe() {
		return externalCommand.describe();
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
