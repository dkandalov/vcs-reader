package org.vcsreader.vcs.git;

import org.vcsreader.LogFileContentResult;
import org.vcsreader.lang.CommandLine;
import org.vcsreader.vcs.VcsCommand;

import java.nio.charset.Charset;

import static org.vcsreader.lang.StringUtil.trimLastNewLine;
import static org.vcsreader.vcs.git.GitUtil.isSuccessful;

/**
 * See https://git-scm.com/docs/git-show
 * <p/>
 * Note that this class is only compatible with git > 1.5 because "git show" seems to work only since 1.5.x
 * (http://stackoverflow.com/questions/610208/how-to-retrieve-a-single-file-from-specific-revision-in-git).
 */
// because it's similar to HgLogFileContent
@SuppressWarnings("Duplicates")
class GitLogFileContent implements VcsCommand<LogFileContentResult> {
	private final String gitPath;
	private final String repoFolder;
	private final String filePath;
	private final String revision;
	private final Charset charset;
	private final CommandLine commandLine;

	GitLogFileContent(String gitPath, String repoFolder, String filePath, String revision, Charset charset) {
		this.gitPath = gitPath;
		this.repoFolder = repoFolder;
		this.filePath = filePath;
		this.revision = revision;
		this.charset = charset;
		this.commandLine = gitLogFileContent(gitPath, repoFolder, filePath, revision, charset);
	}

	static CommandLine gitLogFileContent(String pathToGit, String repoFolder, String filePath, String revision, Charset charset) {
		CommandLine commandLine = new CommandLine(pathToGit, "show", revision + ":" + filePath);
		return commandLine.workingDir(repoFolder).outputCharset(charset).charsetAutoDetect(true);
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

		GitLogFileContent that = (GitLogFileContent) o;

		if (charset != null ? !charset.equals(that.charset) : that.charset != null) return false;
		if (filePath != null ? !filePath.equals(that.filePath) : that.filePath != null) return false;
		if (repoFolder != null ? !repoFolder.equals(that.repoFolder) : that.repoFolder != null) return false;
		if (gitPath != null ? !gitPath.equals(that.gitPath) : that.gitPath != null) return false;
		if (revision != null ? !revision.equals(that.revision) : that.revision != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = gitPath != null ? gitPath.hashCode() : 0;
		result = 31 * result + (repoFolder != null ? repoFolder.hashCode() : 0);
		result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
		result = 31 * result + (revision != null ? revision.hashCode() : 0);
		result = 31 * result + (charset != null ? charset.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitLogFileContent{" +
				"gitPath='" + gitPath + '\'' +
				", repoFolder='" + repoFolder + '\'' +
				", filePath='" + filePath + '\'' +
				", revision='" + revision + '\'' +
				", charset=" + charset +
				'}';
	}
}
