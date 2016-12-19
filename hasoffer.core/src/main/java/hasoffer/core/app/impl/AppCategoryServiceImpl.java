package hasoffer.core.app.impl;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.AppCategoryService;
import hasoffer.core.bo.product.CategoryBo;
import hasoffer.core.bo.product.CategoryVo;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.utils.ImageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016年12月19日.
 * Time 17:25
 */
@Service
public class AppCategoryServiceImpl implements AppCategoryService {
    private static final String CACHE_KEY_PRE = "APP_PTM_CATEGORY";
    private static final long CACHE_EXPIRE_TIME = TimeUtils.MILLISECONDS_OF_1_DAY;
    @Resource
    ICacheService<CategoryBo> CategoryBoService;
    @Resource
    ICacheService<PtmCategory> PtmCategoryService;
    @Resource
    AppServiceImpl appService;

    public List getCategorys(String categoryId) {
        String key;
        List categorys = new ArrayList();
        if (StringUtils.isBlank(categoryId)) {
            key = CACHE_KEY_PRE + "_LEVEL1";
            CategoryBo categoryBo = CategoryBoService.get(CategoryBo.class, key, 0);
            if (categoryBo != null) {
                categorys = categoryBo.getCategorys();
            } else {
                categoryBo = new CategoryBo();
                categorys = new ArrayList();
                List<PtmCategory> ptmCategorys = appService.getCategory();
                for (PtmCategory ptmCategory : ptmCategorys) {
                    CategoryVo categoryVo = new CategoryVo();
                    categoryVo.setId(ptmCategory.getId());
                    categoryVo.setHasChildren(1);
                    categoryVo.setImage(ptmCategory.getImageUrl() == null ? "" : ImageUtil.getImageUrl(ptmCategory.getImageUrl()));
                    categoryVo.setLevel(ptmCategory.getLevel());
                    categoryVo.setName(ptmCategory.getName());
                    categoryVo.setParentId(ptmCategory.getParentId());
                    categoryVo.setRank(ptmCategory.getRank());
                    List<PtmCategory> ptmCategorysTemp = appService.getChildCategorys(categoryVo.getId().toString());
                    if (ptmCategorysTemp == null && ptmCategorysTemp.size() < 1) {
                        categoryVo.setHasChildren(0);
                    }
                    categorys.add(categoryVo);
                }
            }
            categoryBo.setCategorys(categorys);
            if (categoryBo.getCategorys() != null && categoryBo.getCategorys().size() > 0) {
                CategoryBoService.add(key, categoryBo, CACHE_EXPIRE_TIME);
            }
        } else {
            key = CACHE_KEY_PRE + categoryId;
            CategoryBo categoryBo = CategoryBoService.get(CategoryBo.class, key, 0);
            if (categoryBo != null) {
                categorys = categoryBo.getCategorys();
            } else {
                categoryBo = new CategoryBo();
                List<PtmCategory> ptmCategorys = null;
                ptmCategorys = appService.getChildCategorys(categoryId);
                for (PtmCategory ptmCategory : ptmCategorys) {
                    List childCategory = new ArrayList();
                    CategoryVo categoryVo = new CategoryVo();
                    categoryVo.setId(ptmCategory.getId());
                    categoryVo.setImage(ptmCategory.getImageUrl() == null ? "" : ImageUtil.getImageUrl(ptmCategory.getImageUrl()));
                    categoryVo.setLevel(ptmCategory.getLevel());
                    categoryVo.setName(ptmCategory.getName());
                    categoryVo.setParentId(ptmCategory.getParentId());
                    categoryVo.setRank(ptmCategory.getRank());
                    categoryVo.setHasChildren(1);
                    List<PtmCategory> ptmCategorysTemp = appService.getChildCategorys(ptmCategory.getId().toString());
                    if (ptmCategorysTemp != null && ptmCategorysTemp.size() > 0) {
                        for (PtmCategory ptmCates : ptmCategorysTemp) {
                            CategoryVo cate = new CategoryVo();
                            cate.setId(ptmCates.getId());
                            cate.setHasChildren(0);
                            cate.setImage(ptmCates.getImageUrl() == null ? "" : ImageUtil.getImageUrl(ptmCates.getImageUrl()));
                            cate.setLevel(ptmCates.getLevel());
                            cate.setName(ptmCates.getName());
                            cate.setParentId(ptmCates.getParentId());
                            cate.setRank(ptmCates.getRank());
                            childCategory.add(cate);
                        }
                    } else {
                        categoryVo.setHasChildren(0);
                    }
                    categoryVo.setCategorys(childCategory);
                    categorys.add(categoryVo);
                }
                categoryBo.setCategorys(categorys);
                if (categoryBo.getCategorys() != null && categoryBo.getCategorys().size() > 0) {
                    CategoryBoService.add(key, categoryBo, CACHE_EXPIRE_TIME);
                }
            }
        }
        return categorys;
    }

    public PtmCategory getCategoryById(Long cateId) {
        PtmCategory ptmCategory;
        String key;
        key = CACHE_KEY_PRE + "_BYID_" + cateId;
        ptmCategory = PtmCategoryService.get(PtmCategory.class, key, 0);
        if (ptmCategory != null) {
            //缓存中有
            return ptmCategory;
        } else {
            ptmCategory = appService.getCategoryInfo(cateId);
            if (ptmCategory != null) {
                System.out.println("将类目加入缓存 :" + ptmCategory.getId());
                PtmCategoryService.add(key, ptmCategory, CACHE_EXPIRE_TIME);
                return ptmCategory;
            }
        }
        return ptmCategory;
    }

    @Override
    public List<CategoryVo> getTopCategoryList() {
        List<CategoryVo> categoryVos = new ArrayList<>();

        //id name image hasChildren parentId rank level

        categoryVos.add(new CategoryVo(0l, "Deal & Offers", "http://img1.hasofferimage.com/topcate/Mobiles.png", 0, 0l, 0, 0));

        categoryVos.add(new CategoryVo(5l, "Mobiles", "http://img1.hasofferimage.com/topcate/Mobiles.png", 1, 1l, 0, 2));

        categoryVos.add(new CategoryVo(179l, "Mobile Accessories", "http://img1.hasofferimage.com/topcate/MobileAccessories.png", 1, 1l, 1, 2));


        categoryVos.add(new CategoryVo(102908l, "Men", "http://img1.hasofferimage.com/topcate/Men.png", 1, 7577l, 0, 2));

        categoryVos.add(new CategoryVo(694l, "Pen Drives", "http://img1.hasofferimage.com/topcate/PenDrives.png", 0, 230l, 2, 3));

        categoryVos.add(new CategoryVo(182l, "Watches", "http://img1.hasofferimage.com/topcate/Watches.png", 1, 7577l, 9, 2));

        categoryVos.add(new CategoryVo(3100l, "Televisions", "http://img1.hasofferimage.com/topcate/Televisions.png", 1, 1l, 3, 2));

        categoryVos.add(new CategoryVo(0l, "All Categories", "http://img1.hasofferimage.com/topcate/AllCategories.png", 0, 0l, 0, 0));

        categoryVos.add(new CategoryVo(1l, "Electronics", "", 1, 0l, 0, 1));

        categoryVos.add(new CategoryVo(7577l, "Lifestyle", "", 1, 0l, 1, 1));

        categoryVos.add(new CategoryVo(4552l, "Home and Furniture", "", 1, 0l, 2, 1));

        return categoryVos;
    }
}
