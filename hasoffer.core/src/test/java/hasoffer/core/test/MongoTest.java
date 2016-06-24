package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.enums.IndexStat;
import hasoffer.core.persistence.mongo.*;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.user.IDeviceService;
import jodd.io.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

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

    @Test
    public void expWords() {
        StringBuffer sb = new StringBuffer();

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
        }
    }

    @Test
    public void testSave() {
        SummaryProduct sp = new SummaryProduct();
        mdm.save(sp);
    }

    @Test
    public void testGet() {

        List<SummaryProduct> query = mdm.query(SummaryProduct.class, new Query(Criteria.where("website").is("SHOPCLUES")));

        for (SummaryProduct sp : query) {
            String subTitle = sp.getSubTitle();
            System.out.print(subTitle);
        }


    }

    @Test
    public void test() {

        Query query = new Query(Criteria.where("longUpdateTime").gt(TimeUtils.today() - 24 * 60 * 60 * 1000));
        query.with(new Sort(Sort.Direction.ASC, "longUpdateTime"));

        PageableResult<PtmCmpSkuFetchResult> pageableResult = mdm.queryPage(PtmCmpSkuFetchResult.class, query, 1, 1000);

        System.out.println(pageableResult);
    }

    @Test
    public void testUpdate1() {
//        PtmCmpSkuFetchResult r = new PtmCmpSkuFetchResult(100L, "333");
//        mdm.save(r);
        Update update = new Update();
        update.set("url", "111");
        int count = mdm.update(PtmCmpSkuFetchResult.class, 100, update);
        System.out.println(count);
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
    public void find4(){

        StatHijackFetch fetch = mdm.queryOne(StatHijackFetch.class, "aifhawofheowajfeafw");
        System.out.println();

    }

    @Test
    public void createTimeSave(){

        StatHijackFetch statHijackFetch = mdm.queryOne(StatHijackFetch.class, "2198d324acebf2a0490dd0023559de6e");

        String id = statHijackFetch.getId();
        Website website = statHijackFetch.getWebsite();
        String sourceId = statHijackFetch.getSourceId();
        String cliQ = statHijackFetch.getCliQ();
        Date createTime = TimeUtils.nowDate();
        long lCreateTime = TimeUtils.now();
        IndexStat status = IndexStat.DIFFERENT_URL;

        statHijackFetch = new StatHijackFetch(id,website,sourceId,cliQ,createTime,lCreateTime,status,null);

        mdm.save(statHijackFetch);

    }
}
