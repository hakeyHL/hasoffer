package hasoffer.task.timedtask;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.enums.TaskTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2016/5/13.
 */
@Component
public class PriceOffNoticeListWorker {

    private static final String Q_PRICEOFFNOTICE = "SELECT distinct t.skuid FROM PriceOffNotice t ORDER BY t.skuid ASC";
    private static Logger logger = LoggerFactory.getLogger(PriceOffNoticeListWorker.class);
    @Resource
    IDataBaseManager dbm;
    @Resource
    IFetchDubboService fetchDubboService;

    @Scheduled(cron = "0 0 12 * * ?")
    public void sendPriceOffNoticeRequest() {

        int page = 1;
        int pageSize = 1000;

        PageableResult<Long> pageableResult = dbm.queryPage(Q_PRICEOFFNOTICE, page, pageSize);

        long totalPage = pageableResult.getTotalPage();
        logger.info("totalPage :" + totalPage);

        while (page <= totalPage) {

            if (page > 1) {
                pageableResult = dbm.queryPage(Q_PRICEOFFNOTICE, page, pageSize);
            }

            List<Long> dataList = pageableResult.getData();

            if (ArrayUtils.hasObjs(dataList)) {

                for (Long skuid : dataList) {

                    Website website = null;
                    String url = "";

                    if (skuid > ConstantUtil.API_ONE_BILLION_NUMBER) {
                        PtmStdPrice ptmStdPrice = dbm.get(PtmStdPrice.class, skuid);

                        if (ptmStdPrice == null) {
                            continue;
                        }
                        website = ptmStdPrice.getWebsite();
                        url = ptmStdPrice.getUrl();
                    } else {
                        PtmCmpSku sku = dbm.get(PtmCmpSku.class, skuid);

                        if (sku == null) {
                            System.out.println("notice sku is null");
                            continue;
                        }
                        website = sku.getWebsite();
                        url = sku.getUrl();
                    }

                    //暂时过滤掉myntra
                    if (Website.MYNTRA.equals(website)) {
                        continue;
                    }

                    //过滤掉snapdeal中viewAllSeller的情况
                    if (Website.SNAPDEAL.equals(website)) {
                        url = StringUtils.filterAndTrim(url, Arrays.asList("/viewAllSellers"));
                    }
                    //过滤掉amazon中gp/offer-listing的url,该url没有描述等信息
                    if (Website.AMAZON.equals(website)) {
                        url = url.replace("gp/offer-listing", "dp");
                    }

                    fetchDubboService.sendUrlTask(website, url, TaskTarget.PRICEOFF_NOTICE, TaskLevel.LEVEL_2, skuid);

                    logger.info("send url request succes for " + website + " sku id is [" + skuid + "]");
                }
            }

            page++;
        }

        logger.info("send url finish");
    }
}
