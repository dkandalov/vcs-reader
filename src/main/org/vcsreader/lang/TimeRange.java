package org.vcsreader.lang;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Date;

public class TimeRange {
	private final Instant from;
	private final Instant to;

	public TimeRange(Date fromDate, Date toDate) {
		this(Instant.ofEpochMilli(fromDate.getTime()), Instant.ofEpochMilli(toDate.getTime()));
	}

	public TimeRange(@NotNull Instant from, @NotNull Instant to) {
		if (from.isAfter(to)) {
			throw new IllegalArgumentException("Invalid time range: " + from + " must be before or equal " + to);
		}
		this.from = from;
		this.to = to;
	}

	public Instant from() {
		return from;
	}

	public Instant to() {
		return to;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TimeRange timeRange = (TimeRange) o;

		return from != null ? from.equals(timeRange.from) : timeRange.from == null &&
				(to != null ? to.equals(timeRange.to) : timeRange.to == null);
	}

	@Override public int hashCode() {
		int result = from != null ? from.hashCode() : 0;
		result = 31 * result + (to != null ? to.hashCode() : 0);
		return result;
	}

	@Override public String toString() {
		return "TimeRange{from=" + from + ", to=" + to + '}';
	}
}
