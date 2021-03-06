package hasoffer.admin.controller;

import hasoffer.admin.common.CategoryHelper;
import hasoffer.admin.controller.vo.CategoryVo;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.exception.CategoryDeleteException;
import hasoffer.core.product.solr.ProductIndex2ServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created on 2015/12/24.
 */
@Controller
@RequestMapping(value = "/cate")
public class CategoryController {

    @Resource
    ICategoryService categoryService;
    @Resource
    IProductService productService;
    @Resource
    ProductIndex2ServiceImpl productIndex2Service;

    private Pattern PATTERN_IN_WORD = Pattern.compile("[^0-9a-zA-Z\\-]");

    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public ModelAndView catelist(HttpServletRequest request,
                                 @RequestParam(defaultValue = "0") int c1,
                                 @RequestParam(defaultValue = "0") int c2) {
        ModelAndView mav = new ModelAndView("product/category");

        List<PtmCategory> routeCates = null;

        List<PtmCategory> categories = categoryService.listSubCategories(0L);
        List<CategoryVo> c1s = CategoryHelper.getCategoryVos(categories);
        statProductCount(c1s);
        mav.addObject("c1s", c1s);

        String keyword = "";
        if (c1 > 0) {
            List<PtmCategory> categories2 = categoryService.listSubCategories(Long.valueOf(c1));
            List<CategoryVo> c2s = CategoryHelper.getCategoryVos(categories2);
            statProductCount(c2s);
            mav.addObject("c2s", c2s);

            if (c2 > 0) {
                List<PtmCategory> categories3 = categoryService.listSubCategories(Long.valueOf(c2));
                List<CategoryVo> c3s = CategoryHelper.getCategoryVos(categories3);
                statProductCount(c3s);
                mav.addObject("c3s", c3s);
            }

            routeCates = categoryService.getRouterCategoryList(c2 > 0 ? c2 : c1);
            keyword = routeCates.get(routeCates.size() - 1).getKeyword();
        }

        mav.addObject("sc1", c1);
        mav.addObject("sc2", c2);
        mav.addObject("routeCates", routeCates);
        mav.addObject("keyword", keyword);
        return mav;
    }

