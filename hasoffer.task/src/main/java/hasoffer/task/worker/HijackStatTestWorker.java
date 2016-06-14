package hasoffer.task.worker;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.exception.CmpSkuUrlNotFoundException;
import hasoffer.core.exception.MultiUrlException;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.enums.IndexStat;
import hasoffer.core.persistence.mongo.StatHijackFetch;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuIndex2;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICmpSkuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/5/13.
 */
public class HijackStatTestWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(HijackStatTestWorker.class);

    private ConcurrentLinkedQueue<SrmSearchLog> queue;
    private ICmpSkuService cmpSkuService;
    private IMongoDbManager mdm;

    public HijackStatTestWorker(ConcurrentLinkedQueue<SrmSearchLog> queue, ICmpSkuService cmpSkuService, IMongoDbManager mdm) {
        this.queue = queue;
        this.cmpSkuService = cmpSkuService;
        this.mdm = mdm;
    }


    @Override
    public void run() {

        while (true) {

            SrmSearchLog log = queue.poll();

            if (log == null) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String cliQ = log.getKeyword();
            String sourceId = log.getSourceId();
            String site = log.getSite();
            Website website = Website.valueOf(site);

            if (!website.equals(Website.FLIPKART) && !website.equals(Website.SNAPDEAL) && !website.equals(Website.SHOPCLUES)) {
                continue;
            }

            PtmCmpSkuIndex2 cmpSkuIndex2 = null;
            StatHijackFetch statHijackFetch = null;

            try {

                StatHijackFetch hijackFetch = mdm.queryOne(StatHijackFetch.class, log.getId());
                if (hijackFetch != null) {
                    continue;
                }

                cmpSkuIndex2 = cmpSkuService.getCmpSkuIndex2(website, sourceId, cliQ);

                if (cmpSkuIndex2 == null) {//no index需要抓取
                    statHijackFetch = new StatHijackFetch(log.getId(), Website.valueOf(log.getSite()), log.getSourceId(), log.getKeyword(), TimeUtils.nowDate(), TimeUtils.now(), IndexStat.NO_INDEX, null);
                } else {
                    statHijackFetch = new StatHijackFetch(log.getId(), Website.valueOf(log.getSite()), log.getSourceId(), log.getKeyword(), TimeUtils.nowDate(), TimeUtils.now(), IndexStat.SUCCESS, null);
                }

            } catch (CmpSkuUrlNotFoundException e1) {
                //url为空
                statHijackFetch = new StatHijackFetch(log.getId(), Website.valueOf(log.getSite()), log.getSourceId(), log.getKeyword(), TimeUtils.nowDate(), TimeUtils.now(), IndexStat.NULL_URL, null);

            } catch (MultiUrlException e2) {
                //different url
                statHijackFetch = new StatHijackFetch(log.getId(), Website.valueOf(log.getSite()), log.getSourceId(), log.getKeyword(), TimeUtils.nowDate(), TimeUtils.now(), IndexStat.DIFFERENT_URL, null);

            }

            mdm.save(statHijackFetch);

//            List<PtmCmpSku> skuList = new ArrayList<PtmCmpSku>();
//
//            if (Website.FLIPKART.equals(website)) {
//                List<PtmCmpSku> flipkartProduct = getFlipkartProduct(cliQ, sourceId);
//                skuList.addAll(flipkartProduct);
//            } else if (Website.SNAPDEAL.equals(website)) {
//                List<PtmCmpSku> snapdealProduct = getSnapdealProduct(cliQ, sourceId);
//                skuList.addAll(snapdealProduct);
//            }
//
//            for (PtmCmpSku sku : skuList) {
//
//                List<PtmCmpSku> resultList = dbm.query(Q_PTMCMPSKU_BYSID, Arrays.asList(sku.getSourceSid()));
//                if (resultList != null && resultList.size() != 0) {
//                    logger.debug(sku.getSourceSid() + " exists");
//
//                    for (PtmCmpSku result : resultList) {
//
//                        Website resultWebsite = result.getWebsite();
//
//                        if (resultWebsite != null) {
//
//                            String cleanUrl = WebsiteHelper.getCleanUrl(resultWebsite, result.getUrl());
//                            String sourceSid = WebsiteHelper.getSkuIdFromUrl(resultWebsite, cleanUrl);
//
//                            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(result.getId());
//
//                            updater.getPo().setUrl(cleanUrl);
//                            updater.getPo().setSourceSid(sourceSid);
//                            updater.getPo().setTitleUpdateTime(DEFAUTL_UPDATETIME);
//
//                            dbm.update(updater);
//                        }
//                    }
//                } else {
//                    PtmCmpSku cmpSku = cmpSkuService.createCmpSkuForIndex(sku);
//
//                    PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(cmpSku.getId());
//
//                    updater.getPo().setTitleUpdateTime(DEFAUTL_UPDATETIME);
//
//                    dbm.update(updater);
//                    logger.debug("create cmpsku success website:" + sku.getWebsite() + ";id:" + cmpSku.getId());
//                }
//            }
        }
    }
}
