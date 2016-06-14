package hasoffer.timer.userbuy;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.UrmDeviceBuyLog;
import hasoffer.core.persistence.po.stat.StatUserBuy;
import hasoffer.core.user.IBuyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/4/11.
 */
@Component
public class StatUserBuyTask {

    private static Logger logger = LoggerFactory.getLogger(StatUserBuyTask.class);

    private static final String Q_MAX_LASTBUYTIME =
            "SELECT MAX(t.lastBuyTime) FROM StatUserBuy t ";

    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    IBuyService buyService;

    @Scheduled(cron = "0 0 0/12 * * ?")
    public void parseStatUserBuy() {
        //结束时间为当前时间前2个小时，考虑timer中buyLog的转化时间
        Date now = TimeUtils.beforeXm(120);

        Date start = dbm.querySingle(Q_MAX_LASTBUYTIME);
        if (start == null) {
            //如果没有更新时间，就已buyLog中的最小createTime作为起始时间
            start = TimeUtils.stringToDate("2016-03-01 00:00:00", "yyyy-MM-dd hh:mm:ss");
        }

        while (true) {

            if (start.getTime() > now.getTime() - TimeUtils.MILLISECONDS_OF_1_HOUR * 12) {
                //如果start + 1天 > now ,直接break
                logger.debug("-------------------------mession---------success-------------------------");
                break;
            }

            Date end = null;
            //如果 end - start > 1天，考虑mongodb查询的压力，就循环查询一天的数据
            if (now.getTime() - start.getTime() > TimeUtils.MILLISECONDS_OF_1_HOUR * 24) {
                end = TimeUtils.addDay(start, 1);
            } else {
                end = now;
            }

            Criteria c = Criteria.where("createTime").gt(start).lt(end);
            Query query = new Query(c);
            query.with(new Sort(Sort.Direction.DESC, "createTime"));

            PageableResult<UrmDeviceBuyLog> pagedResult = mdm.queryPage(UrmDeviceBuyLog.class, query, 1, Integer.MAX_VALUE);

            List<UrmDeviceBuyLog> data = pagedResult.getData();

            if (data.size() == 0) {
                start = end;
                continue;
            }

            Map<Long, StatUserBuy> buyMap = new HashMap<Long, StatUserBuy>();

            //遍历mongodb中返回的数据集合
            for (UrmDeviceBuyLog urmDeviceBuyLog : data) {
                long ptmProductId = urmDeviceBuyLog.getPtmProductId();
                Date createDate = urmDeviceBuyLog.getCreateTime();

                if (buyMap.containsKey(ptmProductId)) {
                    StatUserBuy statUserBuy1 = buyMap.get(ptmProductId);
                    statUserBuy1.setCount(statUserBuy1.getCount() + 1);
                    statUserBuy1.setLastBuyTime(createDate);
                } else {
                    StatUserBuy statUserBuy = new StatUserBuy();
                    statUserBuy.setLastBuyTime(createDate);
                    statUserBuy.setId(ptmProductId);
                    statUserBuy.setCount(1);
                    buyMap.put(ptmProductId, statUserBuy);
                }
            }

            //遍历map集合中的数据
            for (Map.Entry<Long, StatUserBuy> entry : buyMap.entrySet()) {
                Long ptmProductId = entry.getKey();
                StatUserBuy entryValue = entry.getValue();

                StatUserBuy statUserBuy = dbm.get(StatUserBuy.class, ptmProductId);

                if (statUserBuy == null) { //该商品第一次被购买
                    buyService.createStatUserBuy(entryValue);
                } else { //该商品不是第一次购买
                    entryValue.setCount(entryValue.getCount() + statUserBuy.getCount());
                    buyService.updateStatUserBuy(ptmProductId, entryValue);
                }
            }

            logger.debug("--------------------------------------from " + start + " to " + end + " complete--------------------------------------");

            start = end;
        }
    }

}
