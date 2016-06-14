package hasoffer.core.thd;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.thd.ThdCategory;
import hasoffer.core.persistence.po.thd.ThdProduct;
import hasoffer.core.persistence.po.thd.flipkart.ThdBCategory;
import hasoffer.core.persistence.po.thd.flipkart.ThdBProduct;
import hasoffer.core.persistence.po.thd.shopclues.ThdCProduct;
import hasoffer.core.persistence.po.thd.snapdeal.ThdACategory;
import hasoffer.core.persistence.po.thd.snapdeal.ThdAProduct;

/**
 * Date : 2016/2/22
 * Function :
 */
public class ThdHelper {

    //此处的变化，用来反应ajaxUrl中，替换startNum的值，不是真正的pageSize
    public static int getPageSize(Website website){
        if (website.equals(Website.SNAPDEAL)) {
            return 20;
        }
        if (website.equals(Website.FLIPKART)) {
            return 20;
        }
        if (website.equals(Website.SHOPCLUES)) {
            return 1;
        }
        return 0;
    }

    public static Class getThdProductClass(Website website) {
        if (website.equals(Website.SNAPDEAL)) {
            return ThdAProduct.class;
        }
        if (website.equals(Website.FLIPKART)) {
            return ThdBProduct.class;
        }
        if (website.equals(Website.SHOPCLUES)) {
            return ThdCProduct.class;
        }

        return null;
    }

    public static Class getThdCategoryClass(Website website) {
        if (website.equals(Website.SNAPDEAL)) {
            return ThdACategory.class;
        }
        if (website.equals(Website.FLIPKART)) {
            return ThdBCategory.class;
        }

        return null;
    }

    public static ThdProduct newThdProduct(Website website) {
        Class thdProductClass = getThdProductClass(website);
        try {
            return (ThdProduct) thdProductClass.newInstance();
        } catch (Exception e) {
        }
        return null;
    }

    public static ThdCategory newCategory(Website website) {
        Class thdCategoryClass = getThdCategoryClass(website);
        try {
            return (ThdCategory) thdCategoryClass.newInstance();
        } catch (Exception e) {
        }
        return null;
    }
}
