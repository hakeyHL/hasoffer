package hasoffer.core.product;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.core.bo.product.SkuPriceUpdateResultBo;
import hasoffer.core.bo.product.SkuUpdateResult;
import hasoffer.core.persistence.mongo.PriceNode;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSku2;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuIndex2;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.stat.StatSkuPriceUpdateResult;
import hasoffer.core.persistence.po.stat.StatSkuUpdateResult;
import hasoffer.fetch.model.OriFetchedProduct;
import hasoffer.spider.model.FetchedProduct;

import java.util.Date;
import java.util.List;

/**
 * Created on 2016/1/4.
 */
public interface ICmpSkuService {

    /**
     * 该方法用来根据sourceSid和website在数据库中准确定位一个或者多个符合条件的sku信息
     *
     * @param sourceSid
     * @param website
     * @param page
     * @param pageSize
     * @return
     */
    List<PtmCmpSku> getPtmCmpSkuListBySourceSidAndWebsite(String sourceSid, Website website, int page, int pageSize);

    List<PtmCmpSku> getPtmCmpSkuListByUrlKey(String keyUrl);

    /**
     * 该方法用于修复sku表中urlKey，不建议在赋值时使用，赋值参见该类的setUrl()方法
     * @param skuId
     * @param newUrl
     * @param urlKey
     */
    void setUrlKey(long skuId, String newUrl, String urlKey);

    List<PriceNode> queryHistoryPrice(long id);

    void saveHistoryPrice(long id, Date time, float price);

    void saveHistoryPrice(Long sid, List<PriceNode> priceNodes);

//    void createDescription(PtmCmpSku ptmCmpSku, FetchedProduct fetchedProduct);

    void createSkuDescription(PtmCmpSku ptmCmpSku, FetchedProduct fetchedProduct);

    void createProductDescription(PtmCmpSku ptmCmpSku, FetchedProduct fetchedProduct);

    void createPtmCmpSkuImage(long skuId, FetchedProduct fetchedProduct);

    PtmCmpSku2 createCmpSkuForIndex(PtmCmpSku2 ptmCmpSku);

    void createPtmCmpSkuIndexToMysql(PtmCmpSku2 ptmCmpsku);

    void updateCmpSkuByOriFetchedProduct(long skuId, OriFetchedProduct oriFetchedProduct);

    /**
     * SKU更新，通过spider 更新数据。
     *
     * @param skuId
     * @param fetchedProduct
     */
    void updateCmpSkuBySpiderFetchedProduct(long skuId, FetchedProduct fetchedProduct);

    void updateCmpSku(long id, String url, String color, String size, float price, String skuStatus,
                      String title,
                      String imageUrl);
    void updateCmpSku(long id, String url, String color, String size, float price);

    SkuPriceUpdateResultBo countUpdate(String ymd);

    void saveOrUpdateSkuPriceUpdateResult(SkuPriceUpdateResultBo updateResultBo);

    List<StatSkuPriceUpdateResult> listUpdateResults();

    PtmCmpSku createCmpSku(long productId, String url, String color, String size, float price, String skuStatus, String title, String imageUrl);
    PtmCmpSku createCmpSku(long productId, String url, String color, String size, float price);

    PtmCmpSku createCmpSku(PtmCmpSku ptmCmpSku);

    void deleteCmpSku(long id);

    PtmCmpSku getCmpSkuById(long id);

    List<PtmCmpSku> getCmpSkusBySiteAndPrice(float price, Website website, Long productId);

    List<PtmCmpSku> listCmpSkus(long productId);

    void downloadImage(PtmCmpSku sku);

    /**
     * 区别 downloadImage 方法：保存图片的原图、大图、小图路径
     *
     * @param sku
     */
    void downloadImage2(PtmCmpSku sku);

    PtmCmpSku getCmpSku(long id, Website website);

    PtmCmpSku getCmpSku(String q);

    void updateCmpSku(Long id, String newTitle);

    void fixUrls(long id, String url, String dl);

    void updateCmpSku(PtmCmpSkuUpdater updater);

    PtmCmpSkuIndex2 getCmpSkuIndex2(Website cliSite, String sourceId, String keyword);

    int getSkuSoldStoreNum(Long id);

    List<PtmCmpSku> listCmpSkus(long productId, SkuStatus onsale);

    void updateCategoryid(long ptmcmpskuid, long categoryid);

    void updateCategoryid2(Long ptmcmpskuid, long categoryid2);

    void importCmpSku2solr(PtmCmpSku ptmCmpSku);

    void importCmpSku2solrByProductId(Long proId);

    void batchDeleteCmpSku(Long[] ids);

    /**
     * 用来修复小图的问题
     *
     * @param skuid
     * @param smallImagePath
     */
    void fixSmallImagePath(long skuid, String smallImagePath);

    void fixFlipkartSkuTitleNull(long skuid, String skutitle);

    void updateFlipakrtSkuBrandAndModel(long skuid, String brand, String model);

    void updateCmpSkuBrandModel(Long id, String brand, String model);

    void saveSkuUpdateResult(SkuUpdateResult skuUpdateResult);

    List<StatSkuUpdateResult> getSkuUpdateResults();

    PageableResult<PtmCmpSku> listCmpSkus(int page, int size);


}
