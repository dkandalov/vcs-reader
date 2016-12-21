package org.vcsreader;

import org.vcsreader.lang.Aggregatable;
import org.vcsreader.vcs.VcsCommand;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class UpdateResult implements Aggregatable<UpdateResult> {
	public static final VcsCommand.ResultAdapter<UpdateResult> adapter = new VcsCommand.ResultAdapter<UpdateResult>() {
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

	@Override public UpdateResult aggregateWith(UpdateResult value) {
		List<String> newErrors = new ArrayList<>(vcsErrors);
		List<Exception> newExceptions = new ArrayList<>(exceptions);
		newErrors.addAll(value.vcsErrors);
		newExceptions.addAll(value.exceptions);
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
