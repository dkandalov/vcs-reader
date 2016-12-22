package org.vcsreader;

import org.vcsreader.lang.Aggregatable;
import org.vcsreader.vcs.VcsCommand;
import org.vcsreader.vcs.VcsError;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class UpdateResult implements Aggregatable<UpdateResult> {
	public static final VcsCommand.ResultAdapter<UpdateResult> adapter = new VcsCommand.ResultAdapter<UpdateResult>() {
		@Override public UpdateResult wrapAsResult(Exception e) {
			return new UpdateResult(e);
		}

		@Override public boolean isSuccessful(UpdateResult result) {
			return result.isSuccessful();
		}

		@Override public List<String> vcsErrorsIn(UpdateResult result) {
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


	public UpdateResult() {
		this(new ArrayList<>());
	}

	public UpdateResult(Exception e) {
		this(asList(e));
	}

	public UpdateResult(List<Exception> exceptions) {
		this.exceptions = exceptions;
	}

	@Override public UpdateResult aggregateWith(UpdateResult value) {
		List<Exception> newExceptions = new ArrayList<>(exceptions);
		newExceptions.addAll(value.exceptions);
		return new UpdateResult(newExceptions);
	}

	public List<Exception> exceptions() {
		return exceptions;
	}

	public boolean isSuccessful() {
		return exceptions.isEmpty();
	}

	@Override public String toString() {
		return "UpdateResult{exceptions=" + exceptions.size() + '}';
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UpdateResult that = (UpdateResult) o;

		return exceptions != null ? exceptions.equals(that.exceptions) : that.exceptions == null;
	}

	@Override public int hashCode() {
		return exceptions != null ? exceptions.hashCode() : 0;
	}
}
