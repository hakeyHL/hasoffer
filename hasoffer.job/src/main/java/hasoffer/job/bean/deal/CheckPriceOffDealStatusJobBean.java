package hasoffer.job.bean.deal;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    private static int PRICEOFF_DEAL_LIST_THREAD_NUM = 0;

    static {
        CheckPriceOffDealStatusJobBean.PRICEOFF_DEAL_LIST_THREAD_NUM++;
    }

    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    private IDealService dealService;
    @Resource
    private IDataBaseManager dbm;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        logger.info("CheckPriceOffDealStatusJobBean is run at {}", new Date());

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {
            @Override
            public void run() {

                try {


                    int curPage = 1;
                    int pageSize = 1000;

                    PageableResult<AppDeal> pageableResult = dbm.queryPage(Q_PRICEOFF_DEAL, curPage, pageSize, Arrays.asList(TimeUtils.nowDate()));

                    long totalPage = pageableResult.getTotalPage();
                    System.out.println("price off deal status total page =" + totalPage);

                    while (curPage <= totalPage) {

                        System.out.println("price off deal status curpage =" + curPage);

                        if (curPage > 1) {
                            pageableResult = dbm.queryPage(Q_PRICEOFF_DEAL, curPage, pageSize, Arrays.asList(TimeUtils.nowDate()));
                        }

                        List<AppDeal> dealList = pageableResult.getData();
                        System.out.println("find appdeal size =" + dealList.size());

                        for (AppDeal deal : dealList) {

                            long ptmcmpskuid = deal.getPtmcmpskuid();
                            PtmCmpSku ptmCmpSku = dbm.get(PtmCmpSku.class, ptmcmpskuid);
                            if (ptmCmpSku == null) {
                                System.out.println("get null sku,id = ptmcmpskuid");
                                continue;
                            }
                            Website website = ptmCmpSku.getWebsite();
                            String url = ptmCmpSku.getUrl();
                            fetchDubboService.sendUrlTask(website, url, TimeUtils.SECONDS_OF_1_MINUTE * 45, TaskTarget.DEAL_UPDATE, TaskLevel.LEVEL_2);

                            System.out.println("add price off deal to update queue success " + deal.getId());
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

                    long startTime = TimeUtils.now();

                    while (true) {

                        long now = TimeUtils.now();

                        //如果当前时间和启动时间相差超过50分钟，就自杀了吧少年
                        if (now - startTime > TimeUtils.MILLISECONDS_OF_1_MINUTE * 50) {
                            break;
                        }

                        String pop = fetchDubboService.popFetchUrlResult(TaskTarget.DEAL_UPDATE);

                        if (StringUtils.isEmpty(pop)) {
                            try {
                                System.out.println("pop deal update list get null sleep 5 seconds");
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {

                            }
                            continue;
                        }

                        try {

                            FetchUrlResult fetchUrlResult1 = JSONUtil.toObject(pop, FetchUrlResult.class);
                            FetchedProduct fetchedProduct = fetchUrlResult1.getFetchProduct();
                            System.out.println(JSONUtil.toJSON(fetchedProduct).toString());

                            String url = fetchUrlResult1.getUrl();
                            String urlKey = HexDigestUtil.md5(url);

                            List<PtmCmpSku> skuList = cmpSkuService.getPtmCmpSkuListByUrlKey(urlKey);

                            for (PtmCmpSku sku : skuList) {

                                // try update sku
                                Long skuid = sku.getId();
                                Website website = WebsiteHelper.getWebSite(url);

                                if (website == null) {
                                    continue;
                                }

                                float newPrice = fetchedProduct.getPrice();

                                //涨价了或者状态不是onsale失效
                                if (newPrice > sku.getPrice() || !SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
                                    AppDeal appdeal = dealService.getDealBySourceId(sku.getId());

                                    if (appdeal != null) {
                                        dealService.updateDealExpire(appdeal.getId());
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("deal update pop string parse error");
                        }
                    }
                }
            });
        }
    }
}
