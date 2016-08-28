package org.vcsreader.vcs.svn;

import org.vcsreader.VcsRoot;
import org.vcsreader.vcs.commandlistener.VcsCommand;
import org.vcsreader.vcs.commandlistener.VcsCommand.ResultAdapter;

import java.util.Date;

import static org.vcsreader.VcsProject.*;

public class SvnVcsRoot implements VcsRoot, VcsCommand.Owner {
	public final String repositoryUrl;
	public final SvnSettings settings;
	private final VcsCommand.Listener listener;
	private String repositoryRoot;
	private boolean quoteDateRange = false;


	public SvnVcsRoot(String repositoryUrl) {
		this(repositoryUrl, SvnSettings.defaults());
	}

	public SvnVcsRoot(String repositoryUrl, SvnSettings settings) {
		this(repositoryUrl, settings, VcsCommand.Listener.none);
	}

	private SvnVcsRoot(String repositoryUrl, SvnSettings settings, VcsCommand.Listener listener) {
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

	@Override public LogResult log(Date fromDate, Date toDate) {
		if (repositoryRoot == null) {
			SvnInfo.Result result = execute(new SvnInfo(settings.svnPath, repositoryUrl), SvnInfo.adapter);
			repositoryRoot = result.repositoryRoot;
		}
		LogResult logResult = execute(svnLog(fromDate, toDate), LogResult.adapter);
		if (hasRevisionArgumentError(logResult)) {
			quoteDateRange = !quoteDateRange;
			logResult = execute(svnLog(fromDate, toDate), LogResult.adapter);
		}
		return logResult;
	}

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		SvnLogFileContent logFileContent = new SvnLogFileContent(
				settings.svnPath,
				repositoryUrl,
				filePath,
				revision,
				settings.defaultFileCharset
		);
		return execute(logFileContent, LogFileContentResult.adapter);
	}

	private SvnLog svnLog(Date fromDate, Date toDate) {
		return new SvnLog(
				settings.svnPath,
				repositoryUrl,
				repositoryRoot,
				fromDate, toDate,
				settings.useMergeHistory,
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
		return VcsCommand.execute(vcsCommand, resultAdapter, listener, settings.failFast);
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnVcsRoot that = (SvnVcsRoot) o;

		return repositoryUrl != null ? repositoryUrl.equals(that.repositoryUrl) : that.repositoryUrl == null &&
				(settings != null ? settings.equals(that.settings) : that.settings == null);
	}

	@Override public int hashCode() {
		int result = repositoryUrl != null ? repositoryUrl.hashCode() : 0;
		result = 31 * result + (settings != null ? settings.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "SvnVcsRoot{" +
				"settings=" + settings +
				", repositoryUrl='" + repositoryUrl + '\'' +
				'}';
	}
}
