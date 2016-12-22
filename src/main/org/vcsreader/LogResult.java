package org.vcsreader;

import org.vcsreader.lang.Aggregatable;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;

public class LogResult implements Aggregatable<LogResult> {
	public static final VcsCommand.ResultAdapter<LogResult> adapter = new VcsCommand.ResultAdapter<LogResult>() {
		@Override public LogResult wrapAsResult(Exception e) {
			return new LogResult(e);
		}

		@Override public boolean isSuccessful(LogResult result) {
			return result.isSuccessful();
		}

		@Override public List<String> vcsErrorsIn(LogResult result) {
			List<String> vcsErrors = new ArrayList<>();
			for (Exception e : result.exceptions) {
				if (e instanceof VcsError) {
					vcsErrors.add(e.getMessage());
				}
			}
			return vcsErrors;
		}
	};
	private final List<VcsCommit> commits;
	private final List<Exception> exceptions;


	public LogResult() {
		this(new ArrayList<>(), new ArrayList<>());
	}

	public LogResult(List<VcsCommit> commits) {
		this(commits, new ArrayList<>());
	}

	public LogResult(Exception e) {
		this(new ArrayList<>(), asList(e));
	}

	public LogResult(List<VcsCommit> commits, List<Exception> exceptions) {
		this.commits = commits;
		this.exceptions = exceptions;
	}

	@Override public LogResult aggregateWith(LogResult value) {
		List<VcsCommit> newCommits = new ArrayList<>(commits);
		List<Exception> newExceptions = new ArrayList<>(exceptions);

		newCommits.addAll(value.commits);
		newCommits.sort(Comparator.comparing(VcsCommit::getDateTime));
		newExceptions.addAll(value.exceptions);

		return new LogResult(newCommits, newExceptions);
	}

	public boolean isSuccessful() {
		return exceptions.isEmpty();
	}

	public List<Exception> exceptions() {
		return exceptions;
	}

	public List<VcsCommit> commits() {
		return commits;
	}

	LogResult setVcsRoot(VcsRoot vcsRoot) {
		for (VcsCommit commit : commits) {
			if (commit instanceof VcsCommit.WithRootReference) {
				((VcsCommit.WithRootReference) commit).setVcsRoot(vcsRoot);
			}
		}
		return this;
	}

	@Override public String toString() {
		return "LogResult{commits=" + commits.size() + ", exceptions=" + exceptions.size() + '}';
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LogResult logResult = (LogResult) o;

		if (commits != null ? !commits.equals(logResult.commits) : logResult.commits != null) return false;
		return exceptions != null ? exceptions.equals(logResult.exceptions) : logResult.exceptions == null;
	}

	@Override public int hashCode() {
		int result = commits != null ? commits.hashCode() : 0;
		result = 31 * result + (exceptions != null ? exceptions.hashCode() : 0);
		return result;
	}
}
