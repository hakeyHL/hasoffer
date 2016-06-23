package hasoffer.core.admin.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.HibernateDao;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.utils.excel.ExcelImporter;
import hasoffer.core.utils.excel.ImportCallBack;
import hasoffer.core.utils.excel.ImportConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by lihongde on 2016/6/21 14:03
 */
@Service
@Transactional
public class DealServiceImpl implements IDealService {

    private static final long EXPIRE_TIME_MS = 7 * 24 * 60 * 60 * 1000;

    @Resource
    private HibernateDao dao;

    @Resource
    IDataBaseManager dbm;

    @Resource
    ExcelImporter importer;

    private static final String IMPORT_SQL = "insert into appdeal(website, title, linkUrl, expireTime, imageUrl, createTime, description) values(?, ?, ?, ?, ?, ? ,?)";

    @Override
    public PageableResult<AppDeal> findDealList(int page, int size) {
        return dbm.queryPage("select t from AppDeal t order by createTime desc", page, size);
    }

    @Override
    public Map<String, Object> importExcelFile(MultipartFile multipartFile) throws Exception {
        Map<String, Object> importResult = importer.setImportConfig(new ImportConfig() {
            @Override
            public String validation(Workbook xwb) {
                return null;
            }

            @Override
            public String getImportSQL() {
                return IMPORT_SQL;
            }

            @Override
            public List<Object[]> getImportData(HibernateDao dao, List<Object[]> data) {

                return data;
            }

            @Override
            public ImportCallBack getImportCallBack() {
                return new ImportCallBack() {
                    @Override
                    public Map<String, Object> preOperation(HibernateDao dao, List<Object[]> data) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        int _nullRows = 0;
                        int repeatRows = 0;
                        List<Object[]> dataQueue = new LinkedList<Object[]>();
                        for (int i = 0; i < data.size(); i++) {
                            Object[] tempData = new Object[7];
                            for (int j = 0; j < tempData.length; j++) {
                                //TODO  网站名/deal名称/deal跳转链接为空 记录日志
                                if(StringUtils.isBlank(data.get(i)[0] + "") || StringUtils.isBlank(data.get(i)[1] + "") || StringUtils.isBlank(data.get(i)[2] + "")){
                                    _nullRows ++;
                                }else{
                                    System.arraycopy(data.get(i), 0, tempData, 0, data.get(i).length);
                                }

                                if(StringUtils.isBlank(tempData[3] + "")){
                                    tempData[3] = TimeUtils.after(EXPIRE_TIME_MS);
                                }

                                if(tempData[5] == null || StringUtils.isBlank(tempData[5] + "")){
                                    tempData[5] = new Date(TimeUtils.now());
                                }


                                // TODO 重复元素记录日志
                                for(int k = 0; k < dataQueue.size(); k++){
                                    if(dataQueue.get(k)[2].equals(tempData[2])){
                                        repeatRows ++;
                                        dataQueue.remove(k);
                                    }
                                }
                            }
                            dataQueue.add(tempData);
                        }
                            map.put("_nullRows", _nullRows);
                            map.put("repeatRows", repeatRows);
                            map.put("dataQueue", dataQueue);
                            return map;
                        }

                        @Override
                        public void postOperation (HibernateDao dao, List < Object[]>data){

                        }
                    };

                }
            }

            ).importExcelFile(multipartFile);

        return importResult;
        }

    @Override
    public AppDeal getDealById(Long dealId) {

        return dbm.get(AppDeal.class, dealId);
    }

    @Override
    public AppBanner getBannerByDealId(Long dealId) {
        return dbm.get(AppBanner.class, dealId);
    }

    @Override
    public void delete(Long dealId) {
        dbm.delete(AppDeal.class , dealId);
    }

    @Override
    public void addOrUpdateBanner(AppBanner banner) {
        dbm.create(banner);
    }

    @Override
    public void updateDeal(AppDeal deal) {
        dao.save(deal);
    }
}
