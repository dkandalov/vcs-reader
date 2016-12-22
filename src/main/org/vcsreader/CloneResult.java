package org.vcsreader;

import org.vcsreader.lang.Aggregatable;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsError;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class CloneResult implements Aggregatable<CloneResult> {
	public static final VcsCommand.ResultAdapter<CloneResult> adapter = new VcsCommand.ResultAdapter<CloneResult>() {
		@Override public CloneResult wrapAsResult(Exception e) {
			return new CloneResult(e);
		}

		@Override public boolean isSuccessful(CloneResult result) {
			return result.isSuccessful();
		}

		@Override public List<String> vcsErrorsIn(CloneResult result) {
			List<String> vcsErrors = new ArrayList<>();
			for (Exception e : result.exceptions) {
				if (e instanceof VcsError) {
					vcsErrors.add(e.getMessage());
				}
			}
			return vcsErrors;
		}
	};
	private final List<Exception> exceptions;


	public CloneResult() {
		this(new ArrayList<>());
	}

	public CloneResult(Exception e) {
		this(asList(e));
	}

	public CloneResult(String vcsError) {
		this(asList(new VcsError(vcsError)));
	}

	private CloneResult(List<Exception> exceptions) {
		this.exceptions = exceptions;
	}

	@Override public CloneResult aggregateWith(CloneResult value) {
		List<Exception> newExceptions = new ArrayList<>(exceptions);
		newExceptions.addAll(value.exceptions);
		return new CloneResult(newExceptions);
	}

	public List<Exception> exceptions() {
		return exceptions;
	}

	public boolean isSuccessful() {
		return exceptions.isEmpty();
	}

	@Override public String toString() {
		return "CloneResult{exceptions=" + exceptions.size() + '}';
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CloneResult that = (CloneResult) o;

		return exceptions != null ? exceptions.equals(that.exceptions) : that.exceptions == null;
	}

	@Override public int hashCode() {
		return exceptions != null ? exceptions.hashCode() : 0;
	}
}
