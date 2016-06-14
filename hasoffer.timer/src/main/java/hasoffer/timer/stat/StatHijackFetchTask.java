package hasoffer.timer.stat;

import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.StatHijackFetch;
import hasoffer.core.persistence.mongo.StatHijackFetchCount;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created on 2016/6/6.
 */
@Component
public class StatHijackFetchTask {


    @Resource
    IMongoDbManager mdm;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void f() {

        //4.劫持失败数
        List<Website> websiteList = new ArrayList<Website>();
        websiteList.add(Website.FLIPKART);
        websiteList.add(Website.SNAPDEAL);
        websiteList.add(Website.SHOPCLUES);

        String todayString = TimeUtils.parse(TimeUtils.today(), "yyyyMMdd");//20160530

        for (Website website : websiteList) {

            String id = HexDigestUtil.md5(website.name() + todayString); //1.当日零点做为id

            long totalAmount = getTotalAmount(website); //2.StatHijackFetch中当天的数据的总数作为当体应劫持总数
            long statusSuccessAmount = getStatusSuccessAmount(website);//3.劫持成功数
            long noIndexAmount = getNoIndexAmount(website);  //6.未收录--no_index
            long differentUrlAmount = getDifferentUrlAmount(website);//5.因重名失败——different_url
            long noIndexSuccessAmount = getNoIndexSuccessAmount(website);//7.no_index   result:success
            long noIndexFailAmount = getNoIndexFailAmount(website); //8.no_index   result:fail

            StatHijackFetchCount countObject = new StatHijackFetchCount();

            countObject.setId(id);
            countObject.setWebsite(website);
            try {
                Date date = TimeUtils.parse(TimeUtils.parse(TimeUtils.today(), "yyyy-MM-dd"));
                countObject.setDate(date);
            } catch (ParseException e) {

            }
            countObject.setUpdateTime(TimeUtils.now());

            countObject.setTotalAmount(totalAmount);
            countObject.setStatusSuccessAmount(statusSuccessAmount);
            countObject.setNoIndexAmount(noIndexAmount);
            countObject.setDifferentUrlAmount(differentUrlAmount);
            countObject.setNoIndexSuccessAmount(noIndexSuccessAmount);
            countObject.setNoIndexFailAmount(noIndexFailAmount);
            countObject.setStatusFailAmount(differentUrlAmount + noIndexAmount);

            mdm.save(countObject);
        }
    }

    private long getTotalAmount(Website website) {
        Query query = new Query();
        query.addCriteria(Criteria.where("website").is(website.name()));
        query.addCriteria(Criteria.where("lCreateTime").gt(TimeUtils.getDayStart(TimeUtils.now())));
        long totalAmount = mdm.count(StatHijackFetch.class, query);
        return totalAmount;
    }

    private long getStatusSuccessAmount(Website website) {
        Query query = new Query();
        query.addCriteria(Criteria.where("website").is(website.name()));
        query.addCriteria(Criteria.where("status").is("SUCCESS"));
        query.addCriteria(Criteria.where("lCreateTime").gt(TimeUtils.getDayStart(TimeUtils.now())));
        long statusSuccessAmount = mdm.count(StatHijackFetch.class, query);
        return statusSuccessAmount;
    }

    private long getNoIndexAmount(Website website) {
        Query query = new Query();
        query.addCriteria(Criteria.where("website").is(website.name()));
        query.addCriteria(Criteria.where("status").is("NO_INDEX"));
        query.addCriteria(Criteria.where("lCreateTime").gt(TimeUtils.getDayStart(TimeUtils.now())));
        long noIndexAmount = mdm.count(StatHijackFetch.class, query);
        return noIndexAmount;
    }

    private long getDifferentUrlAmount(Website website) {
        Query query = new Query();
        query.addCriteria(Criteria.where("website").is(website.name()));
        query.addCriteria(Criteria.where("status").is("DIFFERENT_URL"));
        query.addCriteria(Criteria.where("lCreateTime").gt(TimeUtils.getDayStart(TimeUtils.now())));
        long differentUrlAmount = mdm.count(StatHijackFetch.class, query);
        return differentUrlAmount;
    }

    private long getNoIndexSuccessAmount(Website website) {
        Query query = new Query();
        query.addCriteria(Criteria.where("website").is(website.name()));
        query.addCriteria(Criteria.where("status").is("NO_INDEX"));
        query.addCriteria(Criteria.where("result").is("success"));
        query.addCriteria(Criteria.where("lCreateTime").gt(TimeUtils.getDayStart(TimeUtils.now())));
        long noIndexSuccessAmount = mdm.count(StatHijackFetch.class, query);
        return noIndexSuccessAmount;
    }

    private long getNoIndexFailAmount(Website website) {
        Query query = new Query();
        query.addCriteria(Criteria.where("website").is(website.name()));
        query.addCriteria(Criteria.where("status").is("NO_INDEX"));
        query.addCriteria(Criteria.where("result").is("fail"));
        query.addCriteria(Criteria.where("lCreateTime").gt(TimeUtils.getDayStart(TimeUtils.now())));
        long noIndexFailAmount = mdm.count(StatHijackFetch.class, query);
        return noIndexFailAmount;
    }

}
