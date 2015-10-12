package vcsreader.lang;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.util.Arrays.asList;

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

    public static Date dateTime(String s) {
        List<SimpleDateFormat> formats = asList(
                new SimpleDateFormat("kk:mm dd/MM/yyyy"),
                new SimpleDateFormat("kk:mm:ss dd/MM/yyyy"),
                new SimpleDateFormat("kk:mm:ss.SSS dd/MM/yyyy"),
                new SimpleDateFormat("E MMM dd kk:mm:ss Z yyyy")
        );
        for (SimpleDateFormat format : formats) {
            try {
                format.setTimeZone(utc);
                return format.parse(s);
            } catch (ParseException ignored) {
            }
        }
        throw new RuntimeException("Failed to parse string as dateTime: " + s);
    }
}