    /**
     * @param cates
     */
    private void statProductCount(List<CategoryVo> cates) {

        for (CategoryVo cate : cates) {
            SearchCriteria sc = new SearchCriteria();
            sc.setCategoryId(String.valueOf(cate.getId()));
            sc.setLevel(cate.getLevel());
            sc.setPage(1);
            sc.setPageSize(1);

            PageableResult<ProductModel2> pros = productIndex2Service.searchProducts(sc);
            cate.setProductCount(pros.getNumFund());
        }
    }

    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public ModelAndView cateDetail(@PathVariable long id) {

        ModelAndView mav = new ModelAndView("product/catedetail");

        List<PtmCategory> categories = categoryService.getRouterCategoryList(id);
        List<CategoryVo> categoryVos = CategoryHelper.getCategoryVos(categories);
        statProductCount(categoryVos);

        mav.addObject("cates", categoryVos);
        mav.addObject("currentCate", categoryVos.get(categoryVos.size() - 1));

        return mav;
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public ModelAndView createCategory(@RequestParam long parentId,
                                       @RequestParam String name) {
        categoryService.createCategory(parentId, name, "");

        ModelAndView mav = new ModelAndView();
        mav.addObject("reault", "ok");
        return mav;
    }

    @RequestMapping(value = "/moveProductsToNewCategory", method = RequestMethod.POST)
    public ModelAndView moveProductsToNewCategory(HttpServletRequest request) {

        String category1Str = request.getParameter("category1");
        String category2Str = request.getParameter("category2");
        String category3Str = request.getParameter("category3");

        long targetCateId = 0L;
        if (!StringUtils.isEmpty(category3Str) && Long.valueOf(category3Str) > 0) {
            targetCateId = Long.valueOf(category3Str);
        } else if (!StringUtils.isEmpty(category2Str) && Long.valueOf(category2Str) > 0) {
            targetCateId = Long.valueOf(category2Str);
        } else if (!StringUtils.isEmpty(category1Str) && Long.valueOf(category1Str) > 0) {
            targetCateId = Long.valueOf(category1Str);
        }

        if (targetCateId <= 0) {
            return new ModelAndView("system/error");
        }

        String currentCateIdStr = request.getParameter("currentCateId");
        moveProducts(Long.valueOf(currentCateIdStr), targetCateId);

        return new ModelAndView("redirect:/cate/detail/" + currentCateIdStr);
    }

    private void moveProducts(Long sourceCate, Long targetCate) {
        final int page = 1, size = 500;
        PageableResult<PtmProduct> pagedProducts = productService.listPagedProducts(sourceCate, page, size);
        long tPage = pagedProducts.getTotalPage();

        List<PtmProduct> products = pagedProducts.getData();
        for (int i = 0; i < tPage; i++) {

            if (i > 0) {
                products = productService.listProducts(sourceCate, page, size);
            }

            if (ArrayUtils.hasObjs(products)) {
                for (PtmProduct product : products) {
                    productService.updateProductCategory(product, targetCate);
                }
            }
        }
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public ModelAndView delete(@PathVariable long id) throws CategoryDeleteException {

        // 判断该类目是否可以删除
        // 可以删除的条件：该类目没有子类目，该类目下没有商品

        List<PtmCategory> subCates = categoryService.listSubCategories(id);

        if (ArrayUtils.hasObjs(subCates)) {
            throw new CategoryDeleteException("子类不为空");
        }

        List<PtmCategory> routerCates = categoryService.getRouterCategoryList(id);

        categoryService.deleteCategory(id);

        String redirectUri = "/cate/main";
        int size = routerCates.size();
        if (size > 1) {
            redirectUri += "?c1=" + routerCates.get(0).getId();
            if (size > 2) {
                redirectUri += "&c2=" + routerCates.get(1).getId();
            }
        }

        ModelAndView mav = new ModelAndView("redirect:" + redirectUri);
        return mav;
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
    public ModelAndView cateUpdate(@PathVariable long id,
                                   @RequestParam(defaultValue = "") String categoryName,
                                   @RequestParam(defaultValue = "") String categoryTag) {
        ModelAndView mav = new ModelAndView("redirect:/cate/detail/" + id);

        boolean reimport2solr = false;

        if (!StringUtils.isEmpty(categoryName)) {
            categoryService.updateCategoryName(id, categoryName);
            reimport2solr = true;
        }

        if (!StringUtils.isEmpty(categoryTag)) {
            if (categoryTag.equalsIgnoreCase("000000")) {
                categoryTag = "";
            }
            categoryService.updateCategoryKeyword(id, categoryTag);
            reimport2solr = true;
        }

        if (reimport2solr) {
            productService.importProduct2SolrByCategory(id);
        }

        return mav;
    }

    @RequestMapping(value = "/updatekeyword", method = RequestMethod.GET)
    public ModelAndView updatekeyword(HttpServletRequest request,
                                      @RequestParam long cateId,
                                      @RequestParam String key) {
        ModelAndView mav = new ModelAndView("product/category");

        categoryService.updateCategoryKeyword(cateId, key);

        return mav;
    }

    @RequestMapping(value = "/list/{parentId}", method = RequestMethod.GET)
    public ModelAndView catelist(HttpServletRequest request,
                                 @PathVariable int parentId) {

        List<PtmCategory> categories = categoryService.listSubCategories(Long.valueOf(parentId));

        List<CategoryVo> categoryVos = CategoryHelper.getCategoryVos(categories);

        ModelAndView mav = new ModelAndView();
        mav.addObject("categories", categoryVos);

        return mav;
    }

    @RequestMapping(value = "/fixlevel", method = RequestMethod.GET)
    public String fixLevel() {

        List<PtmCategory> categories = categoryService.listSubCategories(0L);
        fixCates(categories, 1);

        return "system/ok";
    }

    private void fixCates(List<PtmCategory> categories, int level) {
        if (ArrayUtils.isNullOrEmpty(categories)) {
            return;
        }

        for (PtmCategory category : categories) {
            List<PtmCategory> categories2 = categoryService.listSubCategories(category.getId());
            fixCates(categories2, level + 1);

            categoryService.updateCategoryLevel(category.getId(), level);
        }

    }


}
