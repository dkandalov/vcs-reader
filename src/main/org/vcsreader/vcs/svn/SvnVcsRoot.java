package org.vcsreader.vcs.svn;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.*;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsCommand.ExceptionWrapper;

public class SvnVcsRoot implements VcsRoot, VcsCommand.Observer {
	@NotNull private final String repoUrl;
	@NotNull private final SvnSettings settings;
	private final VcsCommand.Listener listener;
	private String repoRoot;
	private boolean quoteDateRange = false;
	private volatile VcsCommand lastCommand;


	public SvnVcsRoot(@NotNull String repoUrl) {
		this(repoUrl, SvnSettings.defaults());
	}

	/**
	 * @param repoUrl  svn repository URL. This can be any form of URL supported by svn command line.
	 * @param settings settings which will be used by VCS commands executed on this root
	 */
	public SvnVcsRoot(@NotNull String repoUrl, @NotNull SvnSettings settings) {
		this(repoUrl, settings, VcsCommand.Listener.none);
	}

	private SvnVcsRoot(@NotNull String repoUrl, @NotNull SvnSettings settings, VcsCommand.Listener listener) {
		this.repoUrl = repoUrl;
		this.settings = settings;
		this.listener = listener;
	}

	@Override public SvnVcsRoot withListener(VcsCommand.Listener listener) {
		return new SvnVcsRoot(repoUrl, settings, listener);
	}

	@Override public CloneResult cloneToLocal() {
		return new CloneResult();
	}

	@Override public UpdateResult update() {
		return new UpdateResult();
	}

	@Override public LogResult log(TimeRange timeRange) {
		if (repoRoot == null) {
			SvnInfo.Result result = execute(new SvnInfo(settings.svnPath(), repoUrl), SvnInfo.adapter);
			repoRoot = result.repoRoot;
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
				repoUrl,
				filePath,
				revision,
				settings.defaultFileCharset()
		);
		return execute(logFileContent, LogFileContentResult.adapter);
	}

	private SvnLog svnLog(TimeRange timeRange) {
		return new SvnLog(
				settings.svnPath(),
				repoUrl,
				repoRoot,
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
		for (Exception e : logResult.exceptions()) {
			if (e.getMessage().contains("E205000: Syntax error in revision argument")) {
				return true;
			}
		}
		return false;
	}

	private <T> T execute(VcsCommand<T> vcsCommand, ExceptionWrapper<T> exceptionWrapper) {
		try {
			lastCommand = vcsCommand;
			return VcsCommand.execute(vcsCommand, exceptionWrapper, listener, settings.failFast());
		} finally {
			lastCommand = null;
		}
	}

	@NotNull @Override public String repoFolder() {
		return "";
	}

	@Override @NotNull public String repoUrl() {
		return repoUrl;
	}

	@Override public boolean cancelLastCommand() {
		VcsCommand command = lastCommand;
		return command == null || command.cancel();
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnVcsRoot that = (SvnVcsRoot) o;

		return repoUrl.equals(that.repoUrl) && settings.equals(that.settings);
	}

	@Override public int hashCode() {
		int result = repoUrl.hashCode();
		result = 31 * result + settings.hashCode();
		return result;
	}

	@Override public String toString() {
		return "SvnVcsRoot{" +
				"settings=" + settings +
				", repoUrl='" + repoUrl + '\'' +
				'}';
	}
}
