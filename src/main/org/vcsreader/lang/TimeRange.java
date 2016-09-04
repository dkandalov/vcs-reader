package org.vcsreader.lang;

import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class TimeRange {
	public static final TimeRange all = new TimeRange(Instant.MIN, Instant.MAX);

	private final Instant from;
	private final Instant to;


	public static TimeRange beforeNow() {
		return before(Clock.systemUTC().instant());
	}

	public static TimeRange before(@NotNull Instant instant) {
		return new TimeRange(Instant.MIN, instant);
	}

	public static TimeRange after(@NotNull Instant instant) {
		return new TimeRange(instant, Instant.MAX);
	}

	public static TimeRange between(@NotNull Instant from, @NotNull Instant to) {
		return new TimeRange(from, to);
	}

	public TimeRange(@NotNull Date fromDate, @NotNull Date toDate) {
		this(fromDate.toInstant(), toDate.toInstant());
	}

	public TimeRange(@NotNull Calendar fromDate, @NotNull Calendar toDate) {
		this(fromDate.toInstant(), toDate.toInstant());
	}

	public TimeRange(@NotNull Instant from, @NotNull Instant to) {
		if (!from.isBefore(to)) {
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
