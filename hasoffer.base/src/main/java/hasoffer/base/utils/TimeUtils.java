package hasoffer.base.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimeUtils {
    public static final String PATTERN_YMD = "yyyyMMdd";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static long MILLISECONDS_OF_1_SECOND = 1000;
    public static long MILLISECONDS_OF_1_MINUTE = MILLISECONDS_OF_1_SECOND * 60;
    public static long MILLISECONDS_OF_1_HOUR = MILLISECONDS_OF_1_MINUTE * 60;
    public static long MILLISECONDS_OF_1_DAY = MILLISECONDS_OF_1_HOUR * 24;

    public static long SECONDS_OF_1_MINUTE = 60;
    public static long SECONDS_OF_1_HOUR = SECONDS_OF_1_MINUTE * 60;
    public static long SECONDS_OF_1_DAY = SECONDS_OF_1_HOUR * 24;


    public static Date getTime0() {
        return new Date(0);
    }

    public static Date getAppTime0() {
        return stringToDate("2016-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
    }

    public static String dateAsString() {
        Date now = new Date();
        DateFormat sdf = new SimpleDateFormat(PATTERN_YMD);

        return sdf.format(now);
    }

    public static String getCurrentTimeString() {
        Date now = new Date();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sdf.format(now);
    }

    /**
     * 返回月开始的毫秒值
     * 参数：月份偏移量，当前月传0；往前1个月传-1，往后1个月传1
     *
     * @param offsetMonth
     * @return
     */
    public static long getMonthStart(int offsetMonth) {
        Calendar c = Calendar.getInstance();
        c.setTime(nowDate());
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + offsetMonth);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
//		System.out.println(c.getTime());
        return c.getTime().getTime();
    }

    public static String now(String formate) {
        return parse(TimeUtils.nowDate(), formate, Locale.US);
    }

    public static String now(String formate, Locale locale) {
        return parse(TimeUtils.nowDate(), formate, locale);
    }

    public static String parse(Date date, String formate, Locale locale) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formate, locale);
        return simpleDateFormat.format(date);
    }

    public static String parse(Date date, String formate) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formate, Locale.US);
        return simpleDateFormat.format(date);
    }

    public static String parse(long timeMillis, String formate, Locale locale) {
        if (timeMillis == 0) {
            return "";
        }
        return parse(new Date(timeMillis), formate, locale);
    }

    public static String parse(long timeMillis, String formate) {
        if (timeMillis == 0) {
            return "";
        }
        return parse(new Date(timeMillis), formate, Locale.US);
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static Date nowDate() {
        return new Date();
    }

    public static int getHour() {
        Date now = TimeUtils.nowDate();
        return now.getHours();
    }

    public static String ISO8601(long millisecond) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date(millisecond));
    }

    public static Date getGMTDate(Date date) {
        try {
            return DateUtils.parseDate(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("GMT")), "yyyy-MM-dd HH:mm:ss.SSS");
        } catch (ParseException e) {
            return null;
        }
    }

    public static String ISO8601() {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

    public static Date parse(String strDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.parse(strDate);
    }

    public static long time(int year, int month, int day, int hour, int minute,
                            int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(year, month, day, hour, minute, second);

        return calendar.getTimeInMillis();
    }

    public static Date toDate(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return calendar.getTime();
    }

    public static long add(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.getTimeInMillis() + milliseconds;
    }

    public static long today(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        return calendar.getTimeInMillis();
    }

    public static long today() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long tommorrow(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.clear(Calendar.MILLISECOND);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        return calendar.getTimeInMillis();
    }

    public static long tommorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.clear(Calendar.MILLISECOND);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long yesterday(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.clear(Calendar.MILLISECOND);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        return calendar.getTimeInMillis();
    }

    public static long yesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.clear(Calendar.MILLISECOND);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static Date stringToDate(String dateTime, String pattern) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.parse(dateTime);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static int getMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));

        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getWeekOfMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }

    public static long getDayStart(long endTime) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(endTime));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime().getTime();
    }

    public static long getMonthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getDayStart(calendar.getTime().getTime());
    }

    public static long getNextMonthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        return getMonthStart(calendar.getTime());
    }

    public static int getYear(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));

        return calendar.get(Calendar.YEAR);
    }

    public static long getDayStart(String dateStr, String pattern) {
        long time = stringToDate(dateStr, pattern).getTime();
        return getDayStart(time);
    }

    public static long getNextDayStart(String dateStr, String pattern) {
        long time = stringToDate(dateStr, pattern).getTime() + TimeUtils.MILLISECONDS_OF_1_DAY;
        return getDayStart(time);
    }

    public static void fillDays(List<String> days, String startDay, String endDay, String pattern) {
        days.clear();

        if (startDay.equals(endDay)) {
            days.add(startDay);
            return;
        }

        if (startDay.compareTo(endDay) > 0) {
            return;
        }

        Date d1 = stringToDate(startDay, pattern);
        Date d2 = stringToDate(endDay, pattern);
        Date d0 = d1;


        while (d0.compareTo(d2) <= 0) {
            days.add(parse(d0, pattern));
            d0 = addDay(d0, 1);
        }
    }

    public static Date addDay(Date d0, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(d0);
        c.add(Calendar.DAY_OF_MONTH, day);
        return c.getTime();
    }

    public static Date after(long ms) {
        return new Date(System.currentTimeMillis() + ms);
    }

    public static Date after10m() {
        return after(1000 * 60 * 10);
    }

    public static Date beforeXm(int xMinutes) {
        return after(-1000 * 60 * xMinutes);
    }

    public static Date after30d() {
        return after(-1000 * 60 * 60 * 24 * 30);
    }


    public static Date before10m() {
        return after(-1000 * 60 * 10);
    }

    public static Date before30d() {
        return after(-1000 * 60 * 60 * 24 * 30);
    }

    public static long millis() {
        return System.currentTimeMillis();
    }

    public static long seconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static String timeAsString(Date date) {
        return dateFormat.format(date);
    }

    public static void main(String[] args) {
        System.out.println(addDay(stringToDate("20151231", "yyyyMMdd"), 2));
    }

    public static Date add(Date t, long l) {
        return new Date(t.getTime() + l);
    }
}
