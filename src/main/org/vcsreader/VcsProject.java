package org.vcsreader;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsCommand.ResultAdapter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.vcsreader.lang.StringUtil.shortened;

/**
 * Represents a project as a set of {@link VcsRoot}s.
 * This class is the main entry point for reading version control history.
 */
public class VcsProject {
	private final List<VcsRoot> vcsRoots;
	private final CompositeListener compositeListener;

	public VcsProject(VcsRoot... vcsRoots) {
		this(asList(vcsRoots));
	}

	public VcsProject(List<VcsRoot> vcsRoots) {
		this.compositeListener = new CompositeListener();
		this.vcsRoots = unmodifiableList(vcsRoots.stream().map((vcsRoot) -> {
			if (vcsRoot instanceof VcsCommand.Owner) {
				return (VcsRoot)((VcsCommand.Owner) vcsRoot).withListener(compositeListener);
			} else {
				return vcsRoot;
			}
		}).collect(toList()));
	}

	/**
	 * For distributed VCS clones {@link VcsRoot}s to local file system and must be called before querying commits.
	 * Does nothing for centralized VCS because commit history can be queried from server.
	 */
	public CloneResult cloneToLocal() {
		Aggregator<CloneResult> aggregator = new Aggregator<>(new CloneResult());
		for (VcsRoot vcsRoot : vcsRoots) {
			CloneResult cloneResult = vcsRoot.cloneToLocal();
			aggregator.aggregate(cloneResult);
		}
		return aggregator.result;
	}

	/**
	 * For distributed VCS pulls updates from upstream for all {@link VcsRoot}s
	 * Does nothing for centralized VCS because commit history can be queried from server.
	 */
	public UpdateResult update() {
		Aggregator<UpdateResult> aggregator = new Aggregator<>(new UpdateResult());
		for (VcsRoot vcsRoot : vcsRoots) {
			UpdateResult updateResult = vcsRoot.update();
			aggregator.aggregate(updateResult);
		}
		return aggregator.result;
	}

	/**
	 * Request commits from VCS for all {@link VcsRoot}s within specified time range.
	 * For distributed VCS commits are read from currently checked out branch.
	 * <p>
	 * Merge commits are not logged, the commit which was merged into branch is logged instead.
	 * Therefore, it's possible to see commit with time outside of requested time range.
	 *
	 * @param timeRange range of timestamps used to request commits;
	 *                  start is inclusive with one second resolution, end is exclusive with one second resolution
	 */
	public LogResult log(TimeRange timeRange) {
		Aggregator<LogResult> aggregator = new Aggregator<>(new LogResult());
		for (VcsRoot vcsRoot : vcsRoots) {
			LogResult logResult = vcsRoot.log(timeRange);
			logResult = (logResult != null ? logResult.setVcsRoot(vcsRoot) : null);
			aggregator.aggregate(logResult);
		}
		return aggregator.result;
	}

	public VcsProject addListener(VcsCommand.Listener listener) {
		compositeListener.add(listener);
		return this;
	}

	public VcsProject removeListener(VcsCommand.Listener listener) {
		compositeListener.remove(listener);
		return this;
	}

	public List<VcsRoot> vcsRoots() {
		return vcsRoots;
	}

	@Override public String toString() {
		return "VcsProject{" + vcsRoots + '}';
	}


	private interface Aggregatable<T extends Aggregatable<T>> {
		T aggregateWith(T result);
	}


	private static class Aggregator<T extends Aggregatable<T>> {
		private T result;

		public Aggregator(T value) {
			result = value;
		}

		public void aggregate(T value) {
			if (result == null) {
				result = value;
			} else {
				result = result.aggregateWith(value);
			}
		}
	}


