package org.vcsreader.vcs.svn;

import org.vcsreader.VcsRoot;
import org.vcsreader.vcs.commandlistener.VcsCommandExecutor;

import java.util.Date;

import static org.vcsreader.VcsProject.*;

public class SvnVcsRoot implements VcsRoot, VcsRoot.WithCommandExecutor {
	public final String repositoryUrl;
	public final SvnSettings settings;
	private String repositoryRoot;
	private boolean quoteDateRange = false;
	private VcsCommandExecutor commandExecutor;


	public SvnVcsRoot(String repositoryUrl) {
		this(repositoryUrl, SvnSettings.defaults());
	}

	public SvnVcsRoot(String repositoryUrl, SvnSettings settings) {
		this.repositoryUrl = repositoryUrl;
		this.settings = settings;
	}

	@Override public CloneResult cloneToLocal() {
		return new CloneResult();
	}

	@Override public UpdateResult update() {
		return new UpdateResult();
	}

	@Override public LogResult log(Date fromDate, Date toDate) {
		if (repositoryRoot == null) {
			SvnInfo.Result result = commandExecutor.executeAndObserve(new SvnInfo(settings.svnPath, repositoryUrl));
			repositoryRoot = result.repositoryRoot;
		}
		LogResult logResult = commandExecutor.executeAndObserve(svnLog(fromDate, toDate));
		if (hasRevisionArgumentError(logResult)) {
			quoteDateRange = !quoteDateRange;
			logResult = commandExecutor.executeAndObserve(svnLog(fromDate, toDate));
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
		return commandExecutor.executeAndObserve(new SvnLogFileContent(
				settings.svnPath,
				repositoryUrl,
				filePath,
				revision,
				settings.defaultFileCharset
		));
	}

	public void setCommandExecutor(VcsCommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	@Override public String toString() {
		return "SvnVcsRoot{" +
				"settings=" + settings +
				", repositoryUrl='" + repositoryUrl + '\'' +
				'}';
	}
}
