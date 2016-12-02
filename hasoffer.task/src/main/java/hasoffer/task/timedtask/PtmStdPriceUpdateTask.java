package hasoffer.task.timedtask;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
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
 * Created on 2016/11/30.
 */
@Component
public class PtmStdPriceUpdateTask {

    private static Logger logger = LoggerFactory.getLogger(PtmStdPriceUpdateTask.class);

    @Resource
    IDataBaseManager dbm;
    @Resource
    IFetchDubboService fetchDubboService;

    @Scheduled(cron = "0 0 0/2 * * ?")
    public void updatePtmStdPrice() {

        int curPage = 1;
        int pageSize = 1000;

        PageableResult<PtmStdPrice> pageableResult = dbm.queryPage("SELECT t FROM PtmStdPrice t ORDER BY t.id ", curPage, pageSize);

        long totalPage = pageableResult.getTotalPage();

        for (; curPage <= totalPage; curPage++) {

            if (curPage > 1) {
                pageableResult = dbm.queryPage("SELECT t FROM PtmStdPrice t ORDER BY t.id ", curPage, pageSize);
            }

            List<PtmStdPrice> ptmStdPriceList = pageableResult.getData();

            if (ptmStdPriceList != null && ptmStdPriceList.size() > 0) {

                for (PtmStdPrice ptmStdPrice : ptmStdPriceList) {

                    //offsale的不再更新
                    if (SkuStatus.OFFSALE.equals(ptmStdPrice.getSkuStatus())) {
                        continue;
                    }

                    Website website = ptmStdPrice.getWebsite();

                    //暂时过滤掉myntra
                    if (Website.MYNTRA.equals(website)) {
                        continue;
                    }

                    //高优先级的网站
                    if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website) || Website.AMAZON.equals(website)) {

                        //过滤掉snapdeal中viewAllSeller的情况
                        if (Website.SNAPDEAL.equals(website)) {
                            String url = ptmStdPrice.getUrl();
                            url = StringUtils.filterAndTrim(url, Arrays.asList("/viewAllSellers"));
                            ptmStdPrice.setUrl(url);
                        }
                        //过滤掉amazon中gp/offer-listing的url,该url没有描述等信息
                        if (Website.AMAZON.equals(website)) {
                            String url = ptmStdPrice.getUrl();
                            url = url.replace("gp/offer-listing", "dp");
                            ptmStdPrice.setUrl(url);
                        }

                        fetchDubboService.sendUrlTask(ptmStdPrice.getWebsite(), ptmStdPrice.getUrl(), TaskTarget.STDPRICE_UPDATE, TaskLevel.LEVEL_3);
                        System.out.println("send stdprice request succes for " + ptmStdPrice.getWebsite() + " sku id is _" + ptmStdPrice.getId() + "_");
                    } else if (Website.EBAY.equals(website) || Website.PAYTM.equals(website) || Website.SHOPCLUES.equals(website) || Website.INFIBEAM.equals(website)) {
                        fetchDubboService.sendUrlTask(ptmStdPrice.getWebsite(), ptmStdPrice.getUrl(), TaskTarget.STDPRICE_UPDATE, TaskLevel.LEVEL_5);
                        System.out.println("send stdprice request succes for " + ptmStdPrice.getWebsite() + " sku id is _" + ptmStdPrice.getId() + "_");
                    } else {
                        continue;
                    }

                }
            }
        }
    }
}
