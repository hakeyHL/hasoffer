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
import hasoffer.core.persistence.po.app.updater.AppDealUpdater;
import hasoffer.core.product.solr.DealIndexServiceImpl;
import hasoffer.core.product.solr.DealModel;
import hasoffer.core.task.ListProcessTask;
import hasoffer.core.task.worker.ILister;
import hasoffer.core.task.worker.IProcessor;
import hasoffer.core.utils.excel.ExcelImporter;
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
    private static final String IMPORT_SQL = "insert into appdeal(website, title, linkUrl, expireTime, priceDescription ,description, createTime,  push ,display ,imageUrl,discount,dealCategoryId,dealClickCount,appdealSource,ptmcmpskuid) values(?,?, ?, ?, ?, ? ,?, ?, ?, ?,?,?,?,'MANUAL_INPUT',0)";

    private static final String Q_DEALS = "SELECT t FROM AppDeal t";

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

        String querySql = "select t from AppDeal t WHERE t.appdealSource = 'AppDealSourceType' order by t." + orderByField + " desc";

        if (type == 1) {
            querySql = querySql.replace("AppDealSourceType", "MANUAL_INPUT");
            return dbm.queryPage(querySql, page, size);
        } else if (type == 2) {
            querySql = querySql.replace("AppDealSourceType", "PRICE_OFF");
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
        for (int i = 0; i < maps.size() - 1; i++) {
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
                    continue;
                }
            } catch (Exception e) {
                failNum++;
                importResult.put("errorMessage", "未识别的site / not recognized website type !");
                continue;
                //not a website
            }
            //2. deal title
            String dealTitle = stringStringMap.get("1");
            if (!StringUtils.isEmpty(dealTitle)) {
                appDeal.setTitle(dealTitle);
            } else {
                emptyLinkNum++;
                continue;
            }

            //3. link
            String dealLink = stringStringMap.get("2");
            if (!StringUtils.isEmpty(dealLink)) {
                if (linkList.contains(dealLink)) {
                    repeatLinkNum++;
                } else {
                    linkList.add(dealLink);
                    appDeal.setLinkUrl(dealLink.trim());
                }
            } else {
                emptyLinkNum++;
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
                dbm.batchSave(Arrays.asList(appDeal));
            } catch (Exception e) {
                failNum++;
                importResult.put("errorMessage", "插入失败 / insert fail  : " + appDeal.getTitle() + "  msg : " + e.getMessage());
                continue;
            }
            System.out.println("over ");
        }
        importResult.put("totalRows", maps.size() - 1);
        importResult.put("successRows", maps.size() - 1 - failNum);
        importResult.put("failRows", failNum);
        importResult.put("_nullRows", emptyLinkNum);
        importResult.put("repeatRows", repeatLinkNum);
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
    public AppBanner getBannerByDealId(Long dealId) {
        return (AppBanner) dbm.querySingle("SELECT t FROM AppBanner t WHERE t.sourceId = ?0 ", Arrays.asList(dealId.toString()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDealExpire(Long id) {

        AppDeal deal = dbm.get(AppDeal.class, id);

        AppDealUpdater updater = new AppDealUpdater(id);
        updater.getPo().setExpireTime(deal.getCreateTime());

        dbm.update(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeal(Long dealId) {
        dbm.delete(AppDeal.class, dealId);
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
}
