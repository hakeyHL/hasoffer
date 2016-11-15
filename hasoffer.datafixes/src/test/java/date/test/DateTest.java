package date.test;

import hasoffer.base.utils.TimeUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class DateTest {

    public static void main(String[] args) {
        Date startDate = null;
        try {
            startDate = DateUtils.parseDate("2016-08-10", "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date endDate = null;
        try {
            endDate = DateUtils.parseDate("2016-08-25", "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (Date day = startDate; day.compareTo(endDate) <= 0; day = TimeUtils.addDay(day, 1)) {
            System.out.println(DateFormatUtils.format(day, "yyyy-MM-dd"));
        }
    }

}
