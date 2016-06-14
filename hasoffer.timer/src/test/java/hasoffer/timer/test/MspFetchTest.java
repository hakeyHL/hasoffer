package hasoffer.timer.test;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.core.persistence.po.msp.MspProductJob;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.IProductService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.model.ListJob;
import hasoffer.fetch.model.PageModel;
import hasoffer.fetch.sites.mysmartprice.MspCategoryProcessor;
import hasoffer.fetch.sites.mysmartprice.MspList2Processor;
import hasoffer.fetch.sites.mysmartprice.MspListProcessor;
import hasoffer.fetch.sites.mysmartprice.NewMspSkuCompareProcessor;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceCategory;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceProduct;
import hasoffer.timer.msp.worker.SaveJobWorker;
import hasoffer.timer.msp.worker.SaveProductWorker;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.TagNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeStringByXPath;

/**
 * Created by chevy on 2015/12/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class MspFetchTest {

    private final static String Q_CATEGORY =
            "SELECT t FROM MspCategory t ORDER BY t.parentId ASC, t.id DESC";
    //暂时更新手机中没有比较的类目
//    private final static String Q_CATEGORY =
//            "SELECT t FROM MspCategory t WHERE t.parentId > 0 AND t.compared = 0 AND t.groupName = 'Mobile Accessories'";
    private final static String Q_PRODUCT_JOB =
            "SELECT t FROM MspProductJob t ORDER BY t.id ASC ";
    private final static String Q_PRODUCT_JOB_BY_CATEGORY =
            "SELECT t FROM MspProductJob t WHERE t.categoryId = ?0 and t.ptmProductId=0";
    private final static String Q_COUNT_PRODUCT =
            "SELECT COUNT(t.id) FROM PtmProduct t WHERE t.categoryId = ?0 ";
    private final static String Q_COUNT_PRODUCTJOB =
            "SELECT COUNT(t.id) FROM MspProductJob t WHERE t.categoryId = ?0 ";

    private final static String Q_CMP_SKU =
            "SELECT t FROM PtmCmpSku t ";

    @Resource
    IMspService mspService;
    @Resource
    IProductService productService;
    @Resource
    ICategoryService categoryService;
    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(MspFetchTest.class);

    @Test
    public void fixUrl() throws Exception {
        List<PtmCmpSku> cmpSkus = dbm.query(Q_CMP_SKU, 1, 500);

        for (PtmCmpSku cmpSku : cmpSkus) {
            String url = cmpSku.getUrl();
            Website website = WebsiteHelper.getWebSite(url);
            System.out.println(website == null ? url : website.name());
        }

    }

    @Test
    public void fetchProducts_byCategory() {
        final List<MspCategory> categories = dbm.query(Q_CATEGORY);
        for (MspCategory category : categories) {
            int proCount = category.getProCount();
            if (proCount == 0) {
                continue;
            }
            long proCount2 = dbm.querySingle(Q_COUNT_PRODUCT, Arrays.asList(category.getPtmCategoryId()));

            long proCount3 = dbm.querySingle(Q_COUNT_PRODUCTJOB, Arrays.asList(category.getId()));

            logger.debug(category.getName() + "\t" + proCount2 + "/" + proCount3 + "/" + proCount);

            if (proCount3 - proCount2 > 20) {
                fetchProducts(category.getId());
            }
        }
    }

    @Test
    public void fetchProducts_category358() {
        fetchProducts(358);
    }

    public void fetchProducts(long category) {
        List<MspProductJob> productJobs = dbm.query(Q_PRODUCT_JOB_BY_CATEGORY, Arrays.asList(category));

        final ConcurrentLinkedQueue<MspProductJob> jobs = new ConcurrentLinkedQueue<MspProductJob>();
        final ConcurrentLinkedQueue<MspProductJob> jobs2 = new ConcurrentLinkedQueue<MspProductJob>();

        AtomicInteger saveCount = new AtomicInteger(0);

        jobs.addAll(productJobs);

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs2, null, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs2, null, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs2, null, saveCount, mspService));

        int count = 0;
        while (true) {
            logger.debug(String.format("queue size : %d(%d).", jobs.size(), jobs2.size()));
            try {
                TimeUnit.SECONDS.sleep(10);
                if (jobs.size() == 0 && jobs2.size() == 0) {
                    count++;
                    if (count >= 5) {
                        break;
                    }
                } else {
                    count = 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            continue;
        }

        es.shutdown();
    }

    @Test
    public void fetchProducts() {
        final int PAGE_SIZE = 500;

        final ConcurrentLinkedQueue<MspProductJob> jobs = new ConcurrentLinkedQueue<MspProductJob>();
        final ConcurrentLinkedQueue<MspProductJob> jobs2 = new ConcurrentLinkedQueue<MspProductJob>();

        final AtomicInteger findCount = new AtomicInteger(0);

        Runnable findWorker = new Runnable() {
            @Override
            public void run() {
                int pageNum = 1;
                PageableResult<MspProductJob> pagedProductJobs = dbm.queryPage(Q_PRODUCT_JOB, pageNum, PAGE_SIZE);

                List<MspProductJob> productJobs = pagedProductJobs.getData();

                int pageCount = (int) pagedProductJobs.getTotalPage();
                while (pageNum <= pageCount) {

                    if (jobs.size() > 200) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    if (pageNum > 1) {
                        productJobs = dbm.query(Q_PRODUCT_JOB, ++pageNum, PAGE_SIZE);
                    }

                    if (!ArrayUtils.isNullOrEmpty(productJobs)) {
                        logger.debug(String.format("add product jobs : %d, total : %d, queue size : %d.",
                                productJobs.size(),
                                findCount.addAndGet(productJobs.size()),
                                jobs.size()));
                        jobs.addAll(productJobs);
                        pageNum++;
                    }
                }
            }
        };

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(findWorker);

        AtomicInteger saveCount = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        }
        es.execute(new SaveProductWorker(jobs2, null, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs2, null, saveCount, mspService));
        es.execute(new SaveProductWorker(jobs2, null, saveCount, mspService));

        while (true) {
            logger.debug(String.format("queue size : %d(%d).", jobs.size(), jobs2.size()));
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            continue;
        }
    }

    @Test
    public void fetchProduct() {
        String Q_1 = "select t from MspProductJob t where t.id=44070 ";

        PageableResult<MspProductJob> pagedProductJobs = dbm.queryPage(Q_1, 1, 1);
        List<MspProductJob> productJobs = pagedProductJobs.getData();
        MspProductJob productJob = productJobs.get(0);
        NewMspSkuCompareProcessor compareProcessor = new NewMspSkuCompareProcessor();
        try {
            MySmartPriceProduct mspp = compareProcessor.parse(productJob.getUrl());
            mspService.saveProduct(productJob.getId(), productJob.getCategoryId(), mspp);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Test
    public void fetchProductList() {
        final ConcurrentLinkedQueue<ListJob> listQueue = new ConcurrentLinkedQueue<ListJob>();
        final List<MspCategory> categories = dbm.query(Q_CATEGORY);

        List<String> existingProIdList = dbm.query("select t.sourceId from MspProductJob t");
        final Set<String> existingProIds = new HashSet<String>(existingProIdList);

        final Runnable list = new Runnable() {
            @Override
            public void run() {
                MspListProcessor listProcessor = new MspListProcessor();
                MspCategory cate = null;
                for (int i = 0, size = categories.size(); i < size; i++) {
                    cate = categories.get(i);
                    if (cate.getParentId() == 0) {
                        continue;
                    }

                    if (listQueue.size() > 200) {
                        try {
                            i--;
                            TimeUnit.SECONDS.sleep(10);
                            System.out.println("list queue has more than 100 jobs. go to sleep!");
                            continue;
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    PageModel pageModel = listProcessor.getPageModel(cate.getUrl());
                    int pageCount = pageModel.getPageCount();
                    for (int p = 1; p < pageCount; p++) {
                        logger.debug("category : " + cate.getId() + "-" + cate.getName());
                        String url = pageModel.getUrlTemplate().replace("{page}", String.valueOf(p));
                        if (p == 1) {
                            url = cate.getUrl();
                        }
                        ListJob listJob = new ListJob(null, url, String.valueOf(cate.getId()));
                        listQueue.add(listJob);
                    }
                }
            }
        };

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(list);
        es.execute(new SaveJobWorker(existingProIds, listQueue, mspService));
        es.execute(new SaveJobWorker(existingProIds, listQueue, mspService));

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(8);
                System.out.println("listQueue size : " + listQueue.size());
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    @Test
    // 保存类目
    public void fetchCategories() {
        MspCategoryProcessor mcp = new MspCategoryProcessor();
        List<MySmartPriceCategory> categories = mcp.parseCategories();

        for (MySmartPriceCategory category : categories) {
            saveCategory(category, 0L);
        }
    }

    // 保存类目
    private void saveCategory(MySmartPriceCategory category, long parentId) {
        logger.info(category.getName());
        MspCategory catePo = mspService.saveCategory(parentId, category.getName(), category.getUrl(),
                category.getImageUrl(), category.getGroupName());

        if (ArrayUtils.isNullOrEmpty(category.getSubCategories())) {
            return;
        }

        List<MySmartPriceCategory> subCates = category.getSubCategories();
        for (MySmartPriceCategory subCate : subCates) {
            saveCategory(subCate, catePo.getId());
        }
    }

    @Test
    public void updateCategories_0323(){

        final List<MspCategory> categories = dbm.query(Q_CATEGORY);

        int count = 0;

        for (MspCategory category : categories) {
            logger.debug(category.getName());
            if (category.getParentId() == 0) {
                continue;
            }

            long id = category.getId();
            String url = category.getUrl();
            try {
                TagNode root = HtmlUtils.getUrlRootTagNode(url);
                String proCount = getSubNodeStringByXPath(root, "//div[@class='list-hdr__prdct-cnt']/b[@class='js-prdct-cnt__totl']", null);
                if (NumberUtils.isDigits(proCount)) {
                    count++;
//					logger.debug(proCount);
                    mspService.updateCategory(id, Integer.parseInt(proCount));
                    logger.debug(category.getProCount() + "-" + proCount);
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }

        String result = "MspCategoryUpdateTask update " + count + " categories.";

    }

    @Test
    public void updateCategories() {
        MspList2Processor list2Processor = new MspList2Processor();
        final List<MspCategory> categories = dbm.query(Q_CATEGORY);

        for (MspCategory category : categories) {
            logger.debug(category.getName());
            if (category.getParentId() == 0) {
                continue;
            }

            long id = category.getId();
            String url = category.getUrl();
            try {
                TagNode root = HtmlUtils.getUrlRootTagNode(url);
                String proCountStr = getSubNodeStringByXPath(root, "//div[@class='list-hdr__prdct-cnt']/b[@class='js-prdct-cnt__totl']", null);
                int proCount = 0;
                if (StringUtils.isEmpty(proCountStr)) {
                    proCount = list2Processor.getProductCount(url);
                }

                if (NumberUtils.isDigits(proCountStr)) {
                    proCount = Integer.parseInt(proCountStr);
                }

                mspService.updateCategory(id, proCount);
                logger.debug(category.getProCount() + "-" + proCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 该方法用来测试msp上，title为Mobile Accessories且没有比较列表的商品数量，由于getProductCount得到的结果又误差，需要修正
     */
    @Test
    public void testFetchProductCount() {

        MspList2Processor list2Processor = new MspList2Processor();

        String url = "http://www.mysmartprice.com/accessories_nc/cases-covers";

        try {
            TagNode root = HtmlUtils.getUrlRootTagNode(url);
//          div[@class='list-hdr__prdct-cnt']/b[@class='js-prdct-cnt__totl']
//            /html/body/div[4]/div[1]/div[2]/div[2]/div[1]/div[1]/div[2]/span[1]
            String proCountStr = getSubNodeStringByXPath(root, "/body/div[4]/div[1]/div[2]/div[2]/div[1]/div[1]/div[2]/span[1]", null);
            int proCount = 0;
            if (StringUtils.isEmpty(proCountStr)) {
                proCount = list2Processor.getProductCount(url);
            }

            if (NumberUtils.isDigits(proCountStr)) {
                proCount = Integer.parseInt(proCountStr);
            }

            System.out.println("有" + proCount + "个商品");

        } catch (HttpFetchException e) {
            e.printStackTrace();
        } catch (ContentParseException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void convertCategoies() {
        final List<MspCategory> mspCategories = dbm.query(Q_CATEGORY);

        Map<Long, PtmCategory> topCateMap = new HashMap<Long, PtmCategory>();
        Map<Long, Map<String, Long>> groupMap = new HashMap<Long, Map<String, Long>>();

        for (MspCategory mspCategory : mspCategories) {

            long parentId = 0L;
            if (mspCategory.getParentId() > 0) {
                PtmCategory parent = topCateMap.get(mspCategory.getParentId());
                parentId = parent == null ? -1L : parent.getId();
            }

            PtmCategory ptmCategory = null;
            if (parentId == 0L) {
                // 1级目录
                ptmCategory = categoryService.createCategory(parentId, mspCategory.getName(), mspCategory.getImageUrl());
                topCateMap.put(mspCategory.getId(), ptmCategory);
            } else {
                //
                String groupName = mspCategory.getGroupName();
                Map<String, Long> groupIdMap = groupMap.get(parentId);
                if (groupIdMap == null) {
                    groupIdMap = new HashMap<String, Long>();
                    groupMap.put(parentId, groupIdMap);
                }

                Long groupId = groupIdMap.get(groupName);
                if (groupId == null) {
                    PtmCategory groupCate = categoryService.createCategory(parentId, groupName, "");
                    groupIdMap.put(groupName, groupCate.getId());
                    groupId = groupCate.getId();
                }

                ptmCategory = categoryService.createCategory(groupId, mspCategory.getName(), mspCategory.getImageUrl());
            }

            mspService.relateCategory(mspCategory.getId(), ptmCategory);
        }
    }

}
