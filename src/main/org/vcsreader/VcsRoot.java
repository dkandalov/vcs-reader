package org.vcsreader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vcsreader.lang.TimeRange;

/**
 * Represents VCS repository.
 * <p>
 * For clone/log/update use {@link VcsProject} API.
 */
public interface VcsRoot {

	@NotNull String repoFolder();

	@Nullable String repoUrl();

	CloneResult cloneToLocal();

	UpdateResult update();

	LogResult log(TimeRange timeRange);

	LogFileContentResult logFileContent(String filePath, String revision);

	boolean cancelLastCommand();
}
