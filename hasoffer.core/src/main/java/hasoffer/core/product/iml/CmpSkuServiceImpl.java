package hasoffer.core.product.iml;

import hasoffer.base.exception.ImageDownloadOrUploadException;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.common.ImagePath;
import hasoffer.core.bo.product.SkuPriceUpdateResultBo;
import hasoffer.core.exception.CmpSkuUrlNotFoundException;
import hasoffer.core.exception.MultiUrlException;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuIndex;
import hasoffer.core.persistence.mongo.PtmCmpSkuIndexSearchLog;
import hasoffer.core.persistence.mongo.PtmCmpSkuLog;
import hasoffer.core.persistence.mongo.SummaryProduct;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuIndex2;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.stat.StatPtmCmpSkuUpdate;
import hasoffer.core.persistence.po.stat.StatSkuPriceUpdateResult;
import hasoffer.core.persistence.po.stat.updater.StatPtmCmpSkuUpdateUpdater;
import hasoffer.core.persistence.po.stat.updater.StatSkuPriceUpdateResultUpdater;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IFetchService;
import hasoffer.core.product.solr.CmpSkuModel;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.utils.ImageUtil;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.model.FetchedProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import hasoffer.fetch.sites.snapdeal.SnapdealHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created on 2016/1/4.
 */
@Service
public class CmpSkuServiceImpl implements ICmpSkuService {

    private static final String Q_CMPSKU_BY_PRODUCTID =
            "SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 ";
    private static final String Q_CMPSKU_BY_PRODUCTID_WEBSITE =
            "SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 AND t.website = ?1 ";
    private static final String Q_CMPSKU_BY_TITLE =
            "SELECT t FROM PtmCmpSku t WHERE t.skuTitle = ?0 ";

    private final String Q_CMPSKU_INDEX_BY_TITLEINDEX = "select t from PtmCmpSkuIndex2 t where t.siteSkuTitleIndex = ?0 ";

    private final String Q_CMPSKU_INDEX_BY_SOURCESID = "select t from PtmCmpSkuIndex2 t where t.siteSourceSidIndex = ?0 ";

