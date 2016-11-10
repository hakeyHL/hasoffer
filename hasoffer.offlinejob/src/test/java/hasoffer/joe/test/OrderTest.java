package hasoffer.joe.test;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IFlipkartAffiliateService;
import hasoffer.core.admin.ISnapdealAffiliateService;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-beans.xml"})
public class OrderTest {

    @Resource
    private ISnapdealAffiliateService snapdealAffiliateService;

    @Resource
    private IFlipkartAffiliateService flipkartAffiliateService;

    @org.junit.Test
    public void orderSnapDealTest() {
        try {
            List<OrderStatsAnalysisPO> orderStatsAnalysisPOs = snapdealAffiliateService.countOrderList(DateUtils.parseDate("2016-11-07", "yyyy-MM-dd"), DateUtils.parseDate("2016-11-09", "yyyy-MM-dd"));
            for (OrderStatsAnalysisPO po : orderStatsAnalysisPOs) {
                System.out.println(po);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void orderFlipkartTest() {
        int[] days = new int[]{0};

        for (int i : days) {
            Date day = TimeUtils.addDay(new Date(), -i);
            List<OrderStatsAnalysisPO> orderStatsAnalysisPOs = flipkartAffiliateService.countOrderList(day, day);
            for (OrderStatsAnalysisPO po : orderStatsAnalysisPOs) {
                System.out.println(po);
            }
        }
    }

}
