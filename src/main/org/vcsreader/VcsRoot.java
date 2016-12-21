package org.vcsreader;

import org.vcsreader.lang.TimeRange;

/**
 * Represents repository url (and local cloned folder for distributed VCS).
 * <p>
 * For reading VCS history please use {@link VcsProject} API.
 */
public interface VcsRoot {
	CloneResult cloneToLocal();

	UpdateResult update();

	LogResult log(TimeRange timeRange);

	LogFileContentResult logFileContent(String filePath, String revision);
}