	public static class LogFileContentResult {
		public static final ResultAdapter<LogFileContentResult> adapter = new ResultAdapter<LogFileContentResult>() {
			@Override public LogFileContentResult wrapException(Exception e) {
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


	public static class LogResult implements Aggregatable<LogResult> {
		public static final ResultAdapter<LogResult> adapter = new ResultAdapter<LogResult>() {
			@Override public LogResult wrapException(Exception e) {
				return new LogResult(e);
			}

			@Override public boolean isSuccessful(LogResult result) {
				return result.isSuccessful();
			}

			@Override public List<String> vcsErrorsIn(LogResult result) {
				return result.vcsErrors();
			}
		};
		private final List<VcsCommit> commits;
		private final List<String> vcsErrors;
		private final List<Exception> exceptions;

		public LogResult() {
			this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		}

		public LogResult(Exception e) {
			this(new ArrayList<>(), new ArrayList<>(), asList(e));
		}

		public LogResult(List<VcsCommit> commits, List<String> vcsErrors) {
			this(commits, vcsErrors, new ArrayList<>());
		}

		public LogResult(List<VcsCommit> commits, List<String> vcsErrors, List<Exception> exceptions) {
			this.commits = commits;
			this.vcsErrors = vcsErrors;
			this.exceptions = exceptions;
		}

		public LogResult aggregateWith(LogResult result) {
			List<VcsCommit> newCommits = new ArrayList<>(commits);
			List<String> newErrors = new ArrayList<>(vcsErrors);
			List<Exception> newExceptions = new ArrayList<>(exceptions);
			newCommits.addAll(result.commits);
			newErrors.addAll(result.vcsErrors);
			newExceptions.addAll(result.exceptions);
			sort(newCommits, (commit1, commit2) ->
					new Long(commit1.getTime().getTime()).compareTo(commit2.getTime().getTime())
			);

			return new LogResult(newCommits, newErrors, newExceptions);
		}

		public boolean isSuccessful() {
			return vcsErrors.isEmpty() && exceptions.isEmpty();
		}

		public List<String> vcsErrors() {
			return vcsErrors;
		}

		public List<Exception> exceptions() {
			return exceptions;
		}

		public List<VcsCommit> commits() {
			return commits;
		}

		public LogResult setVcsRoot(VcsRoot vcsRoot) {
			for (VcsCommit commit : commits) {
				if (commit instanceof VcsCommit.WithRootReference) {
					((VcsCommit.WithRootReference) commit).setVcsRoot(vcsRoot);
				}
			}
			return this;
		}

		@Override public String toString() {
			return "LogResult{" +
					"commits=" + commits.size() +
					", vcsErrors=" + vcsErrors.size() +
					", exceptions=" + exceptions.size() +
					'}';
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			LogResult logResult = (LogResult) o;

			return commits.equals(logResult.commits)
				&& vcsErrors.equals(logResult.vcsErrors)
				&& exceptions.equals(logResult.exceptions);
		}

		@Override public int hashCode() {
			int result = commits.hashCode();
			result = 31 * result + vcsErrors.hashCode();
			result = 31 * result + exceptions.hashCode();
			return result;
		}
	}


	public static class UpdateResult implements Aggregatable<UpdateResult> {
		public static final ResultAdapter<UpdateResult> adapter = new ResultAdapter<UpdateResult>() {
			@Override public UpdateResult wrapException(Exception e) {
				return new UpdateResult(e);
			}

			@Override public boolean isSuccessful(UpdateResult result) {
				return result.isSuccessful();
			}

			@Override public List<String> vcsErrorsIn(UpdateResult result) {
				return result.vcsErrors();
			}
		};
		private final List<String> vcsErrors;
		private final List<Exception> exceptions;

		public UpdateResult() {
			this(new ArrayList<>(), new ArrayList<>());
		}

		public UpdateResult(Exception e) {
			this(new ArrayList<>(), asList(e));
		}

		public UpdateResult(List<String> vcsErrors, List<Exception> exceptions) {
			this.vcsErrors = vcsErrors;
			this.exceptions = exceptions;
		}

		public UpdateResult(String error) {
			this(asList(error), new ArrayList<>());
		}

		@Override public UpdateResult aggregateWith(UpdateResult result) {
			List<String> newErrors = new ArrayList<>(vcsErrors);
			List<Exception> newExceptions = new ArrayList<>(exceptions);
			newErrors.addAll(result.vcsErrors);
			newExceptions.addAll(result.exceptions);
			return new UpdateResult(newErrors, newExceptions);
		}

		public List<String> vcsErrors() {
			return vcsErrors;
		}

		public List<Exception> exceptions() {
			return exceptions;
		}

		public boolean isSuccessful() {
			return vcsErrors.isEmpty() && exceptions.isEmpty();
		}

		@Override public String toString() {
			return "UpdateResult{" +
					"vcsErrors=" + vcsErrors.size() +
					", exceptions=" + exceptions.size() +
					'}';
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			UpdateResult that = (UpdateResult) o;

			return vcsErrors.equals(that.vcsErrors) && exceptions.equals(that.exceptions);
		}

		@Override public int hashCode() {
			int result = vcsErrors.hashCode();
			result = 31 * result + exceptions.hashCode();
			return result;
		}
	}


	public static class CloneResult implements Aggregatable<CloneResult> {
		public static final ResultAdapter<CloneResult> adapter = new ResultAdapter<CloneResult>() {
			@Override public CloneResult wrapException(Exception e) {
				return new CloneResult(e);
			}

			@Override public boolean isSuccessful(CloneResult result) {
				return result.isSuccessful();
			}

			@Override public List<String> vcsErrorsIn(CloneResult result) {
				return result.vcsErrors();
			}
		};
		private final List<String> vcsErrors;
		private final List<Exception> exceptions;

		public CloneResult() {
			this(new ArrayList<>(), new ArrayList<>());
		}

		public CloneResult(Exception e) {
			this(new ArrayList<>(), asList(e));
		}

		public CloneResult(List<String> vcsErrors, List<Exception> exceptions) {
			this.vcsErrors = vcsErrors;
			this.exceptions = exceptions;
		}

		public CloneResult(List<String> vcsErrors) {
			this(vcsErrors, new ArrayList<>());
		}

		@Override public CloneResult aggregateWith(CloneResult result) {
			List<String> newErrors = new ArrayList<>(vcsErrors);
			List<Exception> newExceptions = new ArrayList<>(exceptions);
			newErrors.addAll(result.vcsErrors);
			newExceptions.addAll(result.exceptions);
			return new CloneResult(newErrors, newExceptions);
		}

		public List<String> vcsErrors() {
			return vcsErrors;
		}

		public List<Exception> exceptions() {
			return exceptions;
		}

		public boolean isSuccessful() {
			return vcsErrors.isEmpty() && exceptions.isEmpty();
		}

		@Override public String toString() {
			return "CloneResult{" +
					"vcsErrors=" + vcsErrors.size() +
					", exceptions=" + exceptions.size() +
					'}';
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			CloneResult that = (CloneResult) o;

			return vcsErrors.equals(that.vcsErrors) && exceptions.equals(that.exceptions);
		}

		@Override public int hashCode() {
			int result = vcsErrors.hashCode();
			result = 31 * result + exceptions.hashCode();
			return result;
		}
	}


	private class CompositeListener implements VcsCommand.Listener {
		private final List<VcsCommand.Listener> listeners = new ArrayList<>();

		public void add(VcsCommand.Listener listener) {
			listeners.add(listener);
		}

		public void remove(VcsCommand.Listener listener) {
			listeners.remove(listener);
		}

		@Override public void beforeCommand(VcsCommand<?> command) {
			for (VcsCommand.Listener listener : listeners) {
				listener.beforeCommand(command);
			}
		}

		@Override public void afterCommand(VcsCommand<?> command) {
			for (VcsCommand.Listener listener : listeners) {
				listener.afterCommand(command);
			}
		}
	}
}
