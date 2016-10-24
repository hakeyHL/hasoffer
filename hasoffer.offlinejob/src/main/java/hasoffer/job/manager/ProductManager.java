package hasoffer.job.manager;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by chevy on 2016/10/21.
 */
@Component
public class ProductManager {

    @Resource
    IDataBaseManager dbm;

    @DataSource(value = DataSourceType.Slave)
    public void loadImageDownLoadTasks(LinkedBlockingQueue<PtmCmpSku> cmpSkusQueue) {
        final String Q_SKU_IMAGE =
                "SELECT t FROM PtmCmpSku t WHERE t.imagePath IS NULL";

        final int PAGE_SIZE = 1000;

        PageableResult<PtmCmpSku> pSkus = dbm.queryPage(Q_SKU_IMAGE, 1, PAGE_SIZE);

        long totalPage = pSkus.getTotalPage();
        long page = totalPage;

        while (page >= 1) {
            List<PtmCmpSku> skus = null;
            if (page == 1) {
                skus = pSkus.getData();
            } else {
                skus = dbm.query(Q_SKU_IMAGE, (int) page, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(skus)) {
                for (PtmCmpSku sku : skus) {
                    if (StringUtils.isEmpty(sku.getOriImageUrl())) {
                        continue;
                    }
                    if (sku.isFailLoadImage()) {
                        continue;
                    }

                    cmpSkusQueue.add(sku);
                }
            }

            page--;
        }
    }

}
