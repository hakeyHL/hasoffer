package hasoffer.task.timedtask;

import hasoffer.base.utils.TimeUtils;
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
import java.util.List;
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

        long startTime = TimeUtils.now();

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> queue = new ConcurrentLinkedQueue<>();

        es.execute(new SrmProductSearchCountListWorker(dbm, queue, fetchDubboService, redisListService));

        //保证list任务优先执行
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 60; i++) {
            es.execute(new CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, cmpSkuService, redisListService));
        }

        while (true) {
            //如果当前线程已经运行超过23小时，自杀吧孩子
            if (TimeUtils.now() - startTime > TimeUtils.MILLISECONDS_OF_1_HOUR * 23) {
                System.out.println("dubbo update executorService has live above 10 hours ,thread going to die");
                es.shutdown();
                break;
            } else {
                try {
                    TimeUnit.MINUTES.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        List<Runnable> runnables = es.shutdownNow();
        if (runnables != null) {
            System.out.println(runnables.size());
        }

    }

    /**
     * price off notice
     *
     * @return
     */
    @Scheduled(cron = "00 00 11 * * ?")
    public void priceOffNotieUpdatestart() {

        long startTime = TimeUtils.now();

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

        while (true) {
            //如果当前线程已经运行超过3小时，自杀吧孩子
            if (TimeUtils.now() - startTime > TimeUtils.MILLISECONDS_OF_1_HOUR * 3) {
                System.out.println("price off notice update executorService has live above 3 hours ,thread going to die");
                es.shutdown();
                break;
            } else {
                try {
                    TimeUnit.MINUTES.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        List<Runnable> runnables = es.shutdownNow();
        if (runnables != null) {
            System.out.println(runnables.size());
        }
    }

}
