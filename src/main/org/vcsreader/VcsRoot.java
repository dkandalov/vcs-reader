package org.vcsreader;

import org.vcsreader.vcs.commandlistener.VcsCommandObserver;

import java.util.Date;

import static org.vcsreader.VcsProject.*;

/**
 * Represents repository url (and local cloned folder for distributed VCS).
 * <p>
 * For reading VCS history please use {@link VcsProject} API.
 */
public interface VcsRoot {
	CloneResult cloneToLocal();

	UpdateResult update();

	LogResult log(Date fromDate, Date toDate);

	LogFileContentResult logFileContent(String filePath, String revision);

	interface WithCommandObserver {
		void setObserver(VcsCommandObserver commandExecutor);
	}
}
