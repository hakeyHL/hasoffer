package hasoffer.core.admin.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ExcelUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.HibernateDao;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.app.AppDealComment;
import hasoffer.core.persistence.po.app.AppDealThumb;
import hasoffer.core.persistence.po.app.updater.AppBannerUpdater;
import hasoffer.core.persistence.po.app.updater.AppDealUpdater;
import hasoffer.core.product.solr.DealIndexServiceImpl;
import hasoffer.core.product.solr.DealModel;
import hasoffer.core.task.ListProcessTask;
import hasoffer.core.task.worker.ILister;
import hasoffer.core.task.worker.IProcessor;
import hasoffer.core.utils.excel.ExcelImporter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.TempFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lihongde on 2016/6/21 14:03
 */
@Service
@Transactional
public class DealServiceImpl implements IDealService {

    private static final long EXPIRE_TIME_MS = 7 * 24 * 60 * 60 * 1000;

    //手动导入deal的sql
//    private static final String IMPORT_SQL = "insert into appdeal(website, title, linkUrl, expireTime, priceDescription ,description, createTime,  push ,display ,imageUrl,discount,dealCategoryId,dealClickCount,appdealSource,ptmcmpskuid) values(?,?, ?, ?, ?, ? ,?, ?, ?, ?,?,?,?,'MANUAL_INPUT',0)";

    private static final String Q_DEALS = "SELECT t FROM AppDeal t";
    private static final String Q_THUMB_UIDDID = "SELECT t FROM AppDealThumb t where t.userId=?0 and t.dealId=?1";
    private static final String Q_THUMB_TOTAL = "SELECT sum(t.action) FROM AppDealThumb t where t.dealId=?0";
    private static final String Q_COMMENTS_DEALID = "SELECT t FROM AppDealComment t where t.dealId=?0 order by t.createTime desc ";

    @Resource
    IDataBaseManager dbm;
    @Resource
    DealIndexServiceImpl dealIndexService;
    @Resource
    ExcelImporter importer;
    @Resource
    private HibernateDao dao;

    public static void main(String[] args) {
        if (!(Website.valueOf("SHOP") instanceof Website)) {
            System.out.printf("hha ");
        }
    }

    @Override
    public PageableResult<AppDeal> findDealList(int page, int size, int type, String orderByField) {

        String querySql = "select t from AppDeal t WHERE t.appdealSource = 'AppDealSourceType' order by t.weight desc, t." + orderByField + " desc";

        if (type == 1) {
            querySql = querySql.replace("AppDealSourceType", "MANUAL_INPUT");
            return dbm.queryPage(querySql, page, size);
        } else if (type == 2) {
            querySql = querySql.replace("AppDealSourceType", "PRICE_OFF");
            return dbm.queryPage(querySql, page, size);
        } else if (type == 3) {
            querySql = querySql.replace("AppDealSourceType", "DEAL_SITE");
            return dbm.queryPage(querySql, page, size);
        } else {
            querySql = querySql.replace("WHERE t.appdealSource = 'AppDealSourceType'", "");
            return dbm.queryPage(querySql, page, size);
        }
    }

