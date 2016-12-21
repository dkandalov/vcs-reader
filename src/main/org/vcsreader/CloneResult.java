package org.vcsreader;

import org.vcsreader.lang.Aggregatable;
import org.vcsreader.vcs.VcsCommand;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class CloneResult implements Aggregatable<CloneResult> {
	public static final VcsCommand.ResultAdapter<CloneResult> adapter = new VcsCommand.ResultAdapter<CloneResult>() {
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

	@Override public CloneResult aggregateWith(CloneResult value) {
		List<String> newErrors = new ArrayList<>(vcsErrors);
		List<Exception> newExceptions = new ArrayList<>(exceptions);
		newErrors.addAll(value.vcsErrors);
		newExceptions.addAll(value.exceptions);
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
