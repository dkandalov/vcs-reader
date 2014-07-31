package vcsreader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtil {
    private static final TimeZone utc = TimeZone.getTimeZone("UTC");

    public static Date date(String s) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        format.setTimeZone(utc);
        try {
            return format.parse(s);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
