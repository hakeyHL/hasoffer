package hasoffer.timer.test;

import hasoffer.core.admin.IHiJackReportService;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class HiJackReportTaskTest {

    @Resource
    IHiJackReportService hiJackReportService;


    @Test
    public void testCountFail() {
        try {
            hiJackReportService.countHiJack(DateUtils.parseDate("2016-05-10 00:00", "yyyy-MM-dd HH:mm"), DateUtils.parseDate("2016-05-26 00:00", "yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDate() {
        //2016-05-25 06:56:34.257Z
        Date date = new Date(1464159394257L);
        System.out.println(date);
    }



}
