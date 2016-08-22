package hasoffer.admin.controller;

import hasoffer.affiliate.affs.flipkart.FlipkartAffiliateProductProcessor;
import hasoffer.affiliate.model.FlipkartSkuInfo;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping(value = "/solr")
public class SolrController {

    private static final String Q_PRODUCT =
            "SELECT t FROM PtmProduct t where t.id > 726884";
    @Resource
    IProductService productService;
    @Resource
    ICategoryService categoryService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuService cmpSkuService;

    @RequestMapping(value = "/product/importbycategory", method = RequestMethod.GET)
    public void importNewAll() {
        final String Q_SKU = "select t from PtmCmpSku t where t.website=?0 and t.categoryId=?1 and t.sourceSid is not null";

        final ProcessCate pc = new ProcessCate();

        ListAndProcessTask2<PtmCmpSku> listAndProcessTask2 = new ListAndProcessTask2<>(
                new IList() {
                    @Override
                    public PageableResult getData(int page) {
                        PageableResult result = dbm.queryPage(Q_SKU, page, 500, Arrays.asList(Website.FLIPKART, pc.getCateId()));
                        System.out.println(String.format("Import Solr: Category[%d], Page[%d/%d].", pc.getCateId(), page, result.getTotalPage()));
                        return result;
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
                    public void process(PtmCmpSku o) {
                        if (StringUtils.isEmpty(o.getSourceSid())) {
                            return;
                        }

                        String brand = o.getBrand();
                        String model = o.getModel();
                        try {

                            if (StringUtils.isEmpty(brand)) {
                                FlipkartAffiliateProductProcessor fapp = new FlipkartAffiliateProductProcessor();
                                FlipkartSkuInfo skuInfo = fapp.getSkuInfo(o.getSourceSid());
                                brand = skuInfo.getProductBrand();
                                model = skuInfo.getModelName();
                                // 更新商品brand和model， solr
                                cmpSkuService.updateCmpSkuBrandModel(o.getId(), brand, model);
                            }

                            long proId = o.getProductId();
                            // update product brand
                            productService.updateProductBrandModel(proId, brand, model);

                            productService.importProduct2Solr2(proId);

                        } catch (Exception e) {
                            System.out.println(String.format("Error : [%s]. Info : [%s]", e.getMessage(), o.getSourceSid()));
                        }
                    }
                }
        );

        listAndProcessTask2.setProcessorCount(10);
        listAndProcessTask2.setQueueMaxSize(200);

        // cate list for each
        List<PtmCategory> cates = categoryService.listCates();
        for (PtmCategory cate : cates) {
            if (cate.getId() <= 5) {
                continue;
            }
            pc.setCateId(cate.getId());
            listAndProcessTask2.go();
        }
    }

    @RequestMapping(value = "/product/reimportnew", method = RequestMethod.GET)
    public void reimportnew() {
        ListAndProcessTask2<PtmProduct> listAndProcessTask2 = new ListAndProcessTask2<>(
                new IList() {
                    @Override
                    public PageableResult getData(int page) {
                        return dbm.queryPage(Q_PRODUCT, page, 2000);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcess<PtmProduct>() {
                    @Override
                    public void process(PtmProduct o) {
                        productService.importProduct2Solr2(o);
                    }
                }
        );

        listAndProcessTask2.go();
    }

    @RequestMapping(value = "/category/reimport", method = RequestMethod.GET)
    public ModelAndView reimportCategory(HttpServletRequest request) {
        categoryService.reimportCategoryIndex();

        ModelAndView mav = new ModelAndView();
        mav.addObject("result", "ok");
        return mav;
    }

    @RequestMapping(value = "/product/updateall", method = RequestMethod.GET)
    public ModelAndView updateall(HttpServletRequest request) {

        Runnable re = new Runnable() {
            @Override
            public void run() {
                productService.reimport2Solr(false);
            }
        };

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(re);

        ModelAndView mav = new ModelAndView();
        mav.addObject("result", "ok");
        return mav;
    }

    @RequestMapping(value = "/product/reimportsolrindexbycategory", method = RequestMethod.GET)
    public
    @ResponseBody
    String reimportsolrindexbycategory(@RequestParam long cateId) {

        List<PtmProduct> products = productService.listProducts(cateId, 1, Integer.MAX_VALUE);
        int size = products.size();
        int count = 0;
        for (PtmProduct product : products) {
            count++;
            productService.importProduct2Solr(product);

            if (count % 10 == 0) {
                System.out.println("[reimportsolrindexbycategory] - " + count + " / " + size);
            }
        }

        return "ok";
    }

    @RequestMapping(value = "/product/reimport", method = RequestMethod.GET)
    public ModelAndView recreatesolrindex(HttpServletRequest request) {

        Runnable re = new Runnable() {
            @Override
            public void run() {
                productService.reimport2Solr(true);
            }
        };

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(re);

        ModelAndView mav = new ModelAndView();
        mav.addObject("result", "ok");
        return mav;
    }

    @RequestMapping(value = "/product/append", method = RequestMethod.GET)
    public ModelAndView append(HttpServletRequest request) {

        Runnable re = new Runnable() {
            @Override
            public void run() {
                productService.append2Solr();
            }
        };

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(re);

        ModelAndView mav = new ModelAndView();
        mav.addObject("result", "ok");
        return mav;
    }

    class ProcessCate {
        long cateId;

        public long getCateId() {
            return cateId;
        }

        public void setCateId(long cateId) {
            this.cateId = cateId;
        }
    }

}
