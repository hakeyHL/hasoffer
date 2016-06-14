package hasoffer.timer.test;

import hasoffer.base.utils.TimeUtils;

import java.text.ParseException;
import java.util.Date;

public class MainTest {
    public static void main(String[] args) throws ParseException {
//        Date date = DateUtils.parseDate("2016-05-26 00:10","yyyy-MM-dd HH:mm");
//        System.out.println(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm", TimeZone.getTimeZone("GMT")));
        System.out.println(TimeUtils.getGMTDate(new Date()));
        System.out.println(new Date());
        System.out.println(new Date(0));
    }
}
