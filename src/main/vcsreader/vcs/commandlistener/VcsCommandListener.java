package vcsreader.vcs.commandlistener;

public interface VcsCommandListener {
	void beforeCommand(VcsCommand<?> command);

	void afterCommand(VcsCommand<?> command);
}
