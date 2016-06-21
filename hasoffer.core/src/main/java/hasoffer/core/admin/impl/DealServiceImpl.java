package hasoffer.core.admin.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.HibernateDao;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.utils.excel.ExcelImporter;
import hasoffer.core.utils.excel.ImportCallBack;
import hasoffer.core.utils.excel.ImportConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by lihongde on 2016/6/21 14:03
 */
public class DealServiceImpl implements IDealService {


    @Resource
    private HibernateDao dao;

    @Resource
    ExcelImporter importer;

    private static final String IMPORT_SQL = "insert into appdeal(createTime, description, expireTime, imageUrl, linkUrl, title, website) values(?, ?, ?, ?, ?, ? ,?)";

    @Override
    public PageableResult<AppDeal> findDealList(int page, int size) {
        String sql = "select * from appdeal";
        return dao.findPageBySql(sql, page, size);
    }

    @Override
    public Map<String, Object> importExcelFile(MultipartFile multipartFile, String realPath) throws Exception {
        importer.setImportConfig(new ImportConfig() {
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
            List<Object[]> dataQueue = new LinkedList<Object[]>();
            for (Object[] temp : data) {
                Object[] tempData = new Object[66];
                for (int i = 0; i < tempData.length; i++) {
                    if (i==16 || i == 17 || i == 63 || i == 65) {
                        tempData[i] = StringUtils.isBlank(temp[i] + "") ? null : temp[i];
                    } else if ((i >= 18 && i <= 31) || (i >= 33 && i <= 34) || (i >= 43 && i <= 45)) {
                        tempData[i] = StringUtils.isBlank(temp[i] + "") ? null : Double.parseDouble(temp[i] + "");
                    } else {
                        tempData[i] = temp[i];
                    }
                }
                dataQueue.add(tempData);
            }
            return dataQueue;
        }

        @Override
        public ImportCallBack getImportCallBack() {
            return new ImportCallBack() {
                @Override
                public void preOperation(HibernateDao dao, List<Object[]> data) {

                }

                @Override
                public void postOperation(HibernateDao dao, List<Object[]> data) {

                }
            };
        }
    }).importExcelFile(multipartFile, realPath);

        return null;
    }
}
