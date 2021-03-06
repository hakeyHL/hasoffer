package hasoffer.joe.test;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IFlipkartAffiliateService;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import hasoffer.core.admin.ISnapdealAffiliateService;
import hasoffer.core.admin.IUrmAffAccountService;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.admin.UrmAffAccount;
import org.apache.commons.lang3.time.DateFormatUtils;
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

    @Resource
    private IUrmAffAccountService urmAffAccountService;

    @Resource
    private IOrderStatsAnalysisService orderStatsAnalysisService;

    @org.junit.Test
    public void testOrderSnapDeal() {
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
    public void testOrderFlipkart() {
        //int[] days = new int[]{0, 1, 2, 3, 4};
        int[] days = new int[]{1};
        for (int i : days) {
            Date day = TimeUtils.addDay(new Date(), -i);
            String formatStartTime = DateFormatUtils.format(day, "yyyy-MM-dd 00:00:00.000");
            Date startTime = null;
            try {
                startTime = DateUtils.parseDate(formatStartTime, "yyyy-MM-dd HH:mm:ss.SSS");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String formatEndTime = DateFormatUtils.format(day, "yyyy-MM-dd 00:00:00.000");
            Date endTime = null;
            try {
                endTime = DateUtils.parseDate(formatEndTime, "yyyy-MM-dd HH:mm:ss.SSS");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<OrderStatsAnalysisPO> orderStatsAnalysisPOs = flipkartAffiliateService.countOrderList(startTime, endTime);
            //Random random = new Random();
            for (OrderStatsAnalysisPO po : orderStatsAnalysisPOs) {
                //if (MarketChannel.SHANCHUAN.name().equals(po.getChannel())) {
                //    if (random.nextInt(8) == 1) {
                //        po.setChannel(MarketChannel.OFFICIAL.name());
                //    }
                //}
                System.out.println(po);
            }
        }
    }

    @org.junit.Test
    public void testAffAccount() {
        List<UrmAffAccount> affAccountList = urmAffAccountService.findAffAccountList(Website.FLIPKART);
        System.out.println(affAccountList.size());
        for (UrmAffAccount urmAffAccount : affAccountList) {
            System.out.println(urmAffAccount.toString());
        }
        affAccountList = urmAffAccountService.findAffAccountList(Website.SNAPDEAL);
        System.out.println(affAccountList.size());
    }

    @org.junit.Test
    public void testOrderStats() {
        try {
            Date date = new Date();
            Date targetDate = DateUtils.parseDate("2016-11-21", "yyyy-MM-dd");
            Long x = (targetDate.getTime() - date.getTime()) / (1000 * 60 * 60);

            Date start8mTime = DateUtils.parseDate("2016-08-01", "yyyy-MM-dd");
            Date end8mTime = DateUtils.parseDate("2016-08-31", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start8mTime, end8mTime, 592120, x);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}
