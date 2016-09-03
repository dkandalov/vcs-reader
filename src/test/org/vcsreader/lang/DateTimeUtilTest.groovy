package org.vcsreader.lang

import org.junit.Test

import java.time.ZonedDateTime

import static java.time.ZoneOffset.UTC
import static org.vcsreader.lang.DateTimeUtil.dateTime

public class DateTimeUtilTest {
	@Test void convertsStringWithTimeZoneToInstant() {
		ZonedDateTime.ofInstant(dateTime("17:57:48 30/01/2014 -0500"), UTC).with {
			assert it.hour == 22
			assert it.minute == 57
			assert it.second == 48
		}
	}
}