package hasoffer.fetch.helper;

import hasoffer.base.model.Website;
import hasoffer.fetch.core.IListProcessor;
import hasoffer.fetch.core.IProductProcessor;
import hasoffer.fetch.sites.amazon.AmazonListProcessor;
import hasoffer.fetch.sites.amazon.AmazonProductProcessor;
import hasoffer.fetch.sites.banggood.BanggoodListProcessor;
import hasoffer.fetch.sites.banggood.BanggoodProductProcessor;
import hasoffer.fetch.sites.ebay.EbayListProcessor;
import hasoffer.fetch.sites.flipkart.FlipkartListProcessor;
import hasoffer.fetch.sites.flipkart.FlipkartProductProcessor;
import hasoffer.fetch.sites.gearbest.GearbestListProcessor;
import hasoffer.fetch.sites.gearbest.GearbestProductProcessor;
import hasoffer.fetch.sites.geekbuying.GeekbuyingListProcessor;
import hasoffer.fetch.sites.geekbuying.GeekbuyingProductProcessor;
import hasoffer.fetch.sites.paytm.PaytmListProcessor;
import hasoffer.fetch.sites.shopclues.ShopcluesListProcessor;
import hasoffer.fetch.sites.snapdeal.SnapdealListProcessor;
import hasoffer.fetch.sites.tinydeal.TinydealListProcessor;
import hasoffer.fetch.sites.tinydeal.TinydealProductProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WebsiteProcessorFactory {
    private static final Map<Website, Class<IProductProcessor>> webSiteProductProcessorMap =
            new HashMap<Website, Class<IProductProcessor>>();
    public static final Map<Website, Class<IListProcessor>> webSiteListProcessorMap = new HashMap<Website, Class<IListProcessor>>();
    private static Logger logger = LoggerFactory.getLogger(WebsiteProcessorFactory.class);

    static {
        // product processor
        webSiteProductProcessorMap.put(Website.GEARBEST, (Class) GearbestProductProcessor.class);
        webSiteProductProcessorMap.put(Website.BANGGOOD, (Class) BanggoodProductProcessor.class);
        webSiteProductProcessorMap.put(Website.GEEKBUYING, (Class) GeekbuyingProductProcessor.class);
        webSiteProductProcessorMap.put(Website.TINYDEAL, (Class) TinydealProductProcessor.class);
        webSiteProductProcessorMap.put(Website.DX, null);
        webSiteProductProcessorMap.put(Website.ALIEXPRESS, null);
        webSiteProductProcessorMap.put(Website.AMAZON, (Class) AmazonProductProcessor.class);
        webSiteProductProcessorMap.put(Website.FLIPKART, (Class) FlipkartProductProcessor.class);

        // list processor
        webSiteListProcessorMap.put(Website.GEARBEST, (Class) GearbestListProcessor.class);
        webSiteListProcessorMap.put(Website.BANGGOOD, (Class) BanggoodListProcessor.class);
        webSiteListProcessorMap.put(Website.GEEKBUYING, (Class) GeekbuyingListProcessor.class);
        webSiteListProcessorMap.put(Website.TINYDEAL, (Class) TinydealListProcessor.class);
        webSiteListProcessorMap.put(Website.FLIPKART, (Class) FlipkartListProcessor.class);
        webSiteListProcessorMap.put(Website.DX, null);
        webSiteListProcessorMap.put(Website.ALIEXPRESS, null);
        webSiteListProcessorMap.put(Website.AMAZON, (Class) AmazonListProcessor.class);
        webSiteListProcessorMap.put(Website.SNAPDEAL, (Class) SnapdealListProcessor.class);
        webSiteListProcessorMap.put(Website.SHOPCLUES, (Class) ShopcluesListProcessor.class);
        webSiteListProcessorMap.put(Website.PAYTM, (Class) PaytmListProcessor.class);
        webSiteListProcessorMap.put(Website.EBAY, (Class) EbayListProcessor.class);
    }

    public static IProductProcessor getProductProcessor(Website website) {
        Class<IProductProcessor> productProcessorClass = webSiteProductProcessorMap.get(website);
        if (productProcessorClass == null) {
            return null;
        }

        return (IProductProcessor) getProcessorInstance(productProcessorClass);
    }

    public static IListProcessor getListProcessor(Website website) {
        Class<IListProcessor> listProcessorClass = webSiteListProcessorMap.get(website);
        if (listProcessorClass == null) {
            return null;
        }

        return (IListProcessor) getProcessorInstance(listProcessorClass);
    }

    private static Object getProcessorInstance(Class processorClass) {
        String className = processorClass.getName();
        try {
            Class<?> clazz = Class.forName(className);
            if (!processorClass.isAssignableFrom(clazz)) {
                logger.debug("Class " + className + " should be subclass of " + processorClass.getName());
                return null;
            }
            return clazz.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("Class " + className + " not found");
            return null;
        } catch (InstantiationException e) {
            logger.error("Class " + className + " doesn't have a default constructor");
            return null;
        } catch (IllegalAccessException e) {
            logger.error("Class " + className + " default constructor is not public accessible");
            return null;
        }
    }
}
