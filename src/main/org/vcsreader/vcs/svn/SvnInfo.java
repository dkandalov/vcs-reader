package org.vcsreader.vcs.svn;

import org.jetbrains.annotations.Nullable;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.VcsCommand;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.vcsreader.vcs.svn.SvnUtil.isSuccessful;
import static org.vcsreader.vcs.svn.SvnUtil.newExternalCommand;


class SvnInfo implements VcsCommand<SvnInfo.Result> {
	public static ResultAdapter<Result> adapter = new ResultAdapter<Result>() {
		@Override public Result wrapException(Exception e) {
			return new Result(e);
		}

		@Override public boolean isSuccessful(Result result) {
			return result.isSuccessful();
		}

		@Override public List<String> vcsErrorsIn(Result result) {
			return result.errors;
		}
	};
	private final String svnPath;
	private final String repoUrl;
	private final CommandLine commandLine;

	public SvnInfo(String svnPath, String repoUrl) {
		this.svnPath = svnPath;
		this.repoUrl = repoUrl;
		this.commandLine = svnInfo(svnPath, repoUrl);
	}

	@Override public SvnInfo.Result execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			String repoRoot = parse(commandLine.stdout());
			if (repoRoot == null) {
				return new Result(asList("Didn't find svn root in output for " + repoUrl));
			} else {
				return new Result(repoRoot);
			}
		} else {
			return new Result(asList(commandLine.stdout()));
		}
	}

	static CommandLine svnInfo(String svnPath, String repoUrl) {
		return newExternalCommand(svnPath, "info", repoUrl);
	}

	@Nullable private static String parse(String stdout) {
		String[] lines = stdout.split("\n");
		for (String line : lines) {
			if (line.contains("Repository Root:")) {
				return line.replace("Repository Root:", "").trim();
			}
		}
		return null;
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnInfo svnInfo = (SvnInfo) o;

		if (repoUrl != null ? !repoUrl.equals(svnInfo.repoUrl) : svnInfo.repoUrl != null) return false;
		if (svnPath != null ? !svnPath.equals(svnInfo.svnPath) : svnInfo.svnPath != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = svnPath != null ? svnPath.hashCode() : 0;
		result = 31 * result + (repoUrl != null ? repoUrl.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "SvnInfo{" +
				"svnPath='" + svnPath + '\'' +
				", repoUrl='" + repoUrl + '\'' +
				'}';
	}


	public static class Result {
		public static final String unknownRoot = "";

		public final String repoRoot;
		private final List<String> errors;
		private final Exception exception;

		public Result(String repoRoot) {
			this(repoRoot, new ArrayList<>(), null);
		}

		public Result(List<String> errors) {
			this(unknownRoot, errors, null);
		}

		public Result(Exception exception) {
			this(unknownRoot, emptyList(), exception);
		}

		private Result(String repoRoot, List<String> errors, Exception exception) {
			this.repoRoot = repoRoot;
			this.errors = errors;
			this.exception = exception;
		}

		public boolean isSuccessful() {
			return errors.isEmpty() && exception == null;
		}
	}
}
