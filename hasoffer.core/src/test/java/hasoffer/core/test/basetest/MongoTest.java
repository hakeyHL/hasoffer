package hasoffer.core.test.basetest;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.aws.SrmProductSearchCount;
import hasoffer.core.persistence.dbm.aws.AwsDynamoDbService;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.enums.IndexStat;
import hasoffer.core.persistence.mongo.PtmCmpSkuLog;
import hasoffer.core.persistence.mongo.StatHijackFetch;
import hasoffer.core.persistence.mongo.UrmDeviceBuyLog;
import hasoffer.core.persistence.mongo.UrmDeviceRequestLog;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.user.IDeviceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/1/4.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class MongoTest {

    @Resource
    IMongoDbManager mdm;
    @Resource
    IDeviceService deviceService;
    @Resource
    ICmpSkuService cmpSkuService;


    @Test
    public void table() {
        AwsDynamoDbService.getInstance().createTable(SrmProductSearchCount.class);
//        AwsDynamoDbService.getInstance().deleteTable(SrmProductSearchCount.class);
    }

    @Test
    public void ts() {

        Map<Long, Long> countMap = new HashMap<Long, Long>();
        for (long i = 1; i < 100; i++) {
            countMap.put(i, i + 100);
        }

        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {

            long productId = countKv.getKey();
            System.out.println(productId);

            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId, SkuStatus.ONSALE);
            int size = 0;
            if (ArrayUtils.hasObjs(cmpSkus)) {
                size = cmpSkus.size();
            }

            SrmProductSearchCount productSearchCount = new SrmProductSearchCount(productId, "20160707", countKv.getValue(), size);
            AwsDynamoDbService.getInstance().save(productSearchCount);
        }
    }

    @Test
    public void expWords() {
        /*StringBuffer sb = new StringBuffer();

        File file = new File(String.format("d:/TMP/words_1_%d.csv", 0));

        Query query = new Query(Criteria.where("count").is(1));
        query.with(new Sort(Sort.Direction.DESC, "count"));

        int page = 1;
        while (true) {

            List<PtmTitleWordStat> wordStats = mdm.query(PtmTitleWordStat.class, query, page++, 500);
            if (page % 10 == 0) {
                file = new File(String.format("d:/TMP/words_1_%d.csv", page / 10));
                if (file.exists()) {
                    file.delete();
                }
                System.out.println("page = " + page);
            }

            if (ArrayUtils.isNullOrEmpty(wordStats)) {
                break;
            }

            for (int i = 0; i < wordStats.size(); i++) {
                PtmTitleWordStat wordStat = wordStats.get(i);
                sb.append(wordStat.getId()).append("\t").append("\t").append(wordStat.getCount()).append("\n");
            }

            try {
                FileUtil.appendString(file, sb.toString());
//            FileUtils.write(file, sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Test
    public void testUpdate() {
        Update update = new Update();
        update.set("ptmProductId", 100);
        update.set("title", "iPhone 5s1111");
        int count = mdm.update(UrmDeviceBuyLog.class, "56d4ea8ae4b0a11a45930a1b", update);
        System.out.println(count);
    }

    @Test
    public void fffffff() {
        final String DATE_PATTERN_FROM_WEB = "MM/dd/yyyy";

        Date startTime = TimeUtils.stringToDate("03/01/2016", DATE_PATTERN_FROM_WEB);
        Date endTime = TimeUtils.addDay(startTime, 1);

        PageableResult<UrmDeviceRequestLog> logs = deviceService.findDeviceLogsByRequestUri("/app/dot", startTime, endTime, 1, 20);

        System.out.println(logs.getNumFund());
    }

    @Test
    public void ffff() {
        final String DATE_PATTERN_FROM_WEB = "MM/dd/yyyy";

        Date startTime = TimeUtils.stringToDate("03/01/2016", DATE_PATTERN_FROM_WEB);
        Date endTime = TimeUtils.addDay(startTime, 1);

        PageableResult<UrmDeviceRequestLog> logs = deviceService.findDeviceLogsByUriAndQuery("/app/dot", "action=dot", startTime, endTime, 1, 50);

        System.out.println(logs.getNumFund());
    }


    @Test
    public void testQueryBuyLog() {

        final String DATE_PATTERN_FROM_WEB = "MM/dd/yyyy";

        Date startTime = TimeUtils.stringToDate("03/18/2016", DATE_PATTERN_FROM_WEB);
        Date endTime = TimeUtils.stringToDate("03/19/2016", DATE_PATTERN_FROM_WEB);

        PageableResult<UrmDeviceBuyLog> urmDeviceBugLog = deviceService.findUrmDeviceBuyLog(null, null, startTime, endTime, 1, 20, null, 0);

        List<UrmDeviceBuyLog> urmDeviceBuyLogList = urmDeviceBugLog.getData();

        System.out.println(urmDeviceBuyLogList);

    }

    @Test
    public void f() {
        for (int i = 0; i < 10; i += 2) {
            PtmCmpSkuLog csl = new PtmCmpSkuLog(new PtmCmpSku());
            mdm.save(csl);
        }
    }

    @Test
    public void find() {
        Query query = new Query(Criteria.where("pcsId").is(10L));
        query.with(new Sort(Sort.Direction.DESC, "priceTime"));

        PageableResult<PtmCmpSkuLog> list = mdm.queryPage(PtmCmpSkuLog.class, query, 1, 10);

        System.out.println(list.getData().size());

    }

    @Test
    public void find2() {
        Query query = new Query(Criteria.where("priceTime").gte(new Date(TimeUtils.today())));
//		query.with(new Sort(Sort.Direction.DESC, "priceTime"));

        PageableResult<PtmCmpSkuLog> list = mdm.queryPage(PtmCmpSkuLog.class, query, 1, 10);

        System.out.println(list.getNumFund());

    }

    @Test
    public void find3() {
        Date d2 = new Date(TimeUtils.tommorrow(0, 0, 0));
        Date d1 = TimeUtils.addDay(d2, -1);

        for (int i = 0; i < 10; i++) {
            Query query = new Query(Criteria.where("priceTime").gt(d1).lte(d2));

            long count = mdm.count(PtmCmpSkuLog.class, query);

            System.out.println(TimeUtils.timeAsString(d1) + "-" + count);

            d2 = TimeUtils.addDay(d2, -1);
            d1 = TimeUtils.addDay(d2, -1);
        }
    }

    @Test
    public void find4() {

        StatHijackFetch fetch = mdm.queryOne(StatHijackFetch.class, "aifhawofheowajfeafw");
        System.out.println();

    }

    @Test
    public void createTimeSave() {

        StatHijackFetch statHijackFetch = mdm.queryOne(StatHijackFetch.class, "2198d324acebf2a0490dd0023559de6e");

        String id = statHijackFetch.getId();
        Website website = statHijackFetch.getWebsite();
        String sourceId = statHijackFetch.getSourceId();
        String cliQ = statHijackFetch.getCliQ();
        Date createTime = TimeUtils.nowDate();
        long lCreateTime = TimeUtils.now();
        IndexStat status = IndexStat.DIFFERENT_URL;

        statHijackFetch = new StatHijackFetch(id, website, sourceId, cliQ, createTime, lCreateTime, status, null);

        mdm.save(statHijackFetch);

    }
}
