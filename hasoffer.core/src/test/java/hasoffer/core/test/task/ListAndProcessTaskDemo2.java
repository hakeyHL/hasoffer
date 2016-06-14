package hasoffer.core.test.task;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuIndex;
import hasoffer.core.persistence.mongo.SummaryProduct;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import hasoffer.fetch.sites.snapdeal.SnapdealHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Date : 2016/5/3
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ListAndProcessTaskDemo2 {

    @Resource
    IMongoDbManager mdm;
    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuService cmpSkuService;
    private Logger logger = LoggerFactory.getLogger(ListAndProcessTaskDemo2.class);

    @Test
    public void f() {

        ListAndProcessTask2 task = new ListAndProcessTask2(
                new IList() {

                    private int pageSize = 2000;
                    private String sql = "select t from PtmCmpSku t where t.website='SNAPDEAL' ";

                    @Override
                    public PageableResult getData(int page) {
                        return dbm.queryPage(sql, page, pageSize);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcess<PtmCmpSku>() {
                    @Override
                    public void process(PtmCmpSku cmpSku) {

                        SummaryProduct sp = mdm.queryOne(SummaryProduct.class, cmpSku.getId());

                        String title = cmpSku.getTitle();
                        if (sp != null) {
                            if (StringUtils.isEmpty(sp.getTitle())) {
                                title = sp.getTitle();
                            }
                        }

                        // clean sku url
                        String oriUrl = cmpSku.getOriUrl();

                        String url = SnapdealHelper.getCleanUrl(oriUrl);
                        String dl = SnapdealHelper.getDeeplink(oriUrl);

                        String sourcePid = "";
                        String sourceSid = SnapdealHelper.getSkuIdByUrl(url);

                        if (StringUtils.isEmpty(sourceSid)) {
                            return;
                        }

                        PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(cmpSku.getId());

                        boolean doUpdate = false;
                        if (!url.equals(cmpSku.getUrl())) {
                            updater.getPo().setUrl(url);
                            doUpdate = true;
                        }
                        if (!dl.equals(cmpSku.getDeeplink())) {
                            updater.getPo().setDeeplink(dl);
                            doUpdate = true;
                        }
                        if (!sourcePid.equals(cmpSku.getSourcePid())) {
                            updater.getPo().setSourcePid(sourcePid);
                            doUpdate = true;
                        }

                        if (!sourceSid.equals(cmpSku.getSourceSid())) {
                            updater.getPo().setSourceSid(sourceSid);
                            doUpdate = true;
                        }

                        if (doUpdate) {
                            logger.debug(String.format("update : url[%s], dl[%s], sourcePid[%s], sourceSid[%s].", url, dl, sourcePid, sourceSid));
                            cmpSkuService.updateCmpSku(updater);
                        }

                        PtmCmpSkuIndex index = new PtmCmpSkuIndex(cmpSku.getId(), cmpSku.getProductId(), cmpSku.getWebsite(), sourcePid, sourceSid,
                                title, title, cmpSku.getPrice(), url);

                        mdm.save(index);
                        System.out.println("create index for cmpsku : " + index.getId());
//                System.out.println(cmpSku.getUrl() + "\n" + WebsiteHelper.getUrlWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), 0));
                    }
                });
        task.go();
    }


}
