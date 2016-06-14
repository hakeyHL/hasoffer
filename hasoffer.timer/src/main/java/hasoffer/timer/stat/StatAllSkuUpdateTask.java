package hasoffer.timer.stat;

import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.stat.StatPtmCmpSkuUpdate;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.fetch.helper.WebsiteHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Created on 2016/5/30.
 */
@Component
public class StatAllSkuUpdateTask {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final String Q_WEBSITE_PTMCMPSKU_ONSALE = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.status = 'ONSALE' ";
    private static final String Q_WEBSITE_PTMCMPSKU_SOLDOUT = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.status = 'OUTSTOCK' ";
    private static final String Q_WEBSITE_PTMCMPSKU_OFFSALE = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.status = 'OFFSALE' ";
    private static final String Q_WEBSITE_PTMCMPSKU_ALL = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 ";
    private static final String Q_WEBSITE_PTMCMPSKU_UPDATE_SUCCESS = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.updateTime > '" + TimeUtils.parse(TimeUtils.today(), DATE_PATTERN) + "'";
    private static final String Q_WEBSITE_PTMCMPSKU_UPDATE_ALWAYSFAIL = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.updateTime < '" + TimeUtils.parse(TimeUtils.addDay(TimeUtils.toDate(TimeUtils.today()), -3), DATE_PATTERN) + "'";
    private static final String Q_WEBSITE_PTMCMPSKU_CREATE_TODAY = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.createTime > '" + TimeUtils.parse(TimeUtils.today(), DATE_PATTERN) + "'";
    private static final String Q_WEBSITE_PTMCMPSKU_INDEX = "SELECT COUNT(*) FROM PtmCmpSkuIndex2 t WHERE t.website = ?0 ";
    private static final String Q_WEBSITE_PTMCMPSKU_INDEX_NEW = "SELECT COUNT(*) FROM PtmCmpSkuIndex2 t WHERE t.website = ?0 AND t.createTime > '" + TimeUtils.parse(TimeUtils.today(), DATE_PATTERN) + "'";


    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuService cmpSkuService;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void statSkuUpdate() {

        //1.循环7个网站
        //1.1按照网站和当日日期为id，创建或者更新记录
        /*
            1.日期，20160530
            2.网站，website
            3.更新时间,updateTime
            4.onsale数量，select count(*) from ptmcmpsku where t.website = ?0 and t.status = 'onsale'
            5.soldout数量，select count(*) from ptmcmpsku where t.website = ?0 and t.status = 'stockout'
            6.offsale数量，select count(*) from ptmcmpsku where t.website = ?0 and t.status = 'offsale'
            //此处关注下status=null的处理
            7.总量，select count(*) from ptmcmpsku where t.website = ?0
            8.更新成功数量，select count(*) from ptmcmpsku where t.website = ?0 and t.updateTime > '当日零点'
            9.3天以上未更新，select count(*) from ptmcmpsku where t.website = ?0 and t.updateTime < '当日零点-3天'
            10.今天新增sku，select count(*) from ptmcmpsku where t.website = ?0 and t.createTime > '当日零点'
            11.index数量，select count(*) from ptmcmpskuindex2 where t.website = ?0
            12.新增index数量，select count(*) from ptmcmpskuindex2 where t.website = ?0 and t.createTime > '当日零点'
         */

        for (Website website : WebsiteHelper.DEFAULT_WEBSITES) {
            if (Website.FLIPKART.equals(website) || Website.SNAPDEAL.equals(website) || Website.SHOPCLUES.equals(website) || Website.PAYTM.equals(website) || Website.AMAZON.equals(website) || Website.EBAY.equals(website) || Website.INFIBEAM.equals(website)) {


                String todayString = TimeUtils.parse(TimeUtils.today(), "yyyyMMdd");//20160530

                String id = HexDigestUtil.md5(website.name() + todayString);

                long onSaleAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_ONSALE, Arrays.asList(website));
                long soldOutAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_SOLDOUT, Arrays.asList(website));
                long offsaleAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_OFFSALE, Arrays.asList(website));
                long allAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_ALL, Arrays.asList(website));
                long updateSuccessAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_UPDATE_SUCCESS, Arrays.asList(website));
                long alwaysFailAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_UPDATE_ALWAYSFAIL, Arrays.asList(website));
                long newSkuAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_CREATE_TODAY, Arrays.asList(website));
                long indexAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_INDEX, Arrays.asList(website));
                long newIndexAmount = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_INDEX_NEW, Arrays.asList(website));

                //save or update
                StatPtmCmpSkuUpdate skuUpdateStat = dbm.get(StatPtmCmpSkuUpdate.class, id);

                if (skuUpdateStat == null) {

                    skuUpdateStat = new StatPtmCmpSkuUpdate(id, allAmount, alwaysFailAmount, TimeUtils.toDate(TimeUtils.today()), indexAmount, newIndexAmount, newSkuAmount, offsaleAmount, onSaleAmount, soldOutAmount, updateSuccessAmount, TimeUtils.nowDate(), website);

                    cmpSkuService.createStatPtmCmpSkuUpdate(skuUpdateStat);
                } else {

                    cmpSkuService.updateStatPtmCmpSkuUpdate(id, onSaleAmount, soldOutAmount, offsaleAmount, allAmount, updateSuccessAmount, alwaysFailAmount, newSkuAmount, indexAmount, newIndexAmount);
                }

            }
        }

    }

}
