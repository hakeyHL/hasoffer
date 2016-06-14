package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.ThdProductVo;
import hasoffer.base.model.PageModel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.bo.enums.RelateStatus;
import hasoffer.core.bo.enums.RelateType;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.thd.ThdProduct;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.thd.IThdService;
import hasoffer.core.utils.ImageUtil;
import hasoffer.webcommon.helper.PageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date : 2016/2/29
 * Function :
 */
@Controller
@RequestMapping(value = "/thd")
public class ThdController {

    @Resource
    IThdService thdService;

    @Resource
    IProductService productService;
    @Resource
    ProductIndexServiceImpl productIndexService;
    @Resource
    ICmpSkuService cmpSkuService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView thdList(HttpServletRequest request,
                                @RequestParam(defaultValue = "FLIPKART") Website website,
                                @RequestParam(defaultValue = "0") int category3,
                                @RequestParam(defaultValue = "NEW_CMPSKU") RelateType relateType,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size) {

        ModelAndView modelAndView = new ModelAndView("product/thd/list");

        PageableResult pagedResults = null;
        List<ThdProduct> products = null;
        PageModel pageModel = null;

        if (category3 > 0) {
            pagedResults = thdService.getProducts(website, category3, relateType, page, size);
        } else {
            pagedResults = thdService.getProducts(website, relateType, page, size);
        }

        products = pagedResults.getData();
        pageModel = PageHelper.getPageModel(request, pagedResults);

        modelAndView.addObject("website", website);
        modelAndView.addObject("products", getThdProductVos(products));
        modelAndView.addObject("page", pageModel);
        modelAndView.addObject("relateTypes", RelateType.values());
        modelAndView.addObject("relateType", relateType);

        return modelAndView;
    }

    private List<ThdProductVo> getThdProductVos(List<ThdProduct> products) {

        if (ArrayUtils.isNullOrEmpty(products)) {
            return null;
        }

        List<ThdProductVo> thdProductVos = new ArrayList<ThdProductVo>();

        for (ThdProduct thdProduct : products) {

            ThdProductVo thdProductVo = getThdProductVo(thdProduct);
            thdProductVos.add(thdProductVo);

        }

        return thdProductVos;
    }

    private ThdProductVo getThdProductVo(ThdProduct thdProduct) {

        ThdProductVo thdProductVo = new ThdProductVo();

        thdProductVo.setId(thdProduct.getId());
        thdProductVo.setTitle(thdProduct.getTitle());
        thdProductVo.setPrice(thdProduct.getPrice());
        thdProductVo.setImageUrl(ImageUtil.getImage3rdUrl(thdProduct.getImageUrl()));
        thdProductVo.setRelateType(thdProduct.getRelateType());

        long cmpSkuId = thdProduct.getCmpSkuId();
        if (cmpSkuId > 0) {
            PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(cmpSkuId);

            if (ptmCmpSku != null) {
                long productId = ptmCmpSku.getProductId();

                String ptmProductTitle = productService.getProduct(productId).getTitle();

                thdProductVo.setPtmProductTitle(ptmProductTitle);
                thdProductVo.setPtmProductId(productId);
            }
        }

        return thdProductVo;
    }

    @RequestMapping(value = "/relate", method = RequestMethod.POST)
    public ModelAndView relateproducts(HttpServletRequest request) {

        Website website = Website.valueOf(request.getParameter("website"));
        long cateId = Long.valueOf(request.getParameter("category3"));

        if (website == null) {
            throw new InvalidParameterException();
        }

        final int page = 1, size = 500;

        // 取 某网站的 商品列表
        PageableResult<ThdProduct> pagedThdProducts = thdService.getPagedUnrelatedProductsByCategory(website, cateId, page, size);
        List<ThdProduct> thdProducts = pagedThdProducts.getData();

        int ec = 0, epnc = 0, np = 0;

        long pageCount = pagedThdProducts.getTotalPage();

        for (int i = 1; i <= pageCount; i++) {

            if (i > 1) {
                thdProducts = thdService.getUnrelatedProducts(website, page, size);
            }

            for (ThdProduct product : thdProducts) {
                RelateStatus rs = thdService.relate(product);
                switch (rs) {
                    case EXISTS_CMPSKU:
                        ec++;
                        break;
                    case EXISTS_PRODUCT_NO_CMPSKU:
                        epnc++;
                        break;
                    case NO_PRODUCT:
                        np++;
                        break;
                }
            }

        }

        String result = String.format("EXISTS_CMPSKU(%d),EXISTS_PRODUCT_NO_CMPSKU(%d),NO_PRODUCT(%d).", ec, epnc, np);
        ModelAndView mav = new ModelAndView("system/ok");
        mav.addObject("result", result);
        return mav;
    }

}