    @Resource
    IFetchService fetchService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    CmpskuIndexServiceImpl cmpskuIndexService;
    private Logger logger = LoggerFactory.getLogger(CmpSkuServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCmpSku(PtmCmpSkuUpdater updater) {
        dbm.update(updater);
    }

    @Override
    public PtmCmpSkuIndex getCmpSkuIndex(String url) {
        String qIndex = HexDigestUtil.md5(url);
        Query query = new Query(Criteria.where("skuUrlIndex").is(qIndex));
        return mdm.queryOne(PtmCmpSkuIndex.class, query);
    }

    @Override
    public PtmCmpSkuIndex2 getCmpSkuIndex2(Website website, String sourceId, String cliQ) {

        List<PtmCmpSkuIndex2> cmpSkuIndexs = null;
        if (StringUtils.isEmpty(sourceId) || StringUtils.isEqual(sourceId, "0")) {
            String qIndex = HexDigestUtil.md5(website.name() + StringUtils.getCleanChars(cliQ));
            cmpSkuIndexs = dbm.query(Q_CMPSKU_INDEX_BY_TITLEINDEX, Arrays.asList(qIndex));
        } else {
            String qIndex = HexDigestUtil.md5(website.name() + sourceId);
            cmpSkuIndexs = dbm.query(Q_CMPSKU_INDEX_BY_SOURCESID, Arrays.asList(qIndex));
        }

        // todo fix bug

        PtmCmpSkuIndex2 finalIndex = null;
        if (ArrayUtils.hasObjs(cmpSkuIndexs)) {

            finalIndex = cmpSkuIndexs.get(0);

            if (StringUtils.isEmpty(finalIndex.getUrl())) {
                throw new CmpSkuUrlNotFoundException(finalIndex.getId());
            }

            if ((StringUtils.isEmpty(sourceId) || StringUtils.isEqual("0", sourceId)) && cmpSkuIndexs.size() != 1) {
                for (int i = 1; i < cmpSkuIndexs.size(); i++) {

                    PtmCmpSkuIndex2 index = cmpSkuIndexs.get(i);

                    if (!finalIndex.getUrl().equals(index.getUrl())) {
                        throw new MultiUrlException(website, sourceId, cliQ);
                    }
                }
            }
        }

        return finalIndex;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatPtmCmpSkuUpdate createStatPtmCmpSkuUpdate(StatPtmCmpSkuUpdate statPtmCmpSkuUpdate) {
        String id = dbm.create(statPtmCmpSkuUpdate);
        statPtmCmpSkuUpdate.setId(id);
        return statPtmCmpSkuUpdate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatPtmCmpSkuUpdate(String id, long onSaleAmount, long soldOutAmount, long offsaleAmount, long allAmount, long updateSuccessAmount, long alwaysFailAmount, long newSkuAmount, long indexAmount, long newIndexAmount) {

        StatPtmCmpSkuUpdateUpdater updater = new StatPtmCmpSkuUpdateUpdater(id);

        updater.getPo().setUpdateTime(TimeUtils.nowDate());
        updater.getPo().setOnSaleAmount(onSaleAmount);
        updater.getPo().setSoldOutAmount(soldOutAmount);
        updater.getPo().setOffsaleAmount(offsaleAmount);
        updater.getPo().setAllAmount(allAmount);
        updater.getPo().setUpdateSuccessAmount(updateSuccessAmount);
        updater.getPo().setAlwaysFailAmount(alwaysFailAmount);
        updater.getPo().setNewSkuAmount(newSkuAmount);
        updater.getPo().setIndexAmount(indexAmount);
        updater.getPo().setNewIndexAmount(newIndexAmount);

        dbm.update(updater);

    }

    @Override
    public PtmCmpSkuIndex getCmpSkuIndex(Website website, String sourceId, String cliQ) {
        PtmCmpSkuIndexSearchLog indexSearchLog = new PtmCmpSkuIndexSearchLog(website, sourceId, cliQ);

        String qIndex = HexDigestUtil.md5(StringUtils.getCleanChars(cliQ));

        Query query = new Query();
        if (StringUtils.isEmpty(sourceId)) {
            query.addCriteria(Criteria.where("skuTitleIndex").is(qIndex));
        } else {
            query.addCriteria(Criteria.where("sourceSid").is(sourceId));
        }

        List<PtmCmpSkuIndex> pcsis = mdm.query(PtmCmpSkuIndex.class, query);
        PtmCmpSkuIndex finalIndex = null;

        if (ArrayUtils.isNullOrEmpty(pcsis)) {
            indexSearchLog.setErrorMsg("no index.");
        } else {
            finalIndex = pcsis.get(0);

            if (StringUtils.isEmpty(sourceId) && pcsis.size() != 1) {
                if (finalIndex.getUrl() == null) {
                    // todo 这种情况是一种临时的,不跳转
                    indexSearchLog.setErrorMsg("no url.");
                } else {
                    for (int i = 1; i < pcsis.size(); i++) {
                        PtmCmpSkuIndex index = pcsis.get(i);
                        if (!finalIndex.getUrl().equals(index.getUrl())) {
                            indexSearchLog.setErrorMsg("different urls.");
                        }
                    }
                }
            }
        }

        mdm.save(indexSearchLog);

        if (indexSearchLog.isHasExcept()) {
            return null;
        }

        return finalIndex;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fixUrls(long id, String url, String dl) {
        PtmCmpSkuUpdater cmpSkuUpdater = new PtmCmpSkuUpdater(id);
        cmpSkuUpdater.getPo().setUrl(url);
        cmpSkuUpdater.getPo().setDeeplink(url);
        dbm.update(cmpSkuUpdater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCmpSku(Long id, String newTitle) {
        PtmCmpSkuUpdater cmpSkuUpdater = new PtmCmpSkuUpdater(id);
        cmpSkuUpdater.getPo().setSkuTitle(newTitle);
        dbm.update(cmpSkuUpdater);
    }

    @Override
//    @Cacheable(value = CACHE_KEY, key = "#root.methodName + '_' + #root.args[0]")
    public PtmCmpSku getCmpSku(String title) {
        List<PtmCmpSku> cmpSkus = dbm.query(Q_CMPSKU_BY_TITLE, Arrays.asList(title));
        return ArrayUtils.isNullOrEmpty(cmpSkus) ? null : cmpSkus.get(0);
    }

    @Override
    public PtmCmpSku getCmpSku(long proId, Website website) {

        List<PtmCmpSku> cmpSkus = dbm.query(Q_CMPSKU_BY_PRODUCTID_WEBSITE, Arrays.asList(proId, website));

        if (ArrayUtils.hasObjs(cmpSkus)) {
            return cmpSkus.get(0);
        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downloadImage(PtmCmpSku sku) {
        String oriImageUrl = sku.getOriImageUrl();
        if (StringUtils.isEmpty(oriImageUrl)) {
            return;
        }

        PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(sku.getId());

        try {
            String path = ImageUtil.downloadAndUpload(oriImageUrl);

            ptmCmpSkuUpdater.getPo().setImagePath(path);

            dbm.update(ptmCmpSkuUpdater);

        } catch (ImageDownloadOrUploadException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downloadImage2(PtmCmpSku sku) {
        String oriImageUrl = sku.getOriImageUrl();
        if (StringUtils.isEmpty(oriImageUrl)) {
            return;
        }

        try {
            ImagePath imagePath = ImageUtil.downloadAndUpload2(oriImageUrl);

            PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(sku.getId());

            ptmCmpSkuUpdater.getPo().setImagePath(imagePath.getOriginalPath());
            ptmCmpSkuUpdater.getPo().setSmallImagePath(imagePath.getSmallPath());
            ptmCmpSkuUpdater.getPo().setBigImagePath(imagePath.getBigPath());

            dbm.update(ptmCmpSkuUpdater);

        } catch (ImageDownloadOrUploadException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCmpSkuPrice(Long id, float price) {
        PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(id);

        ptmCmpSkuUpdater.getPo().setUpdateTime(TimeUtils.nowDate());
        ptmCmpSkuUpdater.getPo().setPrice(price);

        dbm.update(ptmCmpSkuUpdater);
    }

    @Override
    public List<PtmCmpSku> listCmpSkus(long productId) {
        return dbm.query(Q_CMPSKU_BY_PRODUCTID, Arrays.asList(productId));
    }

    @Override
    public List<StatSkuPriceUpdateResult> listUpdateResults() {
        return dbm.query("select t from StatSkuPriceUpdateResult t order by t.id desc");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateSkuPriceUpdateResult(SkuPriceUpdateResultBo updateResultBo) {

        String ymd = updateResultBo.getYmd();
        StatSkuPriceUpdateResult statSkuPriceUpdateResult = dbm.get(StatSkuPriceUpdateResult.class, ymd);

        if (statSkuPriceUpdateResult == null) {
            statSkuPriceUpdateResult = new StatSkuPriceUpdateResult(ymd, updateResultBo.getCount());
            dbm.create(statSkuPriceUpdateResult);
        } else {
            StatSkuPriceUpdateResultUpdater statSkuPriceUpdateResultUpdater = new StatSkuPriceUpdateResultUpdater(ymd);
            statSkuPriceUpdateResultUpdater.getPo().setCount(updateResultBo.getCount());
            dbm.update(statSkuPriceUpdateResultUpdater);
        }
    }

    @Override
    public SkuPriceUpdateResultBo countUpdate(String ymd) {
        Date startDate = TimeUtils.toDate(TimeUtils.getDayStart(ymd, "yyyyMMdd"));
        Date endDate = TimeUtils.toDate(TimeUtils.getDayStart(ymd, "yyyyMMdd") + TimeUtils.MILLISECONDS_OF_1_DAY);

        Query query = new Query(Criteria.where("priceTime").gte(startDate).lt(endDate));
        long count = mdm.count(PtmCmpSkuLog.class, query);

        return new SkuPriceUpdateResultBo(ymd, count);
    }

    @Override
    public List<PtmCmpSkuLog> listByPcsId(long pcsId) {

        Query query = new Query(Criteria.where("pcsId").is(pcsId));
        query.with(new Sort(Sort.Direction.ASC, "priceTime"));

        return mdm.query(PtmCmpSkuLog.class, query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCmpSku(long id) {
        dbm.delete(PtmCmpSku.class, id);
    }

    @Override
    public PtmCmpSku getCmpSkuById(long id) {
        return dbm.get(PtmCmpSku.class, id);
    }

    /**
     * 新增一条cmpSku记录
     *
     * @param productId
     * @param url
     * @param color
     * @param size
     * @param price
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtmCmpSku createCmpSku(long productId, String url, String color, String size, float price) {

        PtmCmpSku ptmCmpSku = new PtmCmpSku();

        ptmCmpSku.setUrl(url);
        ptmCmpSku.setProductId(productId);
        ptmCmpSku.setColor(color);
        ptmCmpSku.setSize(size);
        ptmCmpSku.setPrice(price);

        Website website = WebsiteHelper.getWebSite(url);
        if (website != null) {
            ptmCmpSku.setWebsite(website);
        }

        createCmpSku(ptmCmpSku);

        return ptmCmpSku;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtmCmpSku createCmpSku(PtmCmpSku ptmCmpSku) {

        //sku创建时，添加创建时间字段
        ptmCmpSku.setCreateTime(TimeUtils.nowDate());
        long skuid = dbm.create(ptmCmpSku);

        //创建sku的时候，将固定网站的商品导入到mongodb中,只导入url、website、id

        if (WebsiteHelper.DEFAULT_WEBSITES.contains(ptmCmpSku.getWebsite())) {

            SummaryProduct summaryProduct = new SummaryProduct();

            summaryProduct.setId(skuid);
            summaryProduct.setWebsite(ptmCmpSku.getWebsite());
            summaryProduct.setUrl(ptmCmpSku.getUrl());

            mdm.save(summaryProduct);
        }

//        createPtmCmpSkuIndexToMongo(ptmCmpSku);
//        createPtmCmpSkuIndexToMysql(ptmCmpSku);

        importCmpSku2solr(ptmCmpSku);

        return ptmCmpSku;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtmCmpSku createCmpSkuForIndex(PtmCmpSku ptmCmpSku) {

        //sku创建时，添加创建时间字段
        ptmCmpSku.setCreateTime(TimeUtils.nowDate());
        ptmCmpSku.setCategoryId(0L);
        long skuid = dbm.create(ptmCmpSku);

        //创建sku的时候，将固定网站的商品导入到mongodb中,只导入url、website、id
        if (WebsiteHelper.DEFAULT_WEBSITES.contains(ptmCmpSku.getWebsite())) {

            SummaryProduct summaryProduct = new SummaryProduct();

            summaryProduct.setId(skuid);
            summaryProduct.setWebsite(ptmCmpSku.getWebsite());
            summaryProduct.setUrl(ptmCmpSku.getUrl());

            mdm.save(summaryProduct);
        }

        return ptmCmpSku;
    }

    private void importCmpSku2solr(PtmCmpSku ptmCmpSku) {
        cmpskuIndexService.createOrUpdate(new CmpSkuModel(ptmCmpSku));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCmpSku(long id, String url, String color, String size, float price) {
        PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(id);

        ptmCmpSkuUpdater.getPo().setUpdateTime(TimeUtils.nowDate());
        ptmCmpSkuUpdater.getPo().setUrl(url);
        ptmCmpSkuUpdater.getPo().setPrice(price);
        ptmCmpSkuUpdater.getPo().setColor(color);
        ptmCmpSkuUpdater.getPo().setSize(size);

        Website website = WebsiteHelper.getWebSite(url);
        if (website != null) {
            ptmCmpSkuUpdater.getPo().setWebsite(website);
        }

        dbm.update(ptmCmpSkuUpdater);
    }

    @Override
    public void updateCmpSku(long id, String url, float price, SkuStatus skuStatus) {

        PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(id);

        ptmCmpSkuUpdater.getPo().setUpdateTime(TimeUtils.nowDate());
        ptmCmpSkuUpdater.getPo().setUrl(url);
        ptmCmpSkuUpdater.getPo().setPrice(price);
        ptmCmpSkuUpdater.getPo().setStatus(skuStatus);

        dbm.update(ptmCmpSkuUpdater);
    }

    @Override
    public void updateCmpSku(long id, SkuStatus skuStatus, String skuTitle, float price, String imageUrl, String url, String deeplink) {

        PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(id);

        ptmCmpSkuUpdater.getPo().setStatus(skuStatus);
        ptmCmpSkuUpdater.getPo().setSkuTitle(skuTitle);
        ptmCmpSkuUpdater.getPo().setPrice(price);

        if (!StringUtils.isEmpty(imageUrl)) {
            ptmCmpSkuUpdater.getPo().setOriImageUrl(imageUrl);
        }

        ptmCmpSkuUpdater.getPo().setUpdateTime(TimeUtils.nowDate());
        ptmCmpSkuUpdater.getPo().setUrl(url);
        ptmCmpSkuUpdater.getPo().setDeeplink(deeplink);

        dbm.update(ptmCmpSkuUpdater);
    }

    @Override
    public void createPtmCmpSkuIndexToMongo(PtmCmpSku ptmCmpSku) {

        if (!SkuStatus.ONSALE.equals(ptmCmpSku.getStatus())) {
            return;
        }

        if (ptmCmpSku.getWebsite() == null) {
            return;
        }

        Website website = ptmCmpSku.getWebsite();
        if (WebsiteHelper.DEFAULT_WEBSITES.contains(website)) {

            Long id = ptmCmpSku.getId();
            long productId = ptmCmpSku.getProductId();
            String sourcePid = ptmCmpSku.getSourcePid();
            String sourceSid = ptmCmpSku.getSourceSid();
            float price = ptmCmpSku.getPrice();
            String oriUrl = ptmCmpSku.getOriUrl();
            if (StringUtils.isEmpty(oriUrl)) {
                oriUrl = ptmCmpSku.getUrl();
            }
            String url = WebsiteHelper.getCleanUrl(website, oriUrl);
            String title = ptmCmpSku.getTitle();
            String skuTitle = StringUtils.isEmpty(ptmCmpSku.getSkuTitle()) ? title : ptmCmpSku.getSkuTitle();

            if (Website.SNAPDEAL.equals(website)) {
                sourceSid = SnapdealHelper.getSkuIdByUrl(oriUrl);
            } else if (Website.FLIPKART.equals(website)) {
                sourceSid = FlipkartHelper.getSkuIdByUrl(oriUrl);
                sourcePid = FlipkartHelper.getProductIdByUrl(oriUrl);
                url = FlipkartHelper.getUrlByDeeplink(url);
                url = FlipkartHelper.getCleanUrl(url);
            }

            if (!StringUtils.isEmpty(sourceSid)) {

                if (StringUtils.isEmpty(sourceSid) && StringUtils.isEmpty(skuTitle)) {
                    return;
                }

                PtmCmpSkuIndex index = new PtmCmpSkuIndex(id, productId, website, sourcePid, sourceSid, title, skuTitle, price, url);

                mdm.save(index);
                logger.debug(index.toString());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPtmCmpSkuIndexToMysql(PtmCmpSku ptmCmpSku) {

        if (ptmCmpSku.getWebsite() == null) {
            return;
        }

        Website website = ptmCmpSku.getWebsite();
        if (WebsiteHelper.DEFAULT_WEBSITES.contains(website)) {

            Long id = ptmCmpSku.getId();
            long productId = ptmCmpSku.getProductId();
            String sourcePid = ptmCmpSku.getSourcePid();
            String sourceSid = ptmCmpSku.getSourceSid();
            float price = ptmCmpSku.getPrice();
            String oriUrl = ptmCmpSku.getOriUrl();
            if (StringUtils.isEmpty(oriUrl)) {
                oriUrl = ptmCmpSku.getUrl();
            }
            String url = WebsiteHelper.getCleanUrl(website, oriUrl);
            String title = ptmCmpSku.getTitle();
            String skuTitle = StringUtils.isEmpty(ptmCmpSku.getSkuTitle()) ? title : ptmCmpSku.getSkuTitle();

            if (Website.SNAPDEAL.equals(website)) {
                sourceSid = SnapdealHelper.getSkuIdByUrl(oriUrl);
            } else if (Website.FLIPKART.equals(website)) {
                sourceSid = FlipkartHelper.getSkuIdByUrl(oriUrl);
                sourcePid = FlipkartHelper.getProductIdByUrl(oriUrl);
                url = FlipkartHelper.getUrlByDeeplink(url);
                url = FlipkartHelper.getCleanUrl(url);
            }


            if (StringUtils.isEmpty(skuTitle)) {
                return;
            }

            PtmCmpSkuIndex2 index = new PtmCmpSkuIndex2(id, productId, website, sourcePid, sourceSid, title, skuTitle, price, url);

            //设置新增时间
            index.setCreateTime(TimeUtils.nowDate());
            dbm.create(index);

            logger.debug("id = [" + id + "],website = [" + website + "],sourceSid = [" + sourceSid + "],skutitle = [" + skuTitle + "]");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCmpSkuBySummaryProduct(long id, FetchedProduct fetchedProduct) {

        //summaryProduct为null
        if (fetchedProduct == null) {
            return;
        }

        //根据id，获得该商品信息
        PtmCmpSku cmpSku = dbm.get(PtmCmpSku.class, id);
        if (cmpSku == null) {
            return;
        }
//        更新mongodb
        PtmCmpSkuLog ptmCmpSkuLog = new PtmCmpSkuLog(cmpSku);
        mdm.save(ptmCmpSkuLog);

        PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(id);
        //获取商品的status
        if (ProductStatus.OFFSALE.equals(fetchedProduct.getProductStatus())) {//如果OFFSALE

            ptmCmpSkuUpdater.getPo().setStatus(SkuStatus.OFFSALE);

        } else {
            //如果售空了修改状态
            if (ProductStatus.OUTSTOCK.equals(fetchedProduct.getProductStatus())) {

                ptmCmpSkuUpdater.getPo().setStatus(SkuStatus.OUTSTOCK);

            } else {//修改价格

                float price = fetchedProduct.getPrice();
                if (price > 0) {
                    if (cmpSku.getPrice() != fetchedProduct.getPrice()) {
                        ptmCmpSkuUpdater.getPo().setPrice(price);
                    }
                }
                ptmCmpSkuUpdater.getPo().setStatus(SkuStatus.ONSALE);
            }

            if (!StringUtils.isEmpty(fetchedProduct.getTitle())) {
                if (StringUtils.isEmpty(cmpSku.getTitle()) || !StringUtils.isEqual(cmpSku.getTitle(), fetchedProduct.getTitle())) {
                    ptmCmpSkuUpdater.getPo().setTitle(fetchedProduct.getTitle());
                }
            }

            String imageUrl = fetchedProduct.getImageUrl();
            if (StringUtils.isEmpty(cmpSku.getOriImageUrl()) || !StringUtils.isEqual(imageUrl, cmpSku.getOriImageUrl())) {
                if (!StringUtils.isEmpty(imageUrl)) {
                    ptmCmpSkuUpdater.getPo().setOriImageUrl(imageUrl);
                }
            }
        }

        if (cmpSku.getWebsite() == null) {
            Website website = fetchedProduct.getWebsite();
            if (website != null) {
                ptmCmpSkuUpdater.getPo().setWebsite(fetchedProduct.getWebsite());
            }
        }

        if (!StringUtils.isEmpty(fetchedProduct.getSubTitle())) {
            ptmCmpSkuUpdater.getPo().setSkuTitle(fetchedProduct.getTitle() + fetchedProduct.getSubTitle());
        }

        ptmCmpSkuUpdater.getPo().setUpdateTime(TimeUtils.nowDate());
        dbm.update(ptmCmpSkuUpdater);
    }
}
