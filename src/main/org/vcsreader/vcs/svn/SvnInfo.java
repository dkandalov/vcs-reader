package org.vcsreader.vcs.svn;

import org.jetbrains.annotations.Nullable;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.commandlistener.VcsCommand;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.vcsreader.vcs.svn.SvnInfo.Result.unknownRoot;
import static org.vcsreader.vcs.svn.SvnCommandLine.newExternalCommand;
import static org.vcsreader.vcs.svn.SvnCommandLine.isSuccessful;


class SvnInfo implements VcsCommand<SvnInfo.Result> {
	private final String svnPath;
	private final String repositoryUrl;
	private final CommandLine commandLine;

	public SvnInfo(String svnPath, String repositoryUrl) {
		this.svnPath = svnPath;
		this.repositoryUrl = repositoryUrl;
		this.commandLine = svnInfo(svnPath, repositoryUrl);
	}

	@Override public SvnInfo.Result execute() {
		commandLine.execute();

		if (isSuccessful(commandLine)) {
			String repositoryRoot = parse(commandLine.stdout());
			if (repositoryRoot == null) {
				return new Result(unknownRoot, asList("Didn't find svn root in output for " + repositoryUrl));
			} else {
				return new Result(repositoryRoot);
			}
		} else {
			return new Result(unknownRoot, asList(commandLine.stdout() + commandLine.exceptionStacktrace()));
		}
	}

	static CommandLine svnInfo(String svnPath, String repositoryUrl) {
		return newExternalCommand(svnPath, "info", repositoryUrl);
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

		if (repositoryUrl != null ? !repositoryUrl.equals(svnInfo.repositoryUrl) : svnInfo.repositoryUrl != null)
			return false;
		if (svnPath != null ? !svnPath.equals(svnInfo.svnPath) : svnInfo.svnPath != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = svnPath != null ? svnPath.hashCode() : 0;
		result = 31 * result + (repositoryUrl != null ? repositoryUrl.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "SvnInfo{" +
				"svnPath='" + svnPath + '\'' +
				", repositoryUrl='" + repositoryUrl + '\'' +
				'}';
	}


	public static class Result {
		public static String unknownRoot = "";

		public final String repositoryRoot;
		private final List<String> errors;

		public Result(String repositoryRoot) {
			this(repositoryRoot, new ArrayList<String>());
		}

		public Result(String repositoryRoot, List<String> errors) {
			this.repositoryRoot = repositoryRoot;
			this.errors = errors;
		}

		public List<String> errors() {
			return errors;
		}

		public boolean isSuccessful() {
			return errors.isEmpty();
		}
	}
}
