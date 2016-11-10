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

    //public static void main(String[] args) throws InterruptedException {
    //    //Logger logger = LoggerFactory.getLogger(OrderTest.class);
    //    Date endTime = TimeUtils.addDay(new Date(), -1);
    //    //头15天
    //    Date startTime = endTime;
    //    List<AffiliateOrder> orderList = new ArrayList<>();
    //    orderList.addAll(getOrderList("affiliate357", "56e46c994b92488c91e43fad138d5c71", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    //orderList.addAll(getOrderList("affiliate357", "56e46c994b92488c91e43fad138d5c71", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    //System.out.println("over affiliate357");
    //    //orderList.addAll(getOrderList("xyangryrg", "c9c9b3d833054bf490c9989ac602b852", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    ////orderList.addAll(getOrderList("xyangryrg", "c9c9b3d833054bf490c9989ac602b852", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    ////System.out.println("over xyangryrg");
    //    //orderList.addAll(getOrderList("zhouxixi0", "55b1c6fa469b4e0296bb00259faf4056", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    ////orderList.addAll(getOrderList("zhouxixi0", "55b1c6fa469b4e0296bb00259faf4056", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    ////System.out.println("over zhouxixi0");
    //    //orderList.addAll(getOrderList("harveyouo", "c54bfd150ea74047a9233a4c3c3d356c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    ////orderList.addAll(getOrderList("harveyouo", "c54bfd150ea74047a9233a4c3c3d356c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    ////System.out.println("over harveyouo" );
    //    //orderList.addAll(getOrderList("allenooou", "857de2a9c43e40bfbdf572db3d728db4", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    ////orderList.addAll(getOrderList("allenooou", "857de2a9c43e40bfbdf572db3d728db4", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    ////System.out.println("over allenooou" );
    //    //orderList.addAll(getOrderList("747306881", "f3ec71e03799496d8b73c38b5456fb0b", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    ////orderList.addAll(getOrderList("747306881", "f3ec71e03799496d8b73c38b5456fb0b", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    ////System.out.println("over 747306881");
    //    //orderList.addAll(getOrderList("hlhakeygm", "01cfa560bac247eaa8a37f57fa8149f8", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    ////orderList.addAll(getOrderList("hlhakeygm", "01cfa560bac247eaa8a37f57fa8149f8", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    ////System.out.println("over hlhakeygm");
    //    //orderList.addAll(getOrderList("oliviersl", "6cf21891892d4bd8b839d85d51ac809c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    ////orderList.addAll(getOrderList("oliviersl", "6cf21891892d4bd8b839d85d51ac809c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    ////System.out.println("over oliviersl");
    //    //orderList.addAll(getOrderList("wuningSFg", "04bece2ed64945a3bce45c2f51293ef0", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    //orderList.addAlvl(getOrderList("wuningSFg", "04bece2ed64945a3bce45c2f51293ef0", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    //System.out.println("over wuningSFg");
    //    //TimeUnit.SECONDS.sleep(5);
    //    //orderList.addAll(getOrderList("hlhakeygm", "01cfa560bac247eaa8a37f57fa8149f8", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    //orderList.addAll(getOrderList("hlhakeygm", "01cfa560bac247eaa8a37f57fa8149f8", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    //System.out.println("over hlhakeygm: order.size="+orderList.size());
    //    //TimeUnit.SECONDS.sleep(5);
    //    //orderList.addAll(getOrderList("oliviersl", "6cf21891892d4bd8b839d85d51ac809c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    //orderList.addAll(getOrderList("oliviersl", "6cf21891892d4bd8b839d85d51ac809c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    //System.out.println("over oliviersl: order.size="+orderList.size());
    //    //TimeUnit.SECONDS.sleep(5);
    //    //orderList.addAll(getOrderList("wuningSFg", "04bece2ed64945a3bce45c2f51293ef0", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
    //    //orderList.addAll(getOrderList("wuningSFg", "04bece2ed64945a3bce45c2f51293ef0", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
    //    System.out.println("over wuningSFg: order.size=" + orderList.size());
    //    System.out.println(orderList.size());
    //    for (AffiliateOrder order : orderList) {
    //        System.out.println(order.getAffID() + "_" + order.getTitle() + "_" + order.getTitle().length());
    //    }
    //
    //
    //}

    //private static List<AffiliateOrder> getOrderList(String affId, String token, String orderState, Date startTime, Date endTime) {
    //    IAffiliateProcessor<AffiliateOrder> flipProcessor = new FlipkartAffiliateProductProcessor();
    //    Map<String, String> headerMap = new HashMap<>();
    //    headerMap.put("Fk-Affiliate-Id", affId);
    //    headerMap.put("Fk-Affiliate-Token", token);
    //    Map<String, String> approvedParamMap = new HashMap<>();
    //    approvedParamMap.put(FlipkartAffiliateProductProcessor.R_START_DATE, DateFormatUtils.format(startTime, "yyyy-MM-dd"));
    //    approvedParamMap.put(FlipkartAffiliateProductProcessor.R_END_DATE, DateFormatUtils.format(endTime, "yyyy-MM-dd"));
    //    approvedParamMap.put(FlipkartAffiliateProductProcessor.R_ORDER_STATUS, orderState);
    //    approvedParamMap.put(FlipkartAffiliateProductProcessor.R_OFFSET, "0");
    //    return flipProcessor.getAffiliateOrderList(headerMap, approvedParamMap);
    //}

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
        int[] days = new int[]{0, 25, 40, 50};

        for (int i : days) {
            Date day = TimeUtils.addDay(new Date(), -i);
            List<OrderStatsAnalysisPO> orderStatsAnalysisPOs = flipkartAffiliateService.countOrderList(day, day);
            for (OrderStatsAnalysisPO po : orderStatsAnalysisPOs) {
                System.out.println(po);
            }
        }
    }

}
