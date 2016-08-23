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
        String str = "\n3.1737967 = sum of:\n  3.1403005 = sum of:\n    0.7952872 = max of:\n      0.7952872 = weight(title:lenovo^50.0 in 40304) [DefaultSimilarity], result of:\n        0.7952872 = score(doc=40304,freq=1.0), product of:\n          0.15440796 = queryWeight, product of:\n            50.0 = boost\n            5.150558 = idf(docFreq=2843, maxDocs=180507)\n            5.995776E-4 = queryNorm\n          5.150558 = fieldWeight in 40304, product of:\n            1.0 = tf(freq=1.0), with freq of:\n              1.0 = termFreq=1.0\n            5.150558 = idf(docFreq=2843, maxDocs=180507)\n            1.0 = fieldNorm(doc=40304)\n    2.3450134 = max of:\n      0.45247084 = weight(model:appl^10.0 in 40304) [DefaultSimilarity], result of:\n        0.45247084 = score(doc=40304,freq=3.0), product of:\n          0.039576527 = queryWeight, product of:\n            10.0 = boost\n            6.6007347 = idf(docFreq=666, maxDocs=180507)\n            5.995776E-4 = queryNorm\n          11.432808 = fieldWeight in 40304, product of:\n            1.7320508 = tf(freq=3.0), with freq of:\n              3.0 = termFreq=3.0\n            6.6007347 = idf(docFreq=666, maxDocs=180507)\n            1.0 = fieldNorm(doc=40304)\n      1.314008 = weight(title:appl^50.0 in 40304) [DefaultSimilarity], result of:\n        1.314008 = score(doc=40304,freq=2.0), product of:\n          0.16689727 = queryWeight, product of:\n            50.0 = boost\n            5.567161 = idf(docFreq=1874, maxDocs=180507)\n            5.995776E-4 = queryNorm\n          7.8731546 = fieldWeight in 40304, product of:\n            1.4142135 = tf(freq=2.0), with freq of:\n              2.0 = termFreq=2.0\n            5.567161 = idf(docFreq=1874, maxDocs=180507)\n            1.0 = fieldNorm(doc=40304)\n      2.3450134 = weight(brand:appl^80.0 in 40304) [DefaultSimilarity], result of:\n        2.3450134 = score(doc=40304,freq=1.0), product of:\n          0.33538246 = queryWeight, product of:\n            80.0 = boost\n            6.9920573 = idf(docFreq=450, maxDocs=180507)\n            5.995776E-4 = queryNorm\n          6.9920573 = fieldWeight in 40304, product of:\n            1.0 = tf(freq=1.0), with freq of:\n              1.0 = termFreq=1.0\n            6.9920573 = idf(docFreq=450, maxDocs=180507)\n            1.0 = fieldNorm(doc=40304)\n  0.033496123 = FunctionQuery(sum(100.0*float(sqrt(log(1.0*float(long(searchCount))+2.0)))+1.0)), product of:\n    55.8662 = sum(100.0*float(sqrt(log(1.0*float(long(searchCount)=0)+2.0)))+1.0)\n    1.0 = boost\n    5.995776E-4 = queryNorm\n";
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
