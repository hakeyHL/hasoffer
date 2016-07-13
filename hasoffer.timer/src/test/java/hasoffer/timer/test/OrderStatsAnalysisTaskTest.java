package hasoffer.timer.test;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class OrderStatsAnalysisTaskTest {


    @Resource
    private IOrderStatsAnalysisService orderStatsAnalysisService;

    @Test
    public void testCountFail() {
        long currentTimes = System.currentTimeMillis();
        long cycleTime = 1000 * 60 * 60 * 24;
        long cycleIndex = currentTimes / cycleTime;
        Date todayTime = new Date();
        //头15天
        Date day15AgoTime = TimeUtils.addDay(todayTime, -15);
        //头三天
        Date day3AgoTime =  TimeUtils.addDay(todayTime, -3);
        //头两天
        Date day2AgoTime =  TimeUtils.addDay(todayTime, -2);
        //头一天
        Date day1AgoTime =  TimeUtils.addDay(todayTime, -1);
        //头三天
//        Date day20AgoTime = new Date(cycleIndex * cycleTime - cycleTime * 20);
//        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day20AgoTime, day20AgoTime);
//        //头三天
//        Date day15AgoTime = new Date(cycleIndex * cycleTime - cycleTime * 15);
//        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day15AgoTime, day15AgoTime);
//        //头三天
//        Date day14AgoTime = new Date(cycleIndex * cycleTime - cycleTime * 14);
//        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day14AgoTime, day14AgoTime);
        //头三天
//        Date day13AgoTime = new Date(cycleIndex * cycleTime);
//        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day13AgoTime, day13AgoTime);

        //头三天
//        Date day3AgoTime = new Date(cycleIndex * cycleTime - cycleTime * 3);
//        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day3AgoTime, day3AgoTime);
//        orderStatsAnalysisService.updateOrder(Website.FLIPKART.toString(), day3AgoTime, day3AgoTime);
//        //头两天
//        Date day2AgoTime = new Date(cycleIndex * cycleTime - cycleTime * 2);
//        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day2AgoTime, day2AgoTime);
//        orderStatsAnalysisService.updateOrder(Website.FLIPKART.toString(), day2AgoTime, day2AgoTime);
//        //头一天
//        Date day1AgoTime = new Date(cycleIndex * cycleTime - cycleTime);
//        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day1AgoTime, day1AgoTime);
//        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day1AgoTime, day1AgoTime);

//        Date todayTime = new Date(cycleIndex * cycleTime);
        orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day1AgoTime, todayTime);
//        orderStatsAnalysisService.updateOrder(Website.FLIPKART.toString(), todayTime, todayTime);

    }

}
