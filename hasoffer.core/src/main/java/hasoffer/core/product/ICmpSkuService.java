package hasoffer.core.product;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.core.bo.product.SkuPriceUpdateResultBo;
import hasoffer.core.persistence.mongo.PtmCmpSkuLog;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSku2;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuIndex2;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.stat.StatSkuPriceUpdateResult;
import hasoffer.fetch.model.OriFetchedProduct;
import hasoffer.spider.model.FetchedProduct;

import java.util.List;

/**
 * Created on 2016/1/4.
 */
public interface ICmpSkuService {

    void createDescription(PtmCmpSku ptmCmpSku, FetchedProduct fetchedProduct);

    void createPtmCmpSkuImage(long skuId, FetchedProduct fetchedProduct);

    PtmCmpSku2 createCmpSkuForIndex(PtmCmpSku2 ptmCmpSku);

    void createPtmCmpSkuIndexToMysql(PtmCmpSku2 ptmCmpsku);

    void updateCmpSkuByOriFetchedProduct(long skuId, OriFetchedProduct oriFetchedProduct);

    void updateCmpSkuBySpiderFetchedProduct(long skuId, FetchedProduct fetchedProduct);

    void updateCmpSku(long id, String url, String color, String size, float price);

    void updateCmpSku(long id, String url, float price, SkuStatus skuStatus);

    void updateCmpSku(long id, SkuStatus skuStatus, String skuTitle, float price, String imageUrl, String url, String deeplink);

    // 某 cmpsku 所有的历史价格
    List<PtmCmpSkuLog> listByPcsId(long pcsId);

    SkuPriceUpdateResultBo countUpdate(String ymd);

    void saveOrUpdateSkuPriceUpdateResult(SkuPriceUpdateResultBo updateResultBo);

    List<StatSkuPriceUpdateResult> listUpdateResults();

    PtmCmpSku createCmpSku(long productId, String url, String color, String size, float price);

    PtmCmpSku createCmpSku(PtmCmpSku ptmCmpSku);

    void deleteCmpSku(long id);

    PtmCmpSku getCmpSkuById(long id);

    List<PtmCmpSku> listCmpSkus(long productId);

    void updateCmpSkuPrice(Long id, float price);

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

    void updateFlipakrtSkuBrandAndModel(long skuid, String brand, String model);
}
