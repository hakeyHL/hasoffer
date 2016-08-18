package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.ICategoryService;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping(value = "/solr")
public class SolrController {

    @Resource
    IProductService productService;
    @Resource
    ICategoryService categoryService;
    @Resource
    IDataBaseManager dbm;

    @RequestMapping(value = "/product/reimportnew", method = RequestMethod.GET)
    public void reimportnew() {
        ListAndProcessTask2<PtmProduct> listAndProcessTask2 = new ListAndProcessTask2<>(
                new IList() {
                    @Override
                    public PageableResult getData(int page) {
                        return productService.listPagedProducts(page, 2000);
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

}
