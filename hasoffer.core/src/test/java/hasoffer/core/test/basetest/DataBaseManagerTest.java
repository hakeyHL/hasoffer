package hasoffer.core.test.basetest;

import hasoffer.affiliate.affs.flipkart.FlipkartAffiliateProductProcessor;
import hasoffer.affiliate.model.AffiliateProduct;
import hasoffer.base.enums.AppType;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmImage;
import hasoffer.core.persistence.po.ptm.updater.PtmImageUpdater;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IPtmCmpSkuImageService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.user.IDeviceService;
import hasoffer.fetch.model.OriFetchedProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import hasoffer.spider.model.FetchedProduct;
import jodd.io.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/4/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class DataBaseManagerTest {
    //private Logger logger = LoggerFactory.getLogger(DataBaseManagerTest.class);

    @Resource
    IDataBaseManager dbm;

    @Test
    @Transactional
    public void testQueryBySql() {
        String sql = "SELECT count(*) as countNum FROM SrmProductSearchCount psc LEFT JOIN ptmcmpsku sku ON psc.productId = sku.productId WHERE psc.ymd = :ymd AND psc.count > :sum AND sku.website = 'AMAZON' AND sku.`status` <> 'OFFSALE'";
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("ymd","20160813");
        paramsMap.put("sum","5");
        List list = dbm.queryBySql(sql, paramsMap);
        for (Object obj : list) {
            if(obj != null){
                Map<String, Object> temp = (Map<String, Object>) obj;
                System.out.println(temp.get("countNum"));
            }
        }
        sql = "SELECT sku.url,sku.productId,sku.id FROM SrmProductSearchCount psc LEFT JOIN ptmcmpsku sku ON psc.productId = sku.productId WHERE psc.ymd = :ymd AND psc.count > :sum AND sku.website = 'AMAZON' AND sku.`status` <> 'OFFSALE' limit :begin, :end";
        paramsMap.put("begin",0);
        paramsMap.put("end",1000);
        list = dbm.queryBySql(sql, paramsMap);
        for (Object obj : list) {
            if(obj != null){
                Map<String, Object> temp = (Map<String, Object>) obj;
                System.out.println(temp.get("id"));
                System.out.println(temp.get("productId"));
                System.out.println(temp.get("url"));
            }
        }


    }


}
