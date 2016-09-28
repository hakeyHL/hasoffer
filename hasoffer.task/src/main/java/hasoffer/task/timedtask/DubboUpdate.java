package hasoffer.task.timedtask;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.task.worker.CmpSkuDubboUpdateWorker;
import hasoffer.task.worker.PriceOffNoticeListWorker;
import hasoffer.task.worker.PriceOffNoticeProcessorWorker;
import hasoffer.task.worker.SrmProductSearchCountListWorker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/9/24.
 */
@Component
public class DubboUpdate {

    @Resource
    @Qualifier("fetchDubboService")
    IFetchDubboService fetchDubboService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IRedisListService redisListService;

    /**
     * sku的日常更新（目前策略热搜）
     *
     * @return
     */
    @Scheduled(cron = "00 30 14 * * ?")
    public void updatestart() {

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> queue = new ConcurrentLinkedQueue<>();

        es.execute(new SrmProductSearchCountListWorker(dbm, queue, fetchDubboService));

        //保证list任务优先执行
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 60; i++) {
            es.execute(new CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, cmpSkuService, redisListService));
        }

    }

    /**
     * sku的日常更新（目前策略热搜）
     *
     * @return
     */
    @Scheduled(cron = "00 30 11 * * ?")
    public void priceOffNotieUpdatestart() {

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> queue = new ConcurrentLinkedQueue<>();

        es.execute(new PriceOffNoticeListWorker(dbm, queue, fetchDubboService));

        //保证list任务优先执行
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            es.execute(new PriceOffNoticeProcessorWorker(queue, fetchDubboService, redisListService, cmpSkuService, dbm));
        }

    }

}
