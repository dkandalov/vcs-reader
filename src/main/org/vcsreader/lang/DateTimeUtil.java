package org.vcsreader.lang;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.util.Arrays.asList;

public class DateTimeUtil {
	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	public static Instant date(String s) {
		DateTimeFormatter formatter = dateTimeFormatter("kk:mm:ss dd/MM/yyyy", ZoneOffset.UTC);
		return formatter.parse("00:00:00 " + s, Instant::from);
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
				dateTimeFormatter("kk:mm:ss.SSS dd/MM/yyyy", ZoneOffset.UTC),
				dateTimeFormatter("kk:mm:ss.SSSSSS dd/MM/yyyy", ZoneOffset.UTC),
				dateTimeFormatter("MMM dd kk:mm:ss yyyy Z", ZoneOffset.UTC),
				dateTimeFormatter("E MMM dd kk:mm:ss Z yyyy", ZoneOffset.UTC)
		);
		for (DateTimeFormatter formatter : formatters) {
			try {
				return formatter.parse(s, Instant::from);
			} catch (Exception ignored) {
			}
		}
		throw new RuntimeException("Failed to parse string as dateTime: " + s);
	}

	public static DateTimeFormatter dateTimeFormatter(String pattern, ZoneId zoneId) {
		return new DateTimeFormatterBuilder().appendPattern(pattern).parseLenient().toFormatter().withZone(zoneId);
	}
}
