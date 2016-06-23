package hasoffer.timer.product;

import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
            "SELECT t FROM PtmCmpSku t WHERE t.imagePath IS NULL AND t.oriImageUrl IS NOT NULL AND t.failLoadImage = 0";
//            "SELECT t FROM PtmCmpSku t ORDER BY t.id ASC ";

    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IDataBaseManager dbm;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void f() {
        final int page = 1, PAGE_SIZE = 500;

        while (true) {
            List<PtmCmpSku> skus = dbm.query(Q_SKU_IMAGE, page, PAGE_SIZE);

            if (ArrayUtils.hasObjs(skus)) {
                for (PtmCmpSku sku : skus) {
                    String oriImageUrl = sku.getOriImageUrl();
                    String imagePath = sku.getImagePath();

                    if (!StringUtils.isEmpty(imagePath) || StringUtils.isEmpty(oriImageUrl)) {
                        continue;
                    }

                    cmpSkuService.downloadImage2(sku);
                }
            } else {
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

}
