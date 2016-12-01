package hasoffer.core.product.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.core.cache.CategoryCacheManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.IPtmStdSkuService;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.search.ISearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by hs on 2016年11月28日.
 * Time 17:21
 */
@Service
public class PtmStdSKuServiceImpl implements IPtmStdSkuService {
    private static final String SOLR_GET_PTMSTDSKU_BY_MINID = " select t from PtmStdSku t where id >= ?0";
    private static final String API_GET_PTMSTDSKU_BY_SKUID = " select t from PtmStdSku t where id = ?0 and t.";
    Logger logger = LoggerFactory.getLogger(PtmStdSKuServiceImpl.class);
    @Resource
    CategoryCacheManager categoryCacheManager;
    @Resource
    IPtmStdPriceService iPtmStdPriceService;
    @Resource
    ISearchService searchService;
    @Resource
    private IDataBaseManager dbm;
    @Resource
    private PtmStdSkuIndexServiceImpl ptmStdSkuIndexServicel;

    @Override
    public PtmStdSku getStdSkuById(Long id) {
        return dbm.get(PtmStdSku.class, id);
    }

    @Override
    public PageableResult<PtmStdSku> getPtmStdSkuListByMinId(Long minId, int page, int pageSize) {
        return dbm.queryPage(SOLR_GET_PTMSTDSKU_BY_MINID, page, pageSize, Arrays.asList(minId));
    }

    @Override
    public void importPtmStdSku2Solr(PtmStdSku ptmStdSku) {
        //导入sku(product)到solr
        if (ptmStdSku == null) {
            return;
        }
        PtmStdSku ptmStdSku1 = dbm.get(PtmStdSku.class, ptmStdSku.getId());
        if (ptmStdSku1 == null) {
            //delete it from solr ,if it exist .
            ptmStdSkuIndexServicel.remove(ptmStdSku.getId() + "");
            return;
        }
        PtmStdSkuModel ptmStdSKuModel = getPtmStdSKuModel(ptmStdSku1);
        if (ptmStdSKuModel == null) {
            ptmStdSkuIndexServicel.remove(ptmStdSku.getId() + "");
        } else {
            ptmStdSkuIndexServicel.createOrUpdate(ptmStdSKuModel);
        }
    }

    public PtmStdSkuModel getPtmStdSKuModel(PtmStdSku ptmStdSku1) {

        PtmStdSkuModel ptmStdSkuModel = new PtmStdSkuModel(ptmStdSku1);
        //  递归获取类目树类目
        List<PtmCategory> routerCategoryList = categoryCacheManager.getRouterCategoryList(ptmStdSku1.getCategoryId());
        setCategoryList(ptmStdSkuModel, routerCategoryList);
        //  price 列表
        List<PtmStdPrice> priceList = iPtmStdPriceService.getPtmStdPriceList(ptmStdSku1.getId(), SkuStatus.ONSALE);
        //符合条件的sku筛选
        Set<Website> websiteSet = new HashSet<>();
        //最低价,高价
        if (priceList != null && priceList.size() > 0) {
            Iterator<PtmStdPrice> iterator = priceList.iterator();
            while (iterator.hasNext()) {
                PtmStdPrice next = iterator.next();
                if (next.getPrice() <= 0) {
                    iterator.remove();
                }
                if (next.getWebsite() != null) {
                    websiteSet.add(next.getWebsite());
                }
            }
        } else {
            return null;
        }
        if (websiteSet.size() < 1) {
            System.out.println("site size <1");
            return null;
        }
        //按价格排序
        Collections.sort(priceList, new Comparator<PtmStdPrice>() {
            @Override
            public int compare(PtmStdPrice o1, PtmStdPrice o2) {
                if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        });
        float minPrice = priceList.get(0).getPrice();
        float maxPrice = priceList.get(priceList.size() - 1).getPrice();
        SrmProductSearchCount searchCount = searchService.findSearchCountByProductId(ptmStdSku1.getId());
        ptmStdSkuModel.setMinPrice(minPrice);
        ptmStdSkuModel.setMaxPrice(maxPrice);
        ptmStdSkuModel.setSearchCount(searchCount == null ? 0 : searchCount.getCount());
        ptmStdSkuModel.setStoreCount(websiteSet.size());
        return ptmStdSkuModel;
    }

    private void setCategoryList(PtmStdSkuModel ptmStdSkuModel, List<PtmCategory> routerCategoryList) {
        if (routerCategoryList != null && routerCategoryList.size() > 0) {
            //cate1
            ptmStdSkuModel.setCate1(routerCategoryList.get(0).getId());
            ptmStdSkuModel.setCate1Name(routerCategoryList.get(0).getName());
            if (routerCategoryList.size() > 1) {
                ptmStdSkuModel.setCate2(routerCategoryList.get(1).getId());
                ptmStdSkuModel.setCate2Name(routerCategoryList.get(1).getName());
            }
            if (routerCategoryList.size() > 2) {
                ptmStdSkuModel.setCate3(routerCategoryList.get(2).getId());
                ptmStdSkuModel.setCate3Name(routerCategoryList.get(2).getName());
            }
        }
    }

}
