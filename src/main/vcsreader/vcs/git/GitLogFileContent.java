package vcsreader.vcs.git;

import vcsreader.lang.ExternalCommand;
import vcsreader.vcs.commandlistener.VcsCommand;

import java.nio.charset.Charset;

import static vcsreader.VcsProject.LogFileContentResult;
import static vcsreader.lang.StringUtil.trimLastNewLine;
import static vcsreader.vcs.git.GitExternalCommand.isSuccessful;

/**
 * See https://git-scm.com/docs/git-show
 * <p/>
 * Note that this class is only compatible with git > 1.5 because "git show" seems to work only since 1.5.x
 * (http://stackoverflow.com/questions/610208/how-to-retrieve-a-single-file-from-specific-revision-in-git).
 */
@SuppressWarnings("Duplicates")
		// because it's similar to HgLogFileContent
class GitLogFileContent implements VcsCommand<LogFileContentResult> {
	private final String gitPath;
	private final String folder;
	private final String filePath;
	private final String revision;
	private final Charset charset;
	private final ExternalCommand externalCommand;

	GitLogFileContent(String gitPath, String folder, String filePath, String revision, Charset charset) {
		this.gitPath = gitPath;
		this.folder = folder;
		this.filePath = filePath;
		this.revision = revision;
		this.charset = charset;
		this.externalCommand = gitLogFileContent(gitPath, folder, filePath, revision, charset);
	}

	static ExternalCommand gitLogFileContent(String pathToGit, String folder, String filePath, String revision, Charset charset) {
		ExternalCommand command = new ExternalCommand(pathToGit, "show", revision + ":" + filePath);
		return command.workingDir(folder).outputCharset(charset).charsetAutoDetect(true);
	}

	@Override public LogFileContentResult execute() {
		externalCommand.execute();
		if (isSuccessful(externalCommand)) {
			return new LogFileContentResult(trimLastNewLine(externalCommand.stdout()));
		} else {
			return new LogFileContentResult(externalCommand.stderr() + externalCommand.exceptionStacktrace(), externalCommand.exitCode());
		}
	}

	@Override public String describe() {
		return externalCommand.describe();
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		GitLogFileContent that = (GitLogFileContent) o;

		if (charset != null ? !charset.equals(that.charset) : that.charset != null) return false;
		if (filePath != null ? !filePath.equals(that.filePath) : that.filePath != null) return false;
		if (folder != null ? !folder.equals(that.folder) : that.folder != null) return false;
		if (gitPath != null ? !gitPath.equals(that.gitPath) : that.gitPath != null) return false;
		if (revision != null ? !revision.equals(that.revision) : that.revision != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = gitPath != null ? gitPath.hashCode() : 0;
		result = 31 * result + (folder != null ? folder.hashCode() : 0);
		result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
		result = 31 * result + (revision != null ? revision.hashCode() : 0);
		result = 31 * result + (charset != null ? charset.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "GitLogFileContent{" +
				"gitPath='" + gitPath + '\'' +
				", folder='" + folder + '\'' +
				", filePath='" + filePath + '\'' +
				", revision='" + revision + '\'' +
				", charset=" + charset +
				'}';
	}
}
