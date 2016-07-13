package hasoffer.timer.affiliate;

import hasoffer.affiliate.affs.AffiliateFactory;
import hasoffer.affiliate.affs.IAffiliateProcessor;
import hasoffer.affiliate.model.AffiliateCategory;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.thd.ThdCategory;
import hasoffer.core.thd.IThdService;
import hasoffer.core.thd.ThdHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created on 2016/3/7.
 */
//@Component
public class SaveAffiliateCategoryTask {

    private static Logger logger = LoggerFactory.getLogger(SaveAffiliateCategoryTask.class);

    //    @Resource
    public IThdService thdService;

    public void updateCategories() {

        IAffiliateProcessor affilicateProductProcessor = AffiliateFactory.getAffiliateProductProcessor(Website.FLIPKART);
//        IAffilicateCategory affilicateCategory = AffiliateCategoryFactory.getAffiliateCategory(Website.SNAPDEAL);

        try {

            List<AffiliateCategory> affiliateCategories = affilicateProductProcessor.getProductDirectory();

            for (AffiliateCategory affiliateCategory : affiliateCategories) {
                ThdCategory thdCateory = ThdHelper.newCategory(affiliateCategory.getWebsite());

                thdCateory.setParentId(affiliateCategory.getParentId());
                thdCateory.setUrl(affiliateCategory.getUrl());
                thdCateory.setName(affiliateCategory.getName());

                thdService.createCategory(thdCateory);
                logger.debug(affiliateCategory.getWebsite() + "--" + affiliateCategory.getName() + "--create success");
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }


}
