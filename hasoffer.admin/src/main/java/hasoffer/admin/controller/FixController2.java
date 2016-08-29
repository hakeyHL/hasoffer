package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.ptm.updater.PtmProductUpdater;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import hasoffer.fetch.helper.WebsiteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date : 2016/3/25
 * Function : 修复商品数据
 * 目前以手机类目开始，fix的项目有：cleanUrl更新、brand标记、sku非相同品牌的删除(修复完成后，更新solr)
 * 1cleanUrl-/fix2/clean_url_sku?cateId=5
 * 2brand1-/fix2/tag_brand?cateId=5
 */
@Controller
@RequestMapping(value = "/fix2")
public class FixController2 {

    private static Logger logger = LoggerFactory.getLogger(FixController2.class);

    @Resource
    IProductService productService;
    @Resource
    ICmpSkuService cmpSkuService;

    /**
     * 根据sku的品牌标记品牌
     * 规则：查询sku的品牌，如果都相同，同时商品的品牌为空，则取该品牌作为商品的品牌
     *
     * @param cateId
     */
    @RequestMapping(value = "/tag_brand", method = RequestMethod.GET)
    @ResponseBody
    public void tag_brand(@RequestParam final long cateId) {
        ListAndProcessTask2<PtmProduct> productListAndProcessTask2 = new ListAndProcessTask2<>(
                new IList() {
                    @Override
                    public PageableResult getData(int page) {
                        return productService.listPagedProducts(cateId, page, 1000);
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
                        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(o.getId());

                        if (ArrayUtils.hasObjs(cmpSkus)) {
                            String skuBrand = "";
                            for (PtmCmpSku cmpSku : cmpSkus) {
                                String skuBrand2 = cmpSku.getBrand();
                                if (!StringUtils.isEmpty(skuBrand2)) {
                                    skuBrand2 = skuBrand2.trim();

                                    if (StringUtils.isEmpty(skuBrand)) {
                                        skuBrand = skuBrand2;
                                        continue;
                                    } else {
                                        if (!skuBrand.equalsIgnoreCase(skuBrand2)) {
                                            print(o.getId() + "-diff sku brand," + skuBrand + "," + skuBrand2);
                                            return;
                                        }
                                    }
                                }
                            }

                            if (StringUtils.isEmpty(skuBrand)) {
                                print(o.getId() + "\t," + o.getBrand() + ",sku brand is null");
                                return;
                            }

                            String proBrand = o.getBrand();
                            if (!StringUtils.isEmpty(proBrand)) {
                                proBrand = proBrand.trim();
                                if (skuBrand.equalsIgnoreCase(proBrand)) {
                                    return;
                                } else {
                                    print(o.getId() + "," + o.getBrand() + "," + skuBrand + ",\tbrand diff!!!");
                                    return;
                                }
                            } else {
                                PtmProductUpdater ptmProductUpdater = new PtmProductUpdater(o.getId());
                                ptmProductUpdater.getPo().setBrand(skuBrand);
                                productService.updateProduct(ptmProductUpdater);
                            }

                        } else {
                            print(o.getId() + "\t no skus.");
                        }
                    }
                }
        );

        productListAndProcessTask2.setProcessorCount(10);
        productListAndProcessTask2.setQueueMaxSize(1500);

        productListAndProcessTask2.go();
    }

    /**
     * 1-将sku中
     *
     * @param cateId
     */
    @RequestMapping(value = "/clean_url_sku", method = RequestMethod.GET)
    @ResponseBody
    public void clean_url_sku(@RequestParam final long cateId) {

        final AtomicInteger delCount = new AtomicInteger(0);

        ListAndProcessTask2<PtmProduct> productListAndProcessTask2 = new ListAndProcessTask2<>(
                new IList() {
                    @Override
                    public PageableResult getData(int page) {
                        return productService.listPagedProducts(cateId, page, 1000);
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
                        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(o.getId());

                        for (PtmCmpSku cmpSku : cmpSkus) {
                            Set<String> urlSet = new HashSet<>();
                            boolean update = false;

                            PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(cmpSku.getId());
                            if (cmpSku.getCategoryId() == null || cmpSku.getCategoryId() != 5) {
                                update = true;
                                ptmCmpSkuUpdater.getPo().setCategoryId(5L);
                            }

                            String cleanUrl = WebsiteHelper.getCleanUrl(cmpSku.getWebsite(), cmpSku.getUrl());
                            if (!cleanUrl.equalsIgnoreCase(cmpSku.getUrl())) {
                                update = true;
                                print(cmpSku.getUrl() + "\t" + cleanUrl);
                                ptmCmpSkuUpdater.getPo().setUrl(cleanUrl);
//                                cmpSkuService.updateCmpSku(cmpSku.getId(), cleanUrl, cmpSku.getColor(), cmpSku.getSize(), cmpSku.getPrice());
                            }

                            if (!urlSet.contains(cleanUrl)) {
                                urlSet.add(cleanUrl);
                                if (update) {
                                    cmpSkuService.updateCmpSku(ptmCmpSkuUpdater);
                                }
                            } else {
                                print("delete sku");
                                cmpSkuService.deleteCmpSku(cmpSku.getId());
                                delCount.addAndGet(1);
                            }
                        }
                    }
                }
        );

        productListAndProcessTask2.setProcessorCount(10);
        productListAndProcessTask2.setQueueMaxSize(1500);

        productListAndProcessTask2.go();
        print(String.format("delete sku count : %d", delCount.get()));

    }

    private void print(String str) {
        System.out.println(str);
    }

}