package com.compastbc.core.utils;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by hemant.
 * Date: 30/8/18
 * Time: 2:34 PM
 */

public final class CalenderUtils {

    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIMESTAMP_FORMAT = "dd/MM/yyyy";
    public static final String DB_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String EXCEPTION_FORMAT = "dd_MM_yyyy HH:mm:ss";
    public static final String SERVER_DATE_FORMAT = "E MMM dd HH:mm:ss Z yyyy";

    private CalenderUtils() {
        // This class is not publicly instantiable
    }

    public static String getTimestamp(String format) {
        return new SimpleDateFormat(format, Locale.US).format(new Date());
    }

    public static String getTimestamp(String format, Locale locale) {
        return new SimpleDateFormat(format, locale).format(new Date());
    }

    public static String getTimestamp() {
        return String.valueOf(new Date().getTime());
    }

    public static long getTimestampInDate(String format) {
        String strDate = new SimpleDateFormat(format, Locale.US).format(new Date());

        SimpleDateFormat parseFormat = new SimpleDateFormat(format, Locale.US);
        parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = null;
        try {
            date = parseFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Objects.requireNonNull(date).getTime();
    }

    public static Date getDateFromTimestamp(Long format) {
        return new Date(format);
    }

    public static String formatTimestamp(Long timestamp, String dFormat) {
        SimpleDateFormat displayFormat = new SimpleDateFormat(dFormat, Locale.US);
        Date tmpDate = new Date(timestamp);
        return displayFormat.format(tmpDate);
    }

    public static String format12HourTime(String time, @NonNull String pFormat, @NonNull String dFormat) {
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat(pFormat, Locale.US);
            SimpleDateFormat displayFormat = new SimpleDateFormat(dFormat, Locale.US);
            Date dTime = parseFormat.parse(time);
            assert dTime != null;
            return displayFormat.format(dTime);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatDate(String date, @NonNull String pFormat, @NonNull String dFormat) {
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat(pFormat, Locale.US);
            SimpleDateFormat displayFormat = new SimpleDateFormat(dFormat, Locale.US);
            Date dTime = parseFormat.parse(date);
            assert dTime != null;
            return displayFormat.format(dTime);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatDate(String date, @NonNull String pFormat, @NonNull String dFormat, Locale locale) {
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat(pFormat, locale);
            SimpleDateFormat displayFormat = new SimpleDateFormat(dFormat, locale);
            Date dTime = parseFormat.parse(date);
            assert dTime != null;
            return displayFormat.format(dTime);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String formatDateUTC(String date, @NonNull String pFormat, @NonNull String dFormat, Locale locale) {
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat(pFormat, locale);
            parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat displayFormat = new SimpleDateFormat(dFormat, locale);
            displayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date dTime = parseFormat.parse(date);
            assert dTime != null;
            return displayFormat.format(dTime);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatByLocale(String date, @NonNull String pFormat, Locale locale) {
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat(pFormat, locale);
            parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date dTime = parseFormat.parse(date);
            return parseFormat.format(Objects.requireNonNull(dTime));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatDate(Date date, @NonNull String dFormat) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat(dFormat, Locale.US);
            return displayFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Date getDateFormat(String date, @NonNull String format) {
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat(format, Locale.US);

            return parseFormat.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    public static String getDateTime(String format, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        return sdf.format(new Date());
    }

    public static Long getTimerDifference(String timer, String myTime) {
        SimpleDateFormat formatter = new SimpleDateFormat(CalenderUtils.DB_TIMESTAMP_FORMAT, Locale.US);

        try {
            Date startDate = formatter.parse(timer);

            Date endDate = formatter.parse(myTime);

            assert startDate != null;
            assert endDate != null;
            long diffInMilliSec = endDate.getTime() - startDate.getTime();

            return (diffInMilliSec / (1000 * 60)) % 60;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    public static String getCurrentDate() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        return (day + "/" + month + "/" + year);
    }

    @NonNull
    public static String getCurrentTime() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return (hour + ":" + minute);
    }
}
