package hasoffer.core.system.impl;

import hasoffer.base.enums.AppType;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.bo.product.Banners;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppVersion;
import hasoffer.core.persistence.po.app.AppWebsite;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.urm.urmUser;
import hasoffer.core.system.IAppService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2015/12/30.
 */
@Service
public class AppServiceImpl implements IAppService {

    private static final String Q_APP_VERSION =
            "SELECT t FROM AppVersion t " +
                    " WHERE t.appType = ?0 " +
                    " ORDER BY t.publishTime DESC";

    private static final String Q_APP_WEBSITE =
            "SELECT t FROM AppWebsite t " +
                    " WHERE t.appshow = ?0 ";

    private static final String Q_APP_ORDERS =
            "SELECT t FROM OrderStatsAnalysisPO t " +
                    " WHERE t.userId = ?0 ";

    private static final String Q_APP_GETUSER =
            "SELECT t FROM urmUser t " +
                    " WHERE t.userToken = ?0 ";

    private static final String Q_APP_ORDER =
            "SELECT t FROM OrderStatsAnalysisPO t " +
                    " WHERE t.orderId = ?0 and t.userId=?1";

    private static final String Q_APP_CATEGORY =
            "SELECT t FROM PtmCategory t " +
                    " order by level ASC,rank ASC";

    private static final String Q_APP_GETUSERBYTHIRDID =
            "SELECT t FROM urmUser t " +
                    " where t.thirdId=?0";

    private  String Q_APP_GETPRODUCTS =
            "SELECT t FROM PtmProduct t " +
                    " where 1=1 and ";

    private static final String Q_APP_GETDEALS =
            "SELECT t FROM AppDeal t ";

    private static final String Q_APP_GETBANNERS =
            " SELECT t from AppBanner t ORDER BY id desc";
    @Resource
    IDataBaseManager dbm;

    @Override
    public AppVersion getLatestVersion(AppType appType) {
//        return dbm.get(AppVersion.class, 3L);
        List<AppVersion> versions = dbm.query(Q_APP_VERSION, Arrays.asList(appType));

        return ArrayUtils.hasObjs(versions) ? versions.get(0) : null;
    }

    @Override
    public List<AppWebsite> getWebsites(boolean appshow) {
        return dbm.query(Q_APP_WEBSITE, Arrays.asList(appshow));
    }

    @Override
    public List<OrderStatsAnalysisPO> getBackDetails(String userToken) {
        List li=new ArrayList();
        li.add(userToken);
        return dbm.query(Q_APP_ORDERS,li);
    }

    @Override
    public urmUser getUserByUserToken(String userToken) {
        List li=new ArrayList();
        li.add(userToken);
        urmUser user=dbm.querySingle(Q_APP_GETUSER, li);
        return user;
    }

    @Override
    public OrderStatsAnalysisPO getOrderDetail(String orderId,String userId) {
        List li=new ArrayList();
        li.add(orderId);
        li.add(userId);
        return dbm.querySingle(Q_APP_ORDER,li);
    }

    @Override
    public PageableResult getDeals(Long page,Long pageSize) {
        if(pageSize==0){
            pageSize=Long.valueOf(20);
        }
        return dbm.queryPage(Q_APP_GETDEALS, page.intValue(), pageSize.intValue());
    }

    @Override
    public List<PtmCategory> getCategory() {
      return   dbm.query(Q_APP_CATEGORY);
    }

    @Override
    public urmUser getUserById(String thirdId) {
        List li=Arrays.asList(thirdId);
        return dbm.querySingle(Q_APP_GETUSERBYTHIRDID,li);
    }

    @Override
    public List getProductByCriteria(SearchCriteria criteria) {
        StringBuilder sb=new StringBuilder();
        int i=0;
        String categoryId=criteria.getCategoryId();
        if(StringUtils.isNotBlank(categoryId)){
            sb.append(" categoryId = ?"+i+"");
            i++;
        }
        int comment=criteria.getComment();
        if(comment==0){
            sb.append(" order by comment desc ");
        }else{
            sb.append(" order by comment asc ");
        }
        String keyword=criteria.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            sb.append(" title like %"+i+"%");
            i++;
        }
        Long maxPrice=criteria.getMaxPrice();
        Long minPrice=criteria.getMinPrice();

        Long page=criteria.getPage();
        Long pageSize=criteria.getPageSize();


        Q_APP_GETPRODUCTS=Q_APP_GETPRODUCTS+"ee";
        return null;
    }

    @Override
    public int addUser(urmUser user) {
        List li=new ArrayList();
        li.add(user);
        return dbm.batchSave(li);
    }

    @Override
    public void updateUserInfo(urmUser uUser) {
        List li=new ArrayList();
        li.add(uUser);
         dbm.update(li);
    }

    @Override
    public List<AppBanner> getBanners() {
       return  dbm.query(Q_APP_GETBANNERS);
    }
}
