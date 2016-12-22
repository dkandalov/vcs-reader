package org.vcsreader;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.vcs.VcsCommand;

import java.util.List;

import static java.util.Arrays.asList;
import static org.vcsreader.lang.StringUtil.shortened;

public class LogFileContentResult {
	public static final VcsCommand.ResultAdapter<LogFileContentResult> adapter = new VcsCommand.ResultAdapter<LogFileContentResult>() {
		@Override public LogFileContentResult wrapAsResult(Exception e) {
			return new LogFileContentResult(e);
		}

		@Override public boolean isSuccessful(LogFileContentResult result) {
			return result.isSuccessful();
		}

		@Override public List<String> vcsErrorsIn(LogFileContentResult result) {
			return asList(result.stderr);
		}
	};
	private final String text;
	private final String stderr;
	private final int exitCode;
	private final Exception exception;

	public LogFileContentResult(Exception exception) {
		this("", "", 0, exception);
	}

	public LogFileContentResult(@NotNull String text) {
		this(text, "", 0, null);
	}

	public LogFileContentResult(@NotNull String stderr, int exitCode) {
		this("", stderr, exitCode, null);
	}

	private LogFileContentResult(@NotNull String text, @NotNull String stderr, int exitCode, Exception exception) {
		this.text = text;
		this.stderr = stderr;
		this.exitCode = exitCode;
		this.exception = exception;
	}

	@NotNull public String text() {
		return text;
	}

	public boolean isSuccessful() {
		return stderr.isEmpty() && exception == null && exitCode == 0;
	}

	@Override public String toString() {
		return "LogFileContentResult{" +
				"text='" + shortened(text, 100) + '\'' +
				", stderr='" + shortened(stderr, 100) + '\'' +
				", exitCode=" + exitCode + '\'' +
				", exception=" + exception.toString() +
				'}';
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LogFileContentResult that = (LogFileContentResult) o;

		return exitCode == that.exitCode &&
				text.equals(that.text) &&
				stderr.equals(that.stderr) &&
				(exception != null ? exception.equals(that.exception) : that.exception == null);
	}

	@Override public int hashCode() {
		int result = text.hashCode();
		result = 31 * result + stderr.hashCode();
		result = 31 * result + exitCode;
		result = 31 * result + (exception != null ? exception.hashCode() : 0);
		return result;
	}
}
