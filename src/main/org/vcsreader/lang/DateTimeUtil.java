package org.vcsreader.lang;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.util.Arrays.asList;

public class DateTimeUtil {
	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	public static Date asDate(Calendar calendar) {
		return new Date(calendar.getTimeInMillis());
	}

	public static Date asDate(OffsetDateTime offsetDateTime) {
		return new Date(offsetDateTime.toEpochSecond());
	}

	public static Date date(String s) {
		try {
			return dateTimeFormat("dd/MM/yyyy", UTC).parse(s);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static Date dateTime(String s) {
		List<DateFormat> formats = asList(
				dateTimeFormat("kk:mm dd/MM/yyyy", UTC),
				dateTimeFormat("kk:mm:ss dd/MM/yyyy", UTC),
				dateTimeFormat("kk:mm:ss.SSS dd/MM/yyyy", UTC),
				dateTimeFormat("E MMM dd kk:mm:ss Z yyyy", UTC)
		);
		for (DateFormat format : formats) {
			try {
				return format.parse(s);
			} catch (ParseException ignored) {
			}
		}
		throw new RuntimeException("Failed to parse string as dateTime: " + s);
	}

	public static DateFormat dateTimeFormat(String pattern, TimeZone timeZone) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(timeZone);
		return format;
	}
}