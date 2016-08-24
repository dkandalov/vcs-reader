package org.vcsreader.vcs.svn;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.commandlistener.VcsCommand;

import java.nio.charset.Charset;

import static org.vcsreader.VcsProject.LogFileContentResult;
import static org.vcsreader.vcs.svn.SvnCommandLine.newExternalCommand;
import static org.vcsreader.vcs.svn.SvnCommandLine.isSuccessful;

/**
 * See http://svnbook.red-bean.com/en/1.8/svn.ref.svn.c.cat.html
 */
class SvnLogFileContent implements VcsCommand<LogFileContentResult> {
	private final String svnPath;
	private final String repositoryRoot;
	private final String filePath;
	private final String revision;
	private final Charset charset;
	private final CommandLine commandLine;

	SvnLogFileContent(String svnPath, String repositoryRoot, String filePath, String revision, Charset charset) {
		this.svnPath = svnPath;
		this.repositoryRoot = repositoryRoot;
		this.filePath = filePath;
		this.revision = revision;
		this.charset = charset;
		this.commandLine = svnLogFileContent(svnPath, repositoryRoot, filePath, revision, charset);
	}

	static CommandLine svnLogFileContent(String pathToSvn, String repositoryRoot, String filePath, String revision, Charset charset) {
		String fileRevisionUrl = repositoryRoot + "/" + filePath + "@" + revision;
		return newExternalCommand(pathToSvn, "cat", fileRevisionUrl).outputCharset(charset).charsetAutoDetect(true);
	}

	@NotNull private static String trimLastNewLine(String s) {
		if (s.endsWith("\r\n")) return s.substring(0, s.length() - 2);
		else return s.endsWith("\n") || s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
	}

	@Override public LogFileContentResult execute() {
		commandLine.execute();
		if (isSuccessful(commandLine)) {
			return new LogFileContentResult(trimLastNewLine(commandLine.stdout()));
		} else {
			return new LogFileContentResult(commandLine.stderr(), commandLine.exitCode());
		}
	}

	@Override public String describe() {
		return commandLine.describe();
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SvnLogFileContent that = (SvnLogFileContent) o;

		if (!charset.equals(that.charset)) return false;
		if (!filePath.equals(that.filePath)) return false;
		if (!svnPath.equals(that.svnPath)) return false;
		if (!repositoryRoot.equals(that.repositoryRoot)) return false;
		if (!revision.equals(that.revision)) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = svnPath.hashCode();
		result = 31 * result + repositoryRoot.hashCode();
		result = 31 * result + filePath.hashCode();
		result = 31 * result + revision.hashCode();
		result = 31 * result + charset.hashCode();
		return result;
	}

	@Override public String toString() {
		return "SvnLogFileContent{" +
				"svnPath='" + svnPath + '\'' +
				", repositoryRoot='" + repositoryRoot + '\'' +
				", filePath='" + filePath + '\'' +
				", revision='" + revision + '\'' +
				", charset=" + charset +
				'}';
	}
}
