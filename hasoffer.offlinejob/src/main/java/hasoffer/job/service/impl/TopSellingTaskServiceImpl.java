package hasoffer.job.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.core.bo.enums.TopSellStatus;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.job.dto.TopSellingTaskDTO;
import hasoffer.job.service.ITopSellingTaskService;
import hasoffer.spider.enums.TaskTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("topSellingTaskService")
public class TopSellingTaskServiceImpl implements ITopSellingTaskService {

    @Resource
    IDataBaseManager dbm;
    @Resource(name = "fetchTimerDubboService")
    IFetchDubboService fetchDubboService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IPriceOffNoticeService priceOffNoticeService;
    private Logger logger = LoggerFactory.getLogger(TopSellingTaskServiceImpl.class);

    @Override
    public void commitTask() {
        String hql = "select new hasoffer.job.dto.TopSellingTaskDTO(p.id,p.productId,p.website,p.url,p.updateTime) from PtmTopSelling s , PtmCmpSku p  where  p.productId = s.id and s.status='"
                + TopSellStatus.ONLINE.toString() + "' and p.id is not null order by s.count desc";

        ExecutorService service = Executors.newCachedThreadPool();

        for (int i = 1; ; i++) {
            List<TopSellingTaskDTO> page = dbm.query(hql, i, 2000);
            logger.debug("top selling size=" + page.size());
            if (page.isEmpty()) {
                break;
            }
            for (TopSellingTaskDTO ptmCmpSku : page) {
                fetchDubboService.sendUrlTask(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), TaskTarget.TOP_SELLING_UPDATE, TaskLevel.LEVEL_3);
                logger.debug("commit topSelling task:" + ptmCmpSku.toString());
            }
            // 获取结果
            service.execute(new TopSellingTaskWorker(page, fetchDubboService, cmpSkuService, priceOffNoticeService));
        }
        service.shutdown();
        while (true) {
            if (service.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
