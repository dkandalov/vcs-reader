package org.vcsreader.vcs;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Exception which means VCS command executed but reported an error.
 * This class wraps failure message from VCS command.
 */
public class VcsError extends RuntimeException {
	private final List<String> vcsErrors;

	public VcsError(String vcsError) {
		this(asList(vcsError));
	}

	public VcsError(List<String> vcsErrors) {
		this.vcsErrors = vcsErrors;
	}

	@Override public String getMessage() {
		return String.join("\n", vcsErrors);
	}

	@Override public String toString() {
		return "Failure{vcsErrors=" + vcsErrors + '}';
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		VcsError failure = (VcsError) o;

		return vcsErrors != null ? vcsErrors.equals(failure.vcsErrors) : failure.vcsErrors == null;
	}

	@Override public int hashCode() {
		return vcsErrors != null ? vcsErrors.hashCode() : 0;
	}
}
