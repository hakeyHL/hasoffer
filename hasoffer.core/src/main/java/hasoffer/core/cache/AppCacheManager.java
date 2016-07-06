package hasoffer.core.cache;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.product.CategoryBo;
import hasoffer.core.bo.product.CategoryVo;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.utils.ImageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016/6/30.
 */
@Component
public class AppCacheManager {
    private static final String CACHE_KEY_PRE = "APP_PTM_CATEGORY";
    private static final long CACHE_EXPIRE_TIME = TimeUtils.MILLISECONDS_OF_1_DAY;
    @Resource
    ICacheService<CategoryBo> cacheService;
    @Resource
    AppServiceImpl appService;
    private Logger logger = LoggerFactory.getLogger(AppCacheManager.class);

    public List getCategorys(String categoryId) {
        String key = null;
        List categorys = new ArrayList();
        if (StringUtils.isBlank(categoryId)) {
            key = CACHE_KEY_PRE + "_LEVEL1";
            CategoryBo categoryBo = cacheService.get(CategoryBo.class, key, 0);
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
                cacheService.add(key, categoryBo, CACHE_EXPIRE_TIME);
            }
        } else {
            key = CACHE_KEY_PRE + categoryId;
            CategoryBo categoryBo = cacheService.get(CategoryBo.class, key, 0);
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
//                            List li=appService.getChildCategorys(ptmCates.getId().toString());
//                            if(li!=null&&li.size()>0){
//                                cate.setHasChildren(1);
//                            }
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
                    cacheService.add(key, categoryBo, CACHE_EXPIRE_TIME);
                }
            }
        }
        return categorys;
    }

}
