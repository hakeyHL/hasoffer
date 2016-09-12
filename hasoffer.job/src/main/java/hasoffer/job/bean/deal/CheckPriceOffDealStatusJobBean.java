package hasoffer.job.bean.deal;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/9/8.
 */
public class CheckPriceOffDealStatusJobBean extends QuartzJobBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(CheckPriceOffDealStatusJobBean.class);
    private static final String Q_PRICEOFF_DEAL = "SELECT t From AppDeal t WHERE t.appdealSource = 'PRICE_OFF' AND t.expireTime > ?0 ORDER BY t.createTime DESC";
    private static int PRICEOFF_DEAL_LIST_THREAD_NUM = 1;

    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    private IDealService dealService;
    @Resource
    private IDataBaseManager dbm;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        logger.info("CheckPriceOffDealStatusJobBean is run at {}", new Date());

        ExecutorService es = Executors.newCachedThreadPool();

        final ConcurrentLinkedQueue<AppDeal> priceOffDealQueue = new ConcurrentLinkedQueue<>();

        es.execute(new Runnable() {
            @Override
            public void run() {

                try {


                    int curPage = 1;
                    int pageSize = 1000;

                    PageableResult<AppDeal> pageableResult = dbm.queryPage(Q_PRICEOFF_DEAL, curPage, pageSize, Arrays.asList(TimeUtils.nowDate()));

                    long totalPage = pageableResult.getTotalPage();

                    while (curPage <= totalPage) {

                        if (priceOffDealQueue.size() > 1000) {
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {

                            }
                            continue;
                        }

                        if (curPage > 1) {
                            pageableResult = dbm.queryPage(Q_PRICEOFF_DEAL, curPage, pageSize, Arrays.asList(TimeUtils.nowDate()));
                        }

                        List<AppDeal> dealList = pageableResult.getData();

                        for (AppDeal deal : dealList) {

                            long ptmcmpskuid = deal.getPtmcmpskuid();
                            PtmCmpSku ptmCmpSku = dbm.get(PtmCmpSku.class, ptmcmpskuid);
                            Website website = ptmCmpSku.getWebsite();
                            String url = ptmCmpSku.getUrl();
                            fetchDubboService.sendUrlTask(website, url, TimeUtils.SECONDS_OF_1_MINUTE * 45, TaskLevel.LEVEL_2);

                            priceOffDealQueue.add(deal);

                        }

                        curPage++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    CheckPriceOffDealStatusJobBean.PRICEOFF_DEAL_LIST_THREAD_NUM--;
                }
            }
        });

        for (int i = 0; i < 10; i++) {

            es.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        AppDeal deal = priceOffDealQueue.poll();

                        if (deal == null) {
                            System.out.println("CheckPriceOffDealStatusJobBean poll get null sleep 5 seconds");

                            if (CheckPriceOffDealStatusJobBean.PRICEOFF_DEAL_LIST_THREAD_NUM == 0 && priceOffDealQueue.size() == 0) {
                                System.out.println("CheckPriceOffDealStatusJobBean list thread is die and queue size is 0 ,process thread go die");
                                break;
                            }
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {

                            }
                            continue;
                        }

                        PtmCmpSku sku = dbm.get(PtmCmpSku.class, deal.getPtmcmpskuid());

                        if (sku == null) {
                            continue;
                        }

                        // try update sku
                        Long skuid = sku.getId();
                        String url = sku.getUrl();
                        Website website = WebsiteHelper.getWebSite(url);

                        if (website == null) {
                            return;
                        }

                        TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(website, url, TimeUtils.SECONDS_OF_1_MINUTE * 45);

                        FetchUrlResult fetchUrlResult = null;

                        //如果返回结果状态为running，那么将sku返回队列
                        if (TaskStatus.RUNNING.equals(taskStatus) || TaskStatus.START.equals(taskStatus)) {
                            priceOffDealQueue.add(deal);
//            logger.info("taskstatus RUNNING for [" + skuid + "]");
                        } else if (TaskStatus.STOPPED.equals(taskStatus)) {
                            logger.info("taskstatus STOPPED for [" + skuid + "]");
                        } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
                            logger.info("taskstatus EXCEPTION for [" + skuid + "]");
                        } else if (TaskStatus.NONE.equals(taskStatus)) {
                            priceOffDealQueue.add(deal);
                            if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website) || Website.AMAZON.equals(website)) {
                                priceOffDealQueue.add(deal);
                                fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TimeUtils.SECONDS_OF_1_MINUTE * 45, TaskLevel.LEVEL_2);
                            } else {
                                priceOffDealQueue.add(deal);
                                fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TimeUtils.SECONDS_OF_1_MINUTE * 45, TaskLevel.LEVEL_5);
                            }
                            logger.info("taskstatus NONE for [" + skuid + "] , resend success");
                        } else {//(TaskStatus.FINISH.equals(taskStatus)))
                            logger.info("taskstatus FINISH for [" + skuid + "]");
                            fetchUrlResult = fetchDubboService.getProductsByUrl(sku.getWebsite(), sku.getUrl(), TimeUtils.SECONDS_OF_1_MINUTE * 45);

                            FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

                            System.out.println(JSONUtil.toJSON(fetchedProduct).toString() + "id=" + skuid);

                            float newPrice = fetchedProduct.getPrice();

                            if (newPrice > sku.getPrice()) {
                                dealService.deleteDeal(deal.getId());
                            }
                        }
                    }
                }
            });

        }

        if (logger.isDebugEnabled()) {
            logger.debug("executeInternal(CheckPriceOffDealStatusJobBean context={}) - end", context);
        }
    }
}

