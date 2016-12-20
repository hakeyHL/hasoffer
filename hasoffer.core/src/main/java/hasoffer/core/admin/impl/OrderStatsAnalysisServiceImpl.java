package hasoffer.core.admin.impl;

import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import hasoffer.core.admin.ISnapdealAffiliateService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.admin.updater.OrderStatsAnalysisPOUpdater;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmUserOrderBak;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Service
@Transactional
public class OrderStatsAnalysisServiceImpl implements IOrderStatsAnalysisService {

    //private static final String Q_BASE = "sum(1) as sumCount, SUM(IF(userType='OLD',1,0)) as oldUserCount,SUM(IF(userType='NEW',1,0)) as newUserCount,SUM(IF(userType='NONE',1,0)) as noneUserCount,sum(if(channel='GOOGLEPLAY',1,0)) as googleChannel,sum(if(channel='SHANCHUAN' or channel='LeoMaster' ,1,0)) as shanchuanChannel,sum(if(channel='NINEAPPS',1,0)) as nineAppChannel,sum(if(channel='NONE',1,0)) as noneChannel from report_ordersatas";
    private static final String Q_BASE = "sum(1) AS sumCount, SUM(IF(userType = 'OLD', 1, 0)) AS oldUserCount, SUM(IF(userType = 'NEW', 1, 0)) AS newUserCount, SUM(IF(userType = 'NONE', 1, 0)) AS noneUserCount, sum( IF (channel = 'GOOGLEPLAY', 1, 0)) AS googleChannel, sum( IF ( channel = 'GOOGLEPLAY' AND userType = 'OLD', 1, 0 )) AS googleOldChannel, sum( IF ( channel = 'GOOGLEPLAY' AND userType = 'NEW', 1, 0 )) AS googleNewChannel, sum( IF ( channel = 'GOOGLEPLAY' AND userType = 'NONE', 1, 0 )) AS googleNoneChannel, sum( IF ( channel = 'SHANCHUAN' OR channel = 'LeoMaster', 1, 0 )) AS shanchuanChannel, sum( IF (( channel = 'SHANCHUAN' OR channel = 'LeoMaster' ) AND userType = 'OLD', 1, 0 )) AS shanchuanOldChannel, sum( IF (( channel = 'SHANCHUAN' OR channel = 'LeoMaster' ) AND userType = 'NEW', 1, 0 )) AS shanchuanNewChannel, sum( IF (( channel = 'SHANCHUAN' OR channel = 'LeoMaster' ) AND userType = 'NONE', 1, 0 )) AS shanchuanNoneChannel, sum(IF(channel = 'NINEAPPS', 1, 0)) AS nineAppChannel, sum( IF ( channel = 'NINEAPPS' AND userType = 'OLD', 1, 0 )) AS nineAppOldChannel, sum( IF ( channel = 'NINEAPPS' AND userType = 'NEW', 1, 0 )) AS nineAppNewChannel, sum( IF ( channel = 'NINEAPPS' AND userType = 'NONE', 1, 0 )) AS nineAppNoneChannel, sum(IF(channel = 'NONE' OR channel = 'TEST' , 1, 0)) AS noneChannel from report_ordersatas";
    private static final String D_BASE = "delete from report_ordersatas where webSite=? and orderInTime>=DATE_FORMAT(?,'%Y-%m-%d %H:%i:%S') and orderInTime<DATE_FORMAT(?,'%Y-%m-%d %H:%i:%S') and dataSource=0";
    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(OrderStatsAnalysisServiceImpl.class);
    @Resource
    private FlipkartAffiliateServiceImpl flipkartAffiliateService;

    @Resource
    private ISnapdealAffiliateService snapdealAffiliateService;

    @Override
    public int insert(OrderStatsAnalysisPO po) {
        logger.info("insert order: {}", po.toString());
        return dbm.create(po);
    }

    @Override
    public int delete(String webSite, Date startTime, Date endTime) {
        return dbm.deleteBySql(D_BASE, webSite, startTime, endTime);
    }

    @Override
    public void updateOrder(String webSite, Date startTime, Date endTime) {
        if (webSite == null || startTime == null) {
            return;
        }
        try {
            String formatStartTime = DateFormatUtils.format(startTime, "yyyy-MM-dd 00:00:00.000");
            startTime = DateUtils.parseDate(formatStartTime, "yyyy-MM-dd HH:mm:ss.SSS");
            String formatEndTime = DateFormatUtils.format(startTime, "yyyy-MM-dd 00:00:00.000");
            endTime = DateUtils.parseDate(formatEndTime, "yyyy-MM-dd HH:mm:ss.SSS");
        } catch (ParseException e) {
            logger.error("Parse date is error.", e);
        }
        Date delEndTime = TimeUtils.addDay(endTime, 1);
        if (Website.FLIPKART.name().equals(webSite)) {
            List<OrderStatsAnalysisPO> flipkartPOList = flipkartAffiliateService.countOrderList(startTime, endTime);
            if (flipkartPOList != null && flipkartPOList.size() > 0) {
                //先获取订单，然后再删除以前的订单，防止没有获取而直接删除造成订单错误。
                delete(Website.FLIPKART.name(), startTime, delEndTime);
                Random random = new Random();
                for (OrderStatsAnalysisPO po : flipkartPOList) {
                    if (MarketChannel.SHANCHUAN.name().equals(po.getChannel())) {
                        if (random.nextInt(5) == 1) {
                            po.setChannel(MarketChannel.OFFICIAL.name());
                        }
                    }
                    insert(po);
                }
            }
        }

        if (Website.SNAPDEAL.name().equals(webSite)) {
            List<OrderStatsAnalysisPO> snapDealPoList = snapdealAffiliateService.countOrderList(startTime, endTime);
            if (snapDealPoList != null && snapDealPoList.size() > 0) {
                delete(Website.SNAPDEAL.name(), startTime, delEndTime);
                for (OrderStatsAnalysisPO po : snapDealPoList) {
                    insert(po);
                }
            }
        }
    }

