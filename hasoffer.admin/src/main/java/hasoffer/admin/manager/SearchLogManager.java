package hasoffer.admin.manager;

import hasoffer.admin.common.CategoryHelper;
import hasoffer.admin.controller.vo.SearchLogVo;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.search.ISearchService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Date : 2016/4/20
 * Function :
 */
@Component
public class SearchLogManager {

    @Resource
    IDataBaseManager dbm;

    @Resource
    ICategoryService categoryService;

    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    ISearchService searchService;

    public List<SearchLogVo> getSearchLogs(List<SrmSearchLog> logs) {
        List<SearchLogVo> searchLogVos = new ArrayList<SearchLogVo>();

        if (ArrayUtils.hasObjs(logs)) {
            for (SrmSearchLog srmSearchLog : logs) {
                String title = "";
                long proId = srmSearchLog.getPtmProductId();

                int skuCount = 0;
                float min = 0, max = 0;

                if (proId > 0) {
                    PtmProduct ptmProduct = dbm.get(PtmProduct.class, srmSearchLog.getPtmProductId());
                    if (ptmProduct != null) {
                        title = ptmProduct.getTitle();
                    }

                    List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(proId);

                    skuCount = cmpSkus.size();

                    for (PtmCmpSku cmpSku : cmpSkus) {
                        float price = cmpSku.getPrice();

                        if (min <= 0 || price < min) {
                            min = price;
                        }
                        if (max <= 0 || price > max) {
                            max = price;
                        }
                    }
                }

                List<PtmCategory> categories = categoryService.getRouterCategoryList(srmSearchLog.getCategory());

                searchLogVos.add(
                        new SearchLogVo(
                                srmSearchLog,
                                title,
                                CategoryHelper.getCategoryVos(categories),
                                min,
                                max,
                                skuCount)
                );
            }
        }

        return searchLogVos;
    }

    public void saveSearchCount(String ymd) {
        Map<Long, Long> countMap = searchLogCacheManager.getProductCount(ymd);

        List<SrmProductSearchCount> spscs = new ArrayList<SrmProductSearchCount>();

        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {
            long productId = countKv.getKey();

            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId, SkuStatus.ONSALE);
            int size = 0;
            if (ArrayUtils.hasObjs(cmpSkus)) {
                size = cmpSkus.size();
            }

            spscs.add(new SrmProductSearchCount(ymd, productId, countKv.getValue(), size));
        }

        /*Collections.sort(spsc, new Comparator<SrmProductSearchCount>() {
            @Override
            public int compare(SrmProductSearchCount o1, SrmProductSearchCount o2) {
                if (o1.getCount() > o2.getCount()) {
                    return -1;
                } else if (o1.getCount() < o2.getCount()) {
                    return 1;
                }
                return 0;
            }
        });*/

        searchService.saveLogCount(spscs);
    }
}
