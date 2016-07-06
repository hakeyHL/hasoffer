package hasoffer.core.test;

import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import org.junit.Test;

import java.util.Date;

/**
 * Created on 2016/4/14.
 */
public class TimeUtilTest {

    @Test
    public void f1() {

        int[] array = {0, 899, 999, 949};

        int sum = 0;
        int avg = 0;
        int res = 0;

        for (Integer i : array) {
            sum += i;
        }

        avg = sum / array.length;

        for (Integer i : array) {

            res += (i - avg) * (i - avg);

        }

        res = res / array.length;

        double result = Math.sqrt(res);

        System.out.println(result);
    }

    @Test
    public void testTimeUtil() {
        final Date DEFAUTL_UPDATETIME = TimeUtils.stringToDate("2016-07-01 15:00:00", "yyyy-MM-dd HH:mm:ss");
        System.out.println(TimeUtils.parse(DEFAUTL_UPDATETIME, "yyyy-MM-dd HH:mm:ss"));
        String todayString = TimeUtils.parse(TimeUtils.today(), "yyyyMMdd");
        System.out.println(todayString);
        System.out.println(DEFAUTL_UPDATETIME.getTime());
    }

    @Test
    public void f() {
        String url = "http://www.snapdeal.com/offers/appliances-summer-sale?MID=42439%7Cweb%7Cplatinum%7C1%7C%7CMobiles%7CAppliances%7C%7CSummerSale&utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_id=12823&aff_sub=p%3A1bsAFIeiEDOz";
        System.out.println(StringUtils.urlDecode(url));
    }

    @Test
    public void f2() {
        System.out.println(new Date(1461816231294L));
    }

    @Test
    public void getTimeMillis() {
        Date date = TimeUtils.stringToDate("2016-06-06 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Date date1 = TimeUtils.stringToDate("2016-06-07 00:00:00", "yyyy-MM-dd hh:mm:ss");
        System.out.println(date.toString());
        System.out.println(date1.toString());
        System.out.println(date.getTime());
        System.out.println(date1.getTime());
    }

    @Test
    public void getDateByLong() {
        long time = 1467710187747L;
        Date date = TimeUtils.toDate(time);
        System.out.println(date);
    }

    @Test
    public void testUtil() {
        long hour = TimeUtils.MILLISECONDS_OF_1_HOUR;
        System.out.println(hour);
    }

    @Test
    public void testTimeUtilToday() {
        System.out.println(TimeUtils.parse(TimeUtils.toDate(TimeUtils.today()),"yyyy-MM-dd HH:mm:ss"));
        System.out.println(TimeUtils.parse(TimeUtils.addDay(TimeUtils.toDate(TimeUtils.today()), -3),"yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testDayStart(){

        long dayStart = TimeUtils.today();

        System.out.println(dayStart);

    }


    @Test
    public void getGMTTime() {
        System.out.println(TimeUtils.getGMTDate(new Date()));
    }
}
