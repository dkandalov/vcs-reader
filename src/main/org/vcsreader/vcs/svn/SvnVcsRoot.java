package org.vcsreader.vcs.svn;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.*;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsCommand.ResultAdapter;

public class SvnVcsRoot implements VcsRoot, VcsCommand.Owner {
	@NotNull private final String repositoryUrl;
	@NotNull private final SvnSettings settings;
	private final VcsCommand.Listener listener;
	private String repositoryRoot;
	private boolean quoteDateRange = false;


	public SvnVcsRoot(@NotNull String repositoryUrl) {
		this(repositoryUrl, SvnSettings.defaults());
	}

	public SvnVcsRoot(@NotNull String repositoryUrl, @NotNull SvnSettings settings) {
		this(repositoryUrl, settings, VcsCommand.Listener.none);
	}

	private SvnVcsRoot(@NotNull String repositoryUrl, @NotNull SvnSettings settings, VcsCommand.Listener listener) {
		this.repositoryUrl = repositoryUrl;
		this.settings = settings;
		this.listener = listener;
	}

	@Override public SvnVcsRoot withListener(VcsCommand.Listener listener) {
		return new SvnVcsRoot(repositoryUrl, settings, listener);
	}

	@Override public CloneResult cloneToLocal() {
		return new CloneResult();
	}

	@Override public UpdateResult update() {
		return new UpdateResult();
	}

	@Override public LogResult log(TimeRange timeRange) {
		if (repositoryRoot == null) {
			SvnInfo.Result result = execute(new SvnInfo(settings.svnPath(), repositoryUrl), SvnInfo.adapter);
			repositoryRoot = result.repositoryRoot;
		}
		LogResult logResult = execute(svnLog(timeRange), LogResult.adapter);
		if (hasRevisionArgumentError(logResult)) {
			quoteDateRange = !quoteDateRange;
			logResult = execute(svnLog(timeRange), LogResult.adapter);
		}
		return logResult;
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		SvnLogFileContent logFileContent = new SvnLogFileContent(
				settings.svnPath(),
				repositoryUrl,
				filePath,
				revision,
				settings.defaultFileCharset()
		);
		return execute(logFileContent, LogFileContentResult.adapter);
	}

	private SvnLog svnLog(TimeRange timeRange) {
		return new SvnLog(
				settings.svnPath(),
				repositoryUrl,
				repositoryRoot,
				timeRange,
				settings.useMergeHistory(),
				quoteDateRange
		);
	}

	/**
	 * This is workaround for error in cygwin when "{}" in date range argument
	 * are interpreted by shell and, therefore, need quoting.
	 * At the same time quoting isn't required and doesn't work when running from other shells.
	 */
	private static boolean hasRevisionArgumentError(LogResult logResult) {
		if (logResult.isSuccessful()) return false;
		for (String error : logResult.vcsErrors()) {
			if (error.contains("E205000: Syntax error in revision argument")) {
				return true;
			}
		}
		return false;
	}

	private <T> T execute(VcsCommand<T> vcsCommand, ResultAdapter<T> resultAdapter) {
		return VcsCommand.execute(vcsCommand, resultAdapter, listener, settings.failFast());
	}

	@NotNull public String repositoryUrl() {
		return repositoryUrl;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnVcsRoot that = (SvnVcsRoot) o;

		return repositoryUrl.equals(that.repositoryUrl) && settings.equals(that.settings);
	}

	@Override public int hashCode() {
		int result = repositoryUrl.hashCode();
		result = 31 * result + settings.hashCode();
		return result;
	}

	@Override public String toString() {
		return "SvnVcsRoot{" +
				"settings=" + settings +
				", repositoryUrl='" + repositoryUrl + '\'' +
				'}';
	}
}