    @Override
    public Map<String, Object> importExcelFile(MultipartFile multipartFile) throws Exception {

        Map<String, Object> importResult = new HashMap<>();
        //import deal into database
        String originalFilename = multipartFile.getOriginalFilename();
        File tempFile = TempFile.createTempFile("/hasoffer/", originalFilename.substring(originalFilename.indexOf("."), originalFilename.length()));
        InputStream inputStream = multipartFile.getInputStream();
        FileUtils.copyInputStreamToFile(inputStream, tempFile);
        inputStream.close();
        int failNum = 0;
        int emptyLinkNum = 0;
        int repeatLinkNum = 0;
        List<String> linkList = new ArrayList<>();
        List<Map<String, String>> maps = ExcelUtils.readRows(1, tempFile);
        for (int i = 0; i < maps.size(); i++) {
            Map<String, String> stringStringMap = maps.get(i);
            //set attribute to object
            AppDeal appDeal = new AppDeal();
            //1. website
            String website = stringStringMap.get("0");
            try {
                if (!StringUtils.isEmpty(website)) {
                    appDeal.setWebsite(Website.valueOf(website.trim()));
                } else {
                    emptyLinkNum++;
                    failNum++;
                    continue;
                }
            } catch (Exception e) {
                failNum++;
                importResult.put("errorMessage", "未识别的site / not recognized website type at row  " + (i + 1));
                continue;
                //not a website
            }
            //2. deal title
            String dealTitle = stringStringMap.get("1");
            if (!StringUtils.isEmpty(dealTitle)) {
                appDeal.setTitle(dealTitle);
            } else {
                emptyLinkNum++;
                failNum++;
                continue;
            }

            //3. link
            String dealLink = stringStringMap.get("2");
            if (!StringUtils.isEmpty(dealLink)) {
                if (linkList.contains(dealLink)) {
                    repeatLinkNum++;
                    failNum++;
                    continue;
                } else {
                    linkList.add(dealLink);
                    appDeal.setLinkUrl(dealLink.trim());
                }
            } else {
                emptyLinkNum++;
                failNum++;
                continue;
            }

            //4. deal expireTime

            String dealExpTime = stringStringMap.get("3");
            if (!StringUtils.isEmpty(dealExpTime)) {
                appDeal.setExpireTime(new SimpleDateFormat("yyyy/MM/dd").parse(dealExpTime.trim()));
            } else {
                //is empty ,set after 7 days
                appDeal.setExpireTime(TimeUtils.addDay(new Date(), 7));
            }

            //5. price description
            String dealPriceDescription = stringStringMap.get("4");
            if (!StringUtils.isEmpty(dealPriceDescription)) {
                appDeal.setPriceDescription(dealPriceDescription);
            }

            //6. maybe have deal description
            String dealDescription = stringStringMap.get("5");
            if (!StringUtils.isEmpty(dealDescription)) {
                appDeal.setDescription(dealDescription);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(website).append(" is offering ").append(dealTitle).append(" .\n");
                sb.append("\n");
                sb.append("Steps to order the item at ").append(website).append(" website: \n");
                sb.append("\n");
                sb.append("1. First, visit the offer page at ").append(website).append(" .\n");
                sb.append("2. Select your product according to the item variety.\n");
                sb.append("3. Then click on Buy Now option. \n");
                sb.append("4. Sign in/ Sign up at ").append(website).append(" and fill up your address. \n");
                sb.append("5. Choose your payment option and make payment your cart value.").append(" .\n");
                appDeal.setDescription(sb.toString());
            }
            try {
                //TODO 插入时报无id
                appDeal.setCreateTime(new Date());
                Long aLong = dbm.create(appDeal);
                dealIndexService.createOrUpdate(new DealModel(appDeal));
                System.out.println(aLong);
            } catch (Exception e) {
                failNum++;
                importResult.put("errorMessage", "插入失败 / insert fail  : row " + (i + 1) + "  , msg : " + e.getMessage());
                continue;
            }
            System.out.println("over ");
        }
        importResult.put("totalRows", maps.size());
        importResult.put("successRows", maps.size() - failNum);
        importResult.put("failRows", failNum);
        importResult.put("nullRows", emptyLinkNum);
        importResult.put("repeatRows", repeatLinkNum);
        importResult.put("errorMessage", importResult.get("errorMessage") == null ? "无" : importResult.get("errorMessage"));
        return importResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppDeal createAppDealByPriceOff(AppDeal appDeal) {

        Long aLong = dbm.create(appDeal);
        appDeal.setId(aLong);

        return appDeal;
    }

    @Override
    public AppDeal getDealById(Long dealId) {
        return dbm.get(AppDeal.class, dealId);
    }

    @Override
    public AppDeal getDealBySourceId(Long skuId) {
        return dbm.querySingle("SELECT t FROM AppDeal t WHERE t.appdealSource = 'PRICE_OFF' AND t.ptmcmpskuid = ?0 ", Arrays.asList(skuId));
    }

    @Override
    public AppBanner getBannerByDealId(Long dealId) {
        return (AppBanner) dbm.querySingle("SELECT t FROM AppBanner t WHERE t.sourceId = ?0 ", Arrays.asList(dealId.toString()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDealExpire(Long id) {

        AppDealUpdater updater = new AppDealUpdater(id);
        //2016-11-2-15:09   过期时间改成当前时间
        updater.getPo().setExpireTime(TimeUtils.nowDate());
        dbm.update(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDealExpire(Long id, float newPrice) {

//        注意此处，先clone生成一份新的deal，然后再对旧的deal数据进行操作
        AppDeal deal = dbm.get(AppDeal.class, id);
        Float originPrice = deal.getOriginPrice();

        if (newPrice < originPrice) {
//      新生成关于新价格的deal；配置规则与原来相同
            try {
                AppDeal newDeal = (AppDeal) BeanUtils.cloneBean(deal);

                newDeal.setId(null);
                newDeal.setCreateTime(TimeUtils.nowDate());
                newDeal.setPriceDescription("Rs." + newPrice);
                newDeal.setDealClickCount(0L);
                newDeal.setPresentPrice(newPrice);
                newDeal.setDiscount((int) ((1 - newPrice / originPrice) * 100));

                createAppDealByPriceOff(newDeal);
            } catch (Exception e) {
                System.out.println("updateDealExpire clone bean fail");
            }
        }

        AppDealUpdater updater = new AppDealUpdater(id);
        //2016-11-2-15:09   过期时间改成当前时间
//        updater.getPo().setExpireTime(deal.getCreateTime());
//        2015-11-23-16:16  过期机制修改
//        原始deal过期失效，且不展示
        updater.getPo().setExpireTime(TimeUtils.nowDate());
        updater.getPo().setDisplay(false);
        dbm.update(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeal(Long dealId) {
        dbm.delete(AppDeal.class, dealId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicalDeleteBanner(Long bannerId) {

        AppBannerUpdater appBannerUpdater = new AppBannerUpdater(bannerId);

        appBannerUpdater.getPo().setDeadline(TimeUtils.nowDate());

        dbm.update(appBannerUpdater);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long bannerId) {
        dbm.delete(AppBanner.class, bannerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(Long[] ids) {
        dao.updateBySql("delete from appdeal where id in(?)", Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateBanner(AppBanner banner) {
        dao.save(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeal(AppDeal deal) {
        dao.save(deal);
    }

    @Override
    public void importDeal2Solr(DealModel dm) {
        dealIndexService.createOrUpdate(dm);
    }

    @Override
    public void reimportAllDeals2Solr() {

        ListProcessTask<AppDeal> listAndProcessTask2 = new ListProcessTask<AppDeal>(
                new ILister<AppDeal>() {
                    @Override
                    public PageableResult getData(int page) {
                        return dbm.queryPage(Q_DEALS, page, 500);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcessor<AppDeal>() {
                    @Override
                    public void process(AppDeal o) {
                        DealModel dm = new DealModel(o);
                        importDeal2Solr(dm);
                    }
                }
        );

        try {
            dealIndexService.removeAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        listAndProcessTask2.go();
    }

    @Override
    public AppDealThumb getDealThumbByUidDid(Long userId, Long dealId) {
        List<AppDealThumb> appDealThumbs = dbm.query(Q_THUMB_UIDDID, Arrays.asList(userId, dealId));
        if (appDealThumbs != null && appDealThumbs.size() > 0) {
            return appDealThumbs.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void updateDealThumb(AppDealThumb appDealThumb) {
        dbm.update(appDealThumb);
    }

    @Override
    public void createThumb(AppDealThumb appDealThumb) {
        dbm.create(appDealThumb);
    }

    @Override
    public void createAppComment(AppDealComment appDealComment) {
        dbm.create(appDealComment);
    }

    @Override
    public Long getTotalDealThumb(long dealId) {
        return dbm.querySingle(Q_THUMB_TOTAL, Arrays.asList(dealId));
    }

    @Override
    public PageableResult<AppDealComment> getPageAbleDealComment(Long dealId, int page, int pageSize) {
        return dbm.queryPage(Q_COMMENTS_DEALID, page, pageSize, Arrays.asList(dealId));
    }

}
