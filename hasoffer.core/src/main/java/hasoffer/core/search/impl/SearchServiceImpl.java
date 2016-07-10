package hasoffer.core.search.impl;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.HasofferRegion;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.analysis.ProductAnalysisService;
import hasoffer.core.bo.product.ProductBo;
import hasoffer.core.bo.product.SearchedSku;
import hasoffer.core.bo.system.SearchLogBo;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.persistence.po.search.SrmSearchUpdateLog;
import hasoffer.core.persistence.po.search.updater.SrmSearchLogUpdater;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.fetch.model.ListProduct;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created on 2015/12/29.
 */
@Service
public class SearchServiceImpl implements ISearchService {

    private static final String Q_NORESULT_SEARCH_LOG =
            " SELECT t FROM SrmSearchLog t " +
                    " WHERE t.ptmProductId = 0 " +
//                    "   AND t.precise <> 'TIMERSET' " +
                    " ORDER BY t.createTime ASC ";
    private static final String Q_NORESULT_AFTERTIME_SEARCH_LOG =
            " SELECT t FROM SrmSearchLog t " +
                    " WHERE t.ptmProductId = 0 " +
//                    "   AND t.precise <> 'TIMERSET' " +
                    "   AND t.createTime > ?0 " +
                    " ORDER BY t.createTime ASC ";
    private static final String Q_NORESULT_SEARCH_LOG_BY_PRECISE =
            " SELECT t FROM SrmSearchLog t " +
                    " WHERE t.ptmProductId = 0 " +
                    "   AND t.precise = ?0 " +
                    "   AND t.createTime > ?1 " +
                    " ORDER BY t.createTime ASC ";
    private static final String Q_COUNT_LOG_BY_PRODUCT = "SELECT count(t.id) FROM SrmSearchLog t WHERE t.ptmProductId = ?0 ";
    private static final String Q_SEARCH_LOG_BY_PRODUCTID = "SELECT t FROM SrmSearchLog t WHERE t.ptmProductId = ?0 ";
    private static final String C_KEYWORD = "select count(t.id) from SrmSearchLog t where t.site=?0 and t.keyword=?1";
    private static final String D_SEARCH_LOG = "delete FROM SrmSearchLog t where t.id in (:ids) ";

    private static final String Q_SEARCH_COUNT = "SELECT t FROM SrmProductSearchCount t WHERE t.ymd=?0 ORDER BY t.count DESC";
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    IProductService productService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;

    private Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Override
    public PageableResult<SrmProductSearchCount> findSearchCountsByYmd(String ymd, int page, int size) {
        return dbm.queryPage(Q_SEARCH_COUNT, page, size, Arrays.asList(ymd));
    }

    @Override
    public void saveSearchCount(String ymd) {
        logger.debug(String.format("save search count [%s]", ymd));

        List<SrmProductSearchCount> spscs = new ArrayList<SrmProductSearchCount>();

        Map<Long, Long> countMap = searchLogCacheManager.getProductCount(ymd);

        int count = 0;
        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {

            long productId = countKv.getKey();

            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId, SkuStatus.ONSALE);
            int size = 0;
            if (ArrayUtils.hasObjs(cmpSkus)) {
                size = cmpSkus.size();
            }

            spscs.add(new SrmProductSearchCount(ymd, productId, countKv.getValue(), size));

            if (count % 2000 == 0) {
                saveLogCount(spscs);
                count = 0;
                spscs.clear();
            }

        }

        if (ArrayUtils.hasObjs(spscs)) {
            saveLogCount(spscs);
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
    }

