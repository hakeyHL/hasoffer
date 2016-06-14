package hasoffer.timer.product;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.system.ITimerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Date : 2016/1/13
 * Function :
 */
@Component
public class SkuImageDownloadTask {

    /**
     * 取ptmimage 逻辑：未下载下来的图片，按照失败次数从小到大排
     */
    private static final String Q_SKU_IMAGE =
            "SELECT t FROM PtmCmpSku t ORDER BY t.id ASC ";

    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    ITimerService timerService;
    private Logger logger = LoggerFactory.getLogger(SkuImageDownloadTask.class);

    @Scheduled(cron = "0 0/10 * * * ?")
    public void f() {
        SysTimerTaskLog log = timerService.createTaskLog("SkuImageDownloadTask");

        int page = 1, PAGE_SIZE = 500;
        PageableResult<PtmCmpSku> pagedSkus = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);

        long totalPage = pagedSkus.getTotalPage();

        while (page <= totalPage) {
            List<PtmCmpSku> skus = null;

            if (page == 1) {
                skus = pagedSkus.getData();
            } else {
                skus = dbm.query(Q_SKU_IMAGE, page, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(skus)) {
                for (PtmCmpSku sku : skus) {
                    String oriImageUrl = sku.getOriImageUrl();
                    String imagePath = sku.getImagePath();

                    if (!StringUtils.isEmpty(imagePath) || StringUtils.isEmpty(oriImageUrl)) {
                        continue;
                    }

                    cmpSkuService.downloadImage(sku);
                }
            }

            page++;
            logger.debug(String.format("download images. page : %d", page));
        }

        timerService.updateTaskLog(log.getId(), "");
    }

}
