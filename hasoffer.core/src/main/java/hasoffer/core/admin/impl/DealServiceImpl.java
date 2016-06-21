package hasoffer.core.admin.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.HibernateDao;
import hasoffer.core.persistence.po.app.AppDeal;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by lihongde on 2016/6/21 14:03
 */
public class DealServiceImpl implements IDealService {

    @Resource
    private HibernateDao dao;

    @Override
    public PageableResult<AppDeal> findDealList(int page, int size) {
        String sql = "select * from appdeal";
        return dao.findPageBySql(sql, page, size);
    }

    @Override
    public Map<String, Object> importExcelFile(MultipartFile multipartFile) {
        return null;
    }
}