    @Override
    public void analysisAndRelate(SrmAutoSearchResult asr) {
        ProductBo productBo = null;

        String proIdStr = asr.getId();
        if (NumberUtils.isNumber(proIdStr)) {
            long proId = Long.valueOf(proIdStr);
            productBo = productService.getProductBo(proId);
        }

        ProductAnalysisService.analysisProducts(asr, productBo);

        relateUnmatchedSearchLogx(asr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeProducts(PtmProduct finalProduct, Map<Website, PtmCmpSku> cmpSkuMap, PtmProduct product) {
        // 把关联的log转到第一个商品下
        PageableResult<SrmSearchLog> pagedSeachLogs = listSearchLogsByProductId(product.getId(), 1, Integer.MAX_VALUE);
        List<SrmSearchLog> searchLogs = pagedSeachLogs.getData();

        if (ArrayUtils.hasObjs(searchLogs)) {
            for (SrmSearchLog searchLog : searchLogs) {
                SrmSearchLogUpdater updater = new SrmSearchLogUpdater(searchLog.getId());
                updater.getPo().setPtmProductId(finalProduct.getId());
                dbm.update(updater);
            }
        }

        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(product.getId());
        for (PtmCmpSku cmpSku : cmpSkus) {
            Website website = cmpSku.getWebsite();
            if (website != null && !cmpSkuMap.containsKey(website)) {
                PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(cmpSku.getId());
                updater.getPo().setProductId(finalProduct.getId());
                dbm.update(updater);
                cmpSkuMap.put(website, cmpSku);
            }
//            dbm.delete(PtmCmpSku.class, cmpSku.getId());
        }

        productService.deleteProduct(product.getId());
    }

    @Override
    public long statLogsCountByProduct(long productId) {
        return dbm.querySingle(Q_COUNT_LOG_BY_PRODUCT, Arrays.asList(productId));
    }

    @Override
    public PageableResult<SrmSearchLog> listSearchLogsByProductId(long productId, int page, int size) {
        return dbm.queryPage(Q_SEARCH_LOG_BY_PRODUCTID, page, size, Arrays.asList(productId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void relateUnmatchedSearchLog(SrmSearchLog searchLog, Map<Website, ListProduct> listProductMap) {
        SrmSearchLogUpdater srmSearchLogUpdater = new SrmSearchLogUpdater(searchLog.getId());

        if (listProductMap.isEmpty()) {
            srmSearchLogUpdater.getPo().setPrecise(SearchPrecise.TIMERSET);
            dbm.update(srmSearchLogUpdater);
            return;
        }

        long ptmProductId = searchLog.getPtmProductId();
        if (ptmProductId == 0) {
            ProductBo productBo = productService.createProductByListProducts(listProductMap);
            srmSearchLogUpdater.getPo().setPtmProductId(productBo.getId());
        } else {
            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(ptmProductId);

            Set<Website> siteSet = new HashSet<Website>();
            for (PtmCmpSku cmpSku : cmpSkus) {
                siteSet.add(cmpSku.getWebsite());
            }

            for (Map.Entry<Website, ListProduct> kv : listProductMap.entrySet()) {
                ListProduct listProduct = kv.getValue();
                if (siteSet.contains(listProduct.getWebsite())) {
                    continue;
                }
                PtmCmpSku cmpSku = productService.createCmpsku(ptmProductId, listProduct.getPrice(),
                        listProduct.getUrl(), listProduct.getTitle(), listProduct.getImageUrl(), "");
            }
        }

        srmSearchLogUpdater.getPo().setPrecise(SearchPrecise.TIMERSET);
        dbm.update(srmSearchLogUpdater);
    }

    @Override
    @Transactional
    public void relateUnmatchedSearchLogx(SrmAutoSearchResult autoSearchResult) {
        if (autoSearchResult == null) {
            return;
        }

        autoSearchResult.setlRelateTime(TimeUtils.now());

        SrmSearchLogUpdater srmSearchLogUpdater = new SrmSearchLogUpdater(autoSearchResult.getId());

        Map<Website, List<SearchedSku>> searchedSkuMap = autoSearchResult.getFinalSkus();

        if (searchedSkuMap == null || searchedSkuMap.size() == 0) {
            logger.debug("searched sku map is empty");
//            srmSearchLogUpdater.getPo().setPrecise(SearchPrecise.TIMERSET2);
            dbm.update(srmSearchLogUpdater);

            mdm.save(autoSearchResult);
            return;
        }

        Map<String, PtmCmpSku> skuMap = new HashMap<String, PtmCmpSku>();

        long ptmProductId = autoSearchResult.getRelatedProId();

        if (ptmProductId == 0) {
            logger.debug("build product...");
            SearchedSku stdSku = null;
            List<SearchedSku> logSkus = searchedSkuMap.get(Website.valueOf(autoSearchResult.getFromWebsite()));
            if (ArrayUtils.hasObjs(logSkus)) {
                stdSku = logSkus.get(0);
            } else {
                Iterator<Website> websiteIterator = searchedSkuMap.keySet().iterator();
                Website website = websiteIterator.next();
                stdSku = searchedSkuMap.get(website).get(0);
            }

            ProductBo productBo = productService.createProduct(0, autoSearchResult.getTitle(), autoSearchResult.getPrice(),
                    stdSku.getWebsite(), stdSku.getUrl(), stdSku.getSourceId(), stdSku.getImageUrl());

            ptmProductId = productBo.getId();

            srmSearchLogUpdater.getPo().setPtmProductId(ptmProductId);

            autoSearchResult.setRelatedProId(ptmProductId);
        } else {
            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(ptmProductId);
            for (PtmCmpSku cmpSku : cmpSkus) {
                if (StringUtils.isEmpty(cmpSku.getUrl())) {
                    continue;
                }
                skuMap.put(cmpSku.getUrl(), cmpSku);
            }
        }

        // todo - 如果同一个网站，两次抓取结果不同，第二次抓取的更准确，则要把第一次的结果更新为更准确的sku
        for (Map.Entry<Website, List<SearchedSku>> kv : searchedSkuMap.entrySet()) {

            List<SearchedSku> ssku = kv.getValue();
            float titleScore = 0;

            for (SearchedSku searchedSku : ssku) {
                float thd_title_score = 0.5f, thd_price_score = 0.5f;

                if (AppConfig.get(AppConfig.SER_REGION).equals(HasofferRegion.USA)) {
                    thd_title_score = 0.3f;
                    thd_price_score = 0.8f;
                }

                if (searchedSku.getTitleScore() < thd_title_score || searchedSku.getPriceScore() > thd_price_score) {
                    logger.debug(String.format("title/price:[%s/%f].titleScore/priceScore:[%f/%f]", searchedSku.getTitle(), searchedSku.getPrice(), searchedSku.getTitleScore(), searchedSku.getPriceScore()));
                    continue;
                }

                String skuUrl = searchedSku.getUrl();

                // 如果sku存在，则进行下次循环
                if (skuMap.containsKey(skuUrl)) {
                    logger.debug("sku exists, continue...");
                    continue;
                }

                if (titleScore == 0) {
                    titleScore = searchedSku.getTitleScore();
                } else if (titleScore != searchedSku.getTitleScore()) {
                    break;
                }

                PtmCmpSku cmpSku = productService.createCmpsku(ptmProductId, searchedSku.getPrice(),
                        skuUrl, searchedSku.getTitle(), searchedSku.getImageUrl());
                /*
                PtmCmpSkuIndex skuIndex = cmpSkuService.getCmpSkuIndex(searchedSku.getUrl());
                if (skuIndex == null) {
                    PtmCmpSku cmpSku = productService.createCmpsku(ptmProductId, searchedSku.getPrice(),
                            searchedSku.getUrl(), searchedSku.getTitle(), searchedSku.getImageUrl());
                } else {
                    // 更新到新的商品下
                    PtmCmpSkuUpdater cmpSkuUpdater = new PtmCmpSkuUpdater(skuIndex.getId());
                    cmpSkuUpdater.getPo().setProductId(ptmProductId);
                    cmpSkuService.updateCmpSku(cmpSkuUpdater);
                }*/
            }
        }

        srmSearchLogUpdater.getPo().setPrecise(SearchPrecise.TIMERSET2);
        dbm.update(srmSearchLogUpdater);

        mdm.save(autoSearchResult);
    }

    @Override
    public PageableResult<SrmSearchLog> listNoresultSearchLogs(int page, int size) {
        return dbm.queryPage(Q_NORESULT_SEARCH_LOG, page, size);
    }

    @Override
    public PageableResult<SrmSearchLog> listNoresultSearchLogs(Date startDate, int page, int size) {
        if (startDate == null) {
            return dbm.queryPage(Q_NORESULT_SEARCH_LOG, page, size);
        }
        return dbm.queryPage(Q_NORESULT_AFTERTIME_SEARCH_LOG, page, size, Arrays.asList(startDate));
    }

    @Override
    public PageableResult<SrmSearchLog> listNoresultSearchLogs(SearchPrecise searchPrecise, Date startDate, int page, int size) {

        if (startDate == null) {
            startDate = TimeUtils.getAppTime0();
        }

        return dbm.queryPage(Q_NORESULT_SEARCH_LOG_BY_PRECISE, page, size, Arrays.asList(searchPrecise, startDate));
    }

    @Override
    public PageableResult<SrmSearchLog> listSearchLogs(int page, int size) {
        StringBuffer sb = new StringBuffer(" SELECT t FROM SrmSearchLog t ORDER BY t.createTime ASC ");

        return dbm.queryPage(sb.toString(), page, size);
    }

    @Override
    public PageableResult<SrmSearchLog> listSearchLogs(SearchPrecise precise, Date startTime, Date endTime, int searchCount, int page, int size) {
        StringBuffer sb = new StringBuffer(" SELECT t FROM SrmSearchLog t ");

        sb.append(" WHERE t.createTime >= ?0 AND t.createTime < ?1 AND t.count >= ?2 ");

        List params = new ArrayList();
        params.add(startTime);
        params.add(endTime);
        params.add(searchCount);
        if (precise != null) {
            sb.append(" AND t.precise = ?3 ");
            params.add(precise);
        }

        sb.append(" ORDER BY t.createTime ASC ");

        return dbm.queryPage(sb.toString(), page, size, params);
    }

    @Override
    public PageableResult<SrmSearchLog> listSearchLogs(int queryType, String sort, Date startTime, Date endTime, String precise, int page, int size) {
        String sortBy = "time";
        String sortDir = "DESC";
        int index = 0;
        if ((index = sort.indexOf("_")) > 0) {
            sortBy = sort.substring(0, index);
            sortDir = sort.substring(index + 1).toUpperCase();
        }

        if ("time".equals(sortBy)) {
            sortBy = "updateTime";
        }

        StringBuffer sb = new StringBuffer(" SELECT t FROM SrmSearchLog t ");

        switch (queryType) {
            case 1:
                sb.append(" WHERE t.ptmProductId = 0 ");
                break;
            case 2:
                sb.append(" WHERE t.ptmProductId > 0 ");
                break;
            default:
                sb.append(" WHERE t.ptmProductId >= 0 ");
                break;
        }

        sb.append(" AND t.createTime >= ?0 AND t.createTime < ?1 ");

        if ("NOCHECK".equals(precise)) {
            sb.append(" AND t.precise = 'NOCHECK' ");
        } else if ("MANUALSET".equals(precise)) {
            sb.append(" AND t.precise = 'MANUALSET' ");
        } else if ("TIMERSET".equals(precise)) {
            sb.append(" AND t.precise = 'TIMERSET' ");
        } else if ("TIMERSET2".equals(precise)) {
            sb.append(" AND t.precise = 'TIMERSET2' ");
        }

        sb.append(" ORDER BY t.").append(sortBy).append(" ").append(sortDir);

        return dbm.queryPage(sb.toString(), page, size, Arrays.asList(startTime, endTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setResultPreciseOk(String logId, long cmpSkuId, String skuUrl) {
        SrmSearchLog srmSearchLog = dbm.get(SrmSearchLog.class, logId);
        if (srmSearchLog == null) {
            return;
        }

        SrmSearchLogUpdater srmSearchLogUpdater = new SrmSearchLogUpdater(logId);

        if (cmpSkuId <= 0) {

            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(srmSearchLog.getPtmProductId());

            Website website = Website.valueOf(srmSearchLog.getSite());

            PtmCmpSku existCmpSku = null;
            if (ArrayUtils.hasObjs(cmpSkus)) {
                for (PtmCmpSku cmpSku : cmpSkus) {
                    if (website.equals(cmpSku.getWebsite())) {
                        existCmpSku = cmpSku;
                        break;
                    }
                }
            }

            // 判断是否需要创建 ptmcmpsku
            if (existCmpSku == null) {
                PtmCmpSku cmpSku = new PtmCmpSku(srmSearchLog.getPtmProductId(), srmSearchLog.getPrice(), skuUrl);
                cmpSkuService.createCmpSku(cmpSku);

                existCmpSku = cmpSku;
            }

            srmSearchLogUpdater.getPo().setPtmCmpSkuId(existCmpSku.getId());
        }

        srmSearchLogUpdater.getPo().setPrecise(SearchPrecise.MANUALSET);
        dbm.update(srmSearchLogUpdater);
    }

    @Override
    // 更新问题
//    @Cacheable(value = CACHE_KEY, key = "#root.methodName + '_' + #root.args[0]")
    public SrmSearchLog findSrmSearchLogById(String id) {
        return dbm.get(SrmSearchLog.class, id);
    }

    @Override
    public void setResultPreciseManualset(String logId) {
        SrmSearchLog srmSearchLog = dbm.get(SrmSearchLog.class, logId);
        if (srmSearchLog == null) {
            return;
        }

        // 更新log 为Manualset
        SrmSearchLogUpdater srmSearchLogUpdater = new SrmSearchLogUpdater(logId);
        srmSearchLogUpdater.getPo().setPrecise(SearchPrecise.MANUALSET);
        srmSearchLogUpdater.getPo().setManualSetTime(TimeUtils.nowDate());
        dbm.update(srmSearchLogUpdater);
    }

    @Override
    public void setPtmProductId(String logId, long productId) {

        SrmSearchLog srmSearchLog = dbm.get(SrmSearchLog.class, logId);
        if (srmSearchLog == null) {
            return;
        }

        // 更新log 的ptmProductId
        SrmSearchLogUpdater srmSearchLogUpdater = new SrmSearchLogUpdater(logId);
        srmSearchLogUpdater.getPo().setPtmProductId(productId);
        dbm.update(srmSearchLogUpdater);
    }

    @Override
    public PageableResult<SrmSearchLog> listSearchLogs(int queryType, String sort, int page, int size) {

        String sortBy = "time";
        String sortDir = "DESC";
        int index = 0;
        if ((index = sort.indexOf("_")) > 0) {
            sortBy = sort.substring(0, index);
            sortDir = sort.substring(index + 1).toUpperCase();
        }

        if ("time".equals(sortBy)) {
            sortBy = "updateTime";
        }

        StringBuffer sb = new StringBuffer(" SELECT t FROM SrmSearchLog t ");

        switch (queryType) {
            case 1:
                sb.append(" WHERE t.ptmProductId = 0 ");
                break;
            case 2:
                sb.append(" WHERE t.ptmProductId > 0 ");
                break;
            default:
                break;
        }

        sb.append(" ORDER BY t.").append(sortBy).append(" ").append(sortDir);

        return dbm.queryPage(sb.toString(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSrmSearchUpdateLog(SrmSearchUpdateLog srmSearchUpdateLog) {
        dbm.create(srmSearchUpdateLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSearchLog(SearchLogBo searchLogBo) {

        String keyword = searchLogBo.getKeyword();
        String brand = searchLogBo.getBrand();
        String site = searchLogBo.getSite();
        float price = searchLogBo.getPrice();
        long productId = searchLogBo.getProductId();
        long ptmCmpSkuId = searchLogBo.getCmpSkuId();
        long category = searchLogBo.getCategory();
        String sourceId = searchLogBo.getSourceId();

        String keywordMd5 = HexDigestUtil.md5(keyword + "-" + site); // 这个值作为log表的id

        // 根据 md5 后的 keyword 查询搜索记录
        SrmSearchLog searchLog = dbm.get(SrmSearchLog.class, keywordMd5);

        if (searchLog == null) {
            // 如果不存在，则创建
            searchLog = new SrmSearchLog(keywordMd5, site, sourceId, keyword, brand, price, category, productId, ptmCmpSkuId);
            dbm.create(searchLog);
        } else {
            Date updateTime = TimeUtils.nowDate();
            // 如果存在（有人搜索过的）,对该log计数器+1，同时记录关键字+1
            SrmSearchLogUpdater srmSearchLogUpdater = new SrmSearchLogUpdater(searchLog.getId());
            srmSearchLogUpdater.getPo().setUpdateTime(updateTime);
            srmSearchLogUpdater.getPo().setlUpdateTime(updateTime.getTime());
            srmSearchLogUpdater.getPo().setCount(searchLog.getCount() + 1);

            if (StringUtils.isEmpty(searchLog.getBrand()) && !StringUtils.isEmpty(brand)) {
                srmSearchLogUpdater.getPo().setBrand(brand);
            }
            if (StringUtils.isEmpty(searchLog.getSourceId()) && !StringUtils.isEmpty(sourceId)) {
                srmSearchLogUpdater.getPo().setSourceId(sourceId);
            }

            if (searchLog.getPrecise().equals(SearchPrecise.NOCHECK)) {
                if (searchLog.getCategory() != category) {
                    srmSearchLogUpdater.getPo().setCategory(category);
                }

                if (searchLog.getPtmProductId() != productId) {
                    srmSearchLogUpdater.getPo().setPtmProductId(productId);
                }

                if (searchLog.getPtmCmpSkuId() != ptmCmpSkuId) {
                    srmSearchLogUpdater.getPo().setPtmCmpSkuId(ptmCmpSkuId);
                }
            }

            dbm.update(srmSearchLogUpdater);
        }
    }

    @Override
    public long findKeywordCount(String site, String keyword) {
        return dbm.querySingle(C_KEYWORD, Arrays.asList(site, keyword));
    }

    @Override
    @Transactional
    public void updateSrmSearchLogStatus(String id, long productId, SearchPrecise precise) {

        SrmSearchLogUpdater srmSearchLogUpdater = new SrmSearchLogUpdater(id);
        srmSearchLogUpdater.getPo().setPrecise(precise);
        srmSearchLogUpdater.getPo().setPtmProductId(productId);

        dbm.update(srmSearchLogUpdater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLogCount(List<SrmProductSearchCount> searchCounts) {
        dbm.batchSave(searchCounts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveSearchLog(List<SearchLogBo> searchLogBoList) {
        Map<String, SrmSearchLog> searchLogBoMap = new HashMap<String, SrmSearchLog>();
        for (SearchLogBo searchLogBo : searchLogBoList) {
            String keyword = searchLogBo.getKeyword();
            String brand = searchLogBo.getBrand();
            String site = searchLogBo.getSite();
            float price = searchLogBo.getPrice();
            long productId = searchLogBo.getProductId();
            long ptmCmpSkuId = searchLogBo.getCmpSkuId();
            long category = searchLogBo.getCategory();
            String sourceId = searchLogBo.getSourceId();

            String keywordMd5 = HexDigestUtil.md5(keyword + "-" + site); // 这个值作为log表的id
            SrmSearchLog srmSearchTemp = searchLogBoMap.get(keywordMd5);
            if (srmSearchTemp == null) {
                srmSearchTemp = searchLogCacheManager.findSrmSearchLogById(keywordMd5);
                if (srmSearchTemp == null) {
                    if (sourceId == null || "".equals(sourceId)) {
                        sourceId = "0";
                    }
                    srmSearchTemp = new SrmSearchLog(keywordMd5, site, sourceId, keyword, brand, price, category, productId, ptmCmpSkuId);
                } else {
                    srmSearchTemp.setCount(srmSearchTemp.getCount());
                    srmSearchTemp.setUpdateTime(TimeUtils.nowDate());
                }
                searchLogBoMap.put(keywordMd5, srmSearchTemp);
            } else {
                srmSearchTemp.setCount(srmSearchTemp.getCount());
                srmSearchTemp.setUpdateTime(TimeUtils.nowDate());
            }

            if (StringUtils.isEmpty(srmSearchTemp.getBrand()) && !StringUtils.isEmpty(brand)) {
                srmSearchTemp.setBrand(brand);
            }
            if (StringUtils.isEmpty(srmSearchTemp.getSourceId()) && !StringUtils.isEmpty(sourceId)) {
                srmSearchTemp.setSourceId(sourceId);
            }

            if (srmSearchTemp.getPrecise().equals(SearchPrecise.NOCHECK)) {
                if (srmSearchTemp.getCategory() != category) {
                    srmSearchTemp.setCategory(category);
                }

                if (srmSearchTemp.getPtmProductId() != productId) {
                    srmSearchTemp.setPtmProductId(productId);
                }

                if (srmSearchTemp.getPtmCmpSkuId() != ptmCmpSkuId) {
                    srmSearchTemp.setPtmCmpSkuId(ptmCmpSkuId);
                }
            }
        }

        List<String> idList = new ArrayList<String>();
        List<SrmSearchLog> searchLogList = new ArrayList<SrmSearchLog>();
        for (Map.Entry<String, SrmSearchLog> temp : searchLogBoMap.entrySet()) {
            idList.add(temp.getKey());
            searchLogList.add(temp.getValue());
        }
        if (idList.size() > 0) {
            dbm.batchDelete(D_SEARCH_LOG, idList.toArray(new String[idList.size()]));
        }
        if (searchLogBoList.size() > 0) {
            dbm.batchSave(searchLogList);
        }


        // 根据 md5 后的 keyword 查询搜索记录
//        SrmSearchLog searchLog = dbm.get(SrmSearchLog.class, keywordMd5);

//        if (searchLog == null) {
//            // 如果不存在，则创建
////            searchLog = new SrmSearchLog(keywordMd5, site, sourceId, keyword, brand, price, category, productId, ptmCmpSkuId);
////            dbm.create(searchLog);
//        } else {
//            // 如果存在（有人搜索过的）,对该log计数器+1，同时记录关键字+1
//            SrmSearchLogUpdater srmSearchLogUpdater = new SrmSearchLogUpdater(searchLog.getId());
//            srmSearchLogUpdater.getPo().setUpdateTime(TimeUtils.nowDate());
//            srmSearchLogUpdater.getPo().setCount(searchLog.getCount() + 1);
//
//            if (StringUtils.isEmpty(searchLog.getBrand()) && !StringUtils.isEmpty(brand)) {
//                srmSearchLogUpdater.getPo().setBrand(brand);
//            }
//            if (StringUtils.isEmpty(searchLog.getSourceId()) && !StringUtils.isEmpty(sourceId)) {
//                srmSearchLogUpdater.getPo().setSourceId(sourceId);
//            }
//
//            if (searchLog.getPrecise().equals(SearchPrecise.NOCHECK)) {
//                if (searchLog.getCategory() != category) {
//                    srmSearchLogUpdater.getPo().setCategory(category);
//                }
//
//                if (searchLog.getPtmProductId() != productId) {
//                    srmSearchLogUpdater.getPo().setPtmProductId(productId);
//                }
//
//                if (searchLog.getPtmCmpSkuId() != ptmCmpSkuId) {
//                    srmSearchLogUpdater.getPo().setPtmCmpSkuId(ptmCmpSkuId);
//                }
//            }
//
//            dbm.update(srmSearchLogUpdater);
//        }

    }
}
