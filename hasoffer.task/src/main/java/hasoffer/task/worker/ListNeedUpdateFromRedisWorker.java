package hasoffer.task.worker;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/1.
 */
public class ListNeedUpdateFromRedisWorker implements Runnable {

    private static final String UPDATE_WAIT_QUEUE = "PRODUCT_WAIT_4_UPDATE_";
    private static Logger logger = LoggerFactory.getLogger(ListNeedUpdateFromRedisWorker.class);
    private ConcurrentLinkedQueue<PtmCmpSku> queue;
    private IFetchDubboService fetchDubboService;
    private IRedisListService redisListService;
    private ICmpSkuService cmpSkuService;
    private long cacheSeconds;

    public ListNeedUpdateFromRedisWorker(ConcurrentLinkedQueue<PtmCmpSku> queue, IFetchDubboService fetchDubboService, IRedisListService redisListService, ICmpSkuService cmpSkuService, long cacheSeconds) {
        this.queue = queue;
        this.fetchDubboService = fetchDubboService;
        this.redisListService = redisListService;
        this.cmpSkuService = cmpSkuService;
        this.cacheSeconds = cacheSeconds;
    }

    @Override
    public void run() {

        String ymd = TimeUtils.parse(TimeUtils.today(), TimeUtils.PATTERN_YMD);

        while (true) {

            //判断队列大小
            int size = queue.size();
            if (size > 50000) {
                logger.info("queue size " + size + " sleep 5 minutes");
                try {
                    TimeUnit.MINUTES.sleep(5);
                } catch (InterruptedException e) {

                }
                continue;
            } else if (size == 0) {
                ymd = TimeUtils.parse(TimeUtils.today(), TimeUtils.PATTERN_YMD);
            }

            //队列取数
            Object pop = redisListService.pop(UPDATE_WAIT_QUEUE + ymd);
            if (pop == null) {//如果队列没有数据了，休息30分钟
                try {
                    TimeUnit.MINUTES.sleep(30);
                } catch (InterruptedException e) {

                }
                continue;
            }

            //根据商品id，发起更新任务
            Long productId = Long.valueOf((String) pop);

            List<PtmCmpSku> ptmCmpSkuList = cmpSkuService.listCmpSkus(productId);

            //在加入队列的时候进行一些必要的判断
            if (ptmCmpSkuList != null && ptmCmpSkuList.size() > 0) {

                for (PtmCmpSku sku : ptmCmpSkuList) {

                    //offsale的不再更新
                    if (SkuStatus.OFFSALE.equals(sku.getStatus())) {
                        continue;
                    }

                    Website website = sku.getWebsite();

                    //暂时过滤掉myntra
                    if (Website.MYNTRA.equals(website)) {
                        continue;
                    }

                    //高优先级的网站
                    if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website) || Website.AMAZON.equals(website)) {

                        //过滤掉snapdeal中viewAllSeller的情况
                        if (Website.SNAPDEAL.equals(website)) {
                            String url = sku.getUrl();
                            url = StringUtils.filterAndTrim(url, Arrays.asList("/viewAllSellers"));
                            sku.setUrl(url);
                        }
                        //过滤掉amazon中gp/offer-listing的url,该url没有描述等信息
                        if (Website.AMAZON.equals(website)) {
                            String url = sku.getUrl();
                            url = url.replace("gp/offer-listing", "dp");
                            sku.setUrl(url);
                        }

                        queue.add(sku);
                        fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), cacheSeconds, TaskLevel.LEVEL_3);
                    } else {
                        queue.add(sku);
                        fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), cacheSeconds, TaskLevel.LEVEL_5);
                    }

                    logger.info("send url request succes for " + sku.getWebsite() + " sku id is [" + sku.getId() + "]");
                }
            }
        }
    }
}
