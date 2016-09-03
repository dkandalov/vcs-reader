package org.vcsreader.lang;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.util.Arrays.asList;

public class DateTimeUtil {
	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	public static Instant date(String s) {
		DateTimeFormatter formatter = dateTimeFormatter("kk:mm:ss dd/MM/yyyy", ZoneOffset.UTC);
		return formatter.parse("00:00:00 " + s, DateTimeUtil::asInstant);
	}

	public static TimeRange timeRange(Date from, Date to) {
		return new TimeRange(from, to);
	}

	public static TimeRange timeRange(String from, String to) {
		return new TimeRange(date(from), date(to));
	}

	public static Instant dateTime(String s) {
		List<DateTimeFormatter> formatters = asList(
				dateTimeFormatter("kk:mm dd/MM/yyyy", ZoneOffset.UTC),
				dateTimeFormatter("kk:mm:ss dd/MM/yyyy", ZoneOffset.UTC),
				dateTimeFormatter("kk:mm:ss dd/MM/yyyy Z", ZoneOffset.UTC),
				dateTimeFormatter("kk:mm:ss.SSS dd/MM/yyyy", ZoneOffset.UTC),
				dateTimeFormatter("kk:mm:ss.SSSSSS dd/MM/yyyy", ZoneOffset.UTC),
				dateTimeFormatter("MMM dd kk:mm:ss yyyy Z", ZoneOffset.UTC),
				dateTimeFormatter("E MMM dd kk:mm:ss Z yyyy", ZoneOffset.UTC)
		);
		for (DateTimeFormatter formatter : formatters) {
			try {
				return formatter.parse(s, DateTimeUtil::asInstant);
			} catch (Exception ignored) {
			}
		}
		throw new RuntimeException("Failed to parse string as dateTime: " + s);
	}

	public static DateTimeFormatter dateTimeFormatter(String pattern, ZoneId zoneId) {
		return new DateTimeFormatterBuilder().appendPattern(pattern).parseLenient().toFormatter().withZone(zoneId);
	}

	/**
	 * Similar to {@link Instant#from(TemporalAccessor)} but also includes time zone offset.
	 */
	public static Instant asInstant(TemporalAccessor temporal) {
		if (temporal instanceof Instant) {
			return (Instant) temporal;
		}
		try {
			long instantSecs = temporal.getLong(INSTANT_SECONDS);
			if (temporal.isSupported(OFFSET_SECONDS)) {
				instantSecs = instantSecs - temporal.getLong(OFFSET_SECONDS);
			}
			int nanoOfSecond = temporal.get(NANO_OF_SECOND);
			return Instant.ofEpochSecond(instantSecs, nanoOfSecond);
		} catch (DateTimeException ex) {
			throw new DateTimeException("Unable to obtain Instant from TemporalAccessor: " +
					temporal + " of type " + temporal.getClass().getName(), ex);
		}
	}
}
