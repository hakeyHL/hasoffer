package hasoffer.admin.controller;

import hasoffer.admin.common.CategoryHelper;
import hasoffer.admin.controller.vo.CategoryVo;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.exception.CategoryDeleteException;
import hasoffer.core.product.solr.CategoryIndexServiceImpl;
import hasoffer.core.product.solr.CategoryModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
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
    CategoryIndexServiceImpl categoryIndexService;
    @Resource
    IProductService productService;

    private Pattern PATTERN_IN_WORD = Pattern.compile("[^0-9a-zA-Z\\-]");

    @RequestMapping(value = "/testsolr", method = RequestMethod.GET)
    public ModelAndView testSolr(@RequestParam(defaultValue = "") String q) {

        List<CategoryModel> cms = categoryIndexService.simpleSearch(q);

        ModelAndView mav = new ModelAndView("product/catesolr");
        mav.addObject("cms", cms);
        mav.addObject("q", q);
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

        String category3Str = request.getParameter("category3");
        if (StringUtils.isEmpty(category3Str) || category3Str.equals("-1")) {
            // todo 抛出异常
        }

        // 当前的类目，目前限定为只能是第三级目录
        String currentCateIdStr = request.getParameter("currentCateId");
        moveProducts(Long.valueOf(currentCateIdStr), Long.valueOf(category3Str));

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

    @RequestMapping(value = "/moveCategoryToNewCategory", method = RequestMethod.GET)
    public ModelAndView moveCategoryToNewCategory(HttpServletRequest request) {

        return null;
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

        if (!StringUtils.isEmpty(categoryName)) {
            categoryService.updateCategoryName(id, categoryName);
        }

        if (!StringUtils.isEmpty(categoryTag)) {
            categoryService.updateCategoryKeyword(id, categoryTag);
        }

        return mav;
    }

    private void analysis(Map<String, Long> statMap, String title) {
        String[] words = title.split(" ");
        for (String w : words) {
            w = w.toLowerCase().trim();
            if (PATTERN_IN_WORD.matcher(w).find()) {
//                System.out.println(w);
                continue;
            }
            Long count = statMap.get(w);
            if (count == null) {
                count = new Long(1);
                statMap.put(w, count);
            } else {
                count++;
                statMap.put(w, count);
            }
        }
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