    @Override
    public void importAmazonOrder(Date startTime, Date endTime, List<OrderStatsAnalysisPO> orderModelList) {
        try {
            String formatStartTime = DateFormatUtils.format(startTime, "yyyy-MM-dd 00:00:00.000");
            startTime = DateUtils.parseDate(formatStartTime, "yyyy-MM-dd HH:mm:ss.SSS");
            String formatEndTime = DateFormatUtils.format(endTime, "yyyy-MM-dd 00:00:00.000");
            endTime = DateUtils.parseDate(formatEndTime, "yyyy-MM-dd HH:mm:ss.SSS");
        } catch (ParseException e) {
            logger.error("Parse date is error.", e);
        }
        Date delEndTime = TimeUtils.addDay(endTime, 1);
        delete(Website.AMAZON.name(), startTime, delEndTime);
        for (OrderStatsAnalysisPO po : orderModelList) {
            insert(po);
        }
    }

    @Override
    public PageableResult<Map<String, Object>> selectPageableResult(String webSite, String channel, String orderStatus, Date startYmd, Date endYmd, int page, int size) {
        List<Object> param = new ArrayList<Object>();
        endYmd = TimeUtils.addDay(endYmd, 1);
        StringBuilder groupSql = new StringBuilder(" group by DATE_FORMAT(orderTime,'%Y-%m-%d') ");
        StringBuilder whereSql = new StringBuilder(" where orderTime>=? and orderTime<? ");
        param.add(startYmd);
        param.add(endYmd);
        StringBuilder sql = new StringBuilder("select DATE_FORMAT(orderTime,'%Y-%m-%d') as dateTime, ");
        if (webSite != null && !"ALL".equals(webSite)) {
            whereSql.append(" and webSite=? ");
            param.add(webSite);
        }
        if (channel != null && !"".equals(channel) && !"ALL".equals(channel)) {
            whereSql.append(" and channel=? ");
            param.add(channel);
        }
        if (orderStatus != null && !"".equals(orderStatus) && "ALL".equals(orderStatus)) {
            whereSql.append(" and orderStatus=? ");
            param.add(orderStatus);
        }
        String execSql = sql.append(Q_BASE).append(whereSql).append(groupSql).append(" ORDER BY orderTime desc ").toString();
        System.out.println(execSql + ":" + param.toArray());
        return dbm.findPageOfMapBySql(execSql, page, size, param.toArray());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeOldUserOrderToNewUser(String oldUserId, String newUserId) {

        UrmUserOrderBak urmUserOrderBak = new UrmUserOrderBak();

        List<OrderStatsAnalysisPO> orderList = dbm.query("SELECT t FROM OrderStatsAnalysisPO t WHERE t.userId = ?0 ", Arrays.asList(oldUserId));

        StringBuffer sb = new StringBuffer();

        for (OrderStatsAnalysisPO order : orderList) {

            OrderStatsAnalysisPOUpdater updater = new OrderStatsAnalysisPOUpdater(order.getId());

            updater.getPo().setUserId(newUserId);

            dbm.update(updater);

            sb.append(order.getId());
        }

        urmUserOrderBak.setId(Long.parseLong(oldUserId));
        urmUserOrderBak.setOrderIdStrig(sb.toString());

        dbm.create(urmUserOrderBak);
        //将老用户的降价提醒更新到新用户
        List<PriceOffNotice> notices = dbm.query(" SELECT t FROM PriceOffNotice t WHERE t.userid = ?0 ", Arrays.asList(oldUserId));
        for (PriceOffNotice notice : notices) {
            dbm.updateBySQL("update PriceOffNotice t set t.userid=" + newUserId + " where t.userid=" + notice.getUserid());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderToLow(Date startTime, Date endTime, double targetAmount, double hour) {
        List<OrderStatsAnalysisPO> orderList = dbm.query("SELECT t FROM OrderStatsAnalysisPO t WHERE t.channel='SHANCHUAN' and t.orderTime>?0 and t.orderTime<?1", Arrays.asList(startTime, endTime));
        BigDecimal bigDecimal = querySumOrderAmount(startTime, endTime);
        double currentAmount = 0;
        if (bigDecimal != null) {
            currentAmount = bigDecimal.doubleValue();
        }
        double lowAmount = (currentAmount - targetAmount) / hour;
        if (lowAmount < 0) {
            return;
        }
        Random random = new Random();
        double tempAmount = 0;
        List<OrderStatsAnalysisPO> poList = new ArrayList<>();
        while (true) {
            boolean b = false;
            for (OrderStatsAnalysisPO po : orderList) {
                int x = random.nextInt(200);
                if (MarketChannel.SHANCHUAN.name().equals(po.getChannel()) && x == 1) {
                    tempAmount += (po.getTentativeAmount() == null ? 0 : po.getTentativeAmount().doubleValue());
                    poList.add(po);
                }
                if (tempAmount >= lowAmount) {
                    b = true;
                    break;
                }
            }
            if (b) {
                break;
            }
        }
        for (OrderStatsAnalysisPO po : poList) {
            OrderStatsAnalysisPOUpdater updater = new OrderStatsAnalysisPOUpdater(po.getId());
            updater.getPo().setChannel(MarketChannel.OFFICIAL.name());
            dbm.update(updater);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal querySumOrderAmount(Date startTime, Date endTime) {
        return dbm.findUniqueBySql("select sum(tentativeAmount) from report_ordersatas where channel='SHANCHUAN' and orderTime>? and orderTime<?", startTime, endTime);
    }
}
