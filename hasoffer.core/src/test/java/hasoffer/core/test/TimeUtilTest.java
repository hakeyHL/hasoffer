package hasoffer.core.test;

import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import org.junit.Test;

import java.util.Date;

/**
 * Created on 2016/4/14.
 */
public class TimeUtilTest {

    private void print(String str) {
        System.out.println(str);
    }

    @Test
    public void ttt() throws Exception {
        String str = "\n5.7407384 = sum of:\n  3.9300325 = max of:\n    1.012725 = weight(title:lenovo^0.5 in 5522) [DefaultSimilarity], result of:\n      1.012725 = score(doc=5522,freq=1.0), product of:\n        0.20037147 = queryWeight, product of:\n          0.5 = boost\n          5.0542374 = idf(docFreq=7443, maxDocs=429081)\n          0.07928851 = queryNorm\n        5.0542374 = fieldWeight in 5522, product of:\n          1.0 = tf(freq=1.0), with freq of:\n            1.0 = termFreq=1.0\n          5.0542374 = idf(docFreq=7443, maxDocs=429081)\n          1.0 = fieldNorm(doc=5522)\n    3.9300325 = weight(brand:lenovo^0.8 in 5522) [DefaultSimilarity], result of:\n      3.9300325 = score(doc=5522,freq=1.0), product of:\n        0.49928463 = queryWeight, product of:\n          0.8 = boost\n          7.871327 = idf(docFreq=444, maxDocs=429081)\n          0.07928851 = queryNorm\n        7.871327 = fieldWeight in 5522, product of:\n          1.0 = tf(freq=1.0), with freq of:\n            1.0 = termFreq=1.0\n          7.871327 = idf(docFreq=444, maxDocs=429081)\n          1.0 = fieldNorm(doc=5522)\n  1.810706 = FunctionQuery(sum(10.0*float(log(10.0*float(long(searchCount))+1.0))+1.0,div(int(rating),const(1000)))), product of:\n    22.836927 = sum(10.0*float(log(10.0*float(long(searchCount)=1)+1.0))+1.0,div(int(rating)=11423,const(1000)))\n    1.0 = boost\n    0.07928851 = queryNorm\n";
        print(str);
    }

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
        String startDateString = TimeUtils.parse(TimeUtils.today() - TimeUtils.MILLISECONDS_OF_1_DAY * 2, "yyyyMMdd");
        System.out.println(startDateString);
    }

    @Test
    public void getTimeMillis() {
        Date date = TimeUtils.stringToDate("2016-08-04 00:00:00", "yyyy-MM-dd HH:mm:ss");
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
        String startDateString = TimeUtils.parse(TimeUtils.today() - TimeUtils.MILLISECONDS_OF_1_DAY, "yyyyMMdd");
        System.out.println(startDateString);
    }

    @Test
    public void testTimeUtilToday() {
        System.out.println(TimeUtils.parse(TimeUtils.toDate(TimeUtils.today()), "yyyy-MM-dd HH:mm:ss"));
        System.out.println(TimeUtils.parse(TimeUtils.addDay(TimeUtils.toDate(TimeUtils.today()), -3), "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testDayStart() {

        long dayStart = TimeUtils.today();

        System.out.println(dayStart);

    }

    @Test
    public void test1() {
        System.out.println(TimeUtils.nowDate());
    }

    @Test
    public void getGMTTime() {
        System.out.println(TimeUtils.getGMTDate(new Date()));
    }

    @Test
    public void test2() {
        // > 0
        System.out.println(TimeUtils.nowDate().compareTo(TimeUtils.toDate(TimeUtils.today())));
    }
}
