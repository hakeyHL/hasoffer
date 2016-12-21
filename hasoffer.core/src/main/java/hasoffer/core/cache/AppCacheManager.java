package hasoffer.core.cache;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.product.CategoryBo;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by hs on 2016/6/30.
 */
@Component
public class AppCacheManager {
    private static final String CACHE_KEY_PRE = "APP_PTM_CATEGORY";
    private static final long CACHE_EXPIRE_TIME = TimeUtils.MILLISECONDS_OF_1_DAY;
    @Resource
    ICacheService<CategoryBo> CategoryBoService;
    @Resource
    ICacheService<PtmCategory> PtmCategoryService;
    @Resource
    AppServiceImpl appService;
    private Logger logger = LoggerFactory.getLogger(AppCacheManager.class);

}
