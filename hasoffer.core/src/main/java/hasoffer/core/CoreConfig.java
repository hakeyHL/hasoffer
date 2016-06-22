package hasoffer.core;


import hasoffer.base.config.AppSettings;
import hasoffer.base.config.AppSettingsStore;

import java.io.Serializable;

/**
 * Created on 2015/10/22.
 */
public class CoreConfig implements Serializable {
    public static final String SOLR_PRODUCT_URL = "SOLR_PRODUCT_URL";
    public static final String IMAGE_URL_3RD_PREFIX = "IMAGE_URL_3RD_PREFIX";
    public static final String IMAGE_UPLOAD_URL = "IMAGE_UPLOAD_URL";
    public static final String IMAGE_UPLOAD_URL2 = "IMAGE_UPLOAD_URL2";
    public static final String IMAGE_HOST = "IMAGE_HOST";
    public static final String WEB_HOST = "WEB_HOST";
    //API_HOST
    public static final String API_HOST = "API_HOST";
    public static final String SOLR_CATEGORY_URL = "SOLR_CATEGORY_URL";
    public static final String SOLR_CMPSKU_URL = "SOLR_CMPSKU_URL";

    public static final int BATCH_MAX_ROW = 30;

    private static final AppSettings appSettings = AppSettingsStore.getInstance().getAppSettings();

    public static String get(String key) {
        return appSettings.getSetting(key);
    }
}
