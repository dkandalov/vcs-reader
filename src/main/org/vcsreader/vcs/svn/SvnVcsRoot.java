package org.vcsreader.vcs.svn;

import org.vcsreader.VcsRoot;
import org.vcsreader.vcs.commandlistener.VcsCommand;

import java.util.Date;

import static org.vcsreader.VcsProject.*;
import static org.vcsreader.vcs.commandlistener.VcsCommand.Listener.executeWith;

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
			SvnInfo.Result result = executeWith(listener, new SvnInfo(settings.svnPath, repositoryUrl));
			repositoryRoot = result.repositoryRoot;
		}
		LogResult logResult = executeWith(listener, svnLog(fromDate, toDate));
		if (hasRevisionArgumentError(logResult)) {
			quoteDateRange = !quoteDateRange;
			logResult = executeWith(listener, svnLog(fromDate, toDate));
		}
		return logResult;
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

	@Override public LogFileContentResult logFileContent(String filePath, String revision) {
		return executeWith(listener, new SvnLogFileContent(
				settings.svnPath,
				repositoryUrl,
				filePath,
				revision,
				settings.defaultFileCharset
		));
	}

	@Override public String toString() {
		return "SvnVcsRoot{" +
				"settings=" + settings +
				", repositoryUrl='" + repositoryUrl + '\'' +
				'}';
	}
}
