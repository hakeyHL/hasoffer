package hasoffer.base.config;

import hasoffer.base.utils.SystemUtil;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by glx on 2015/6/2.
 */
public class AppSettingsStore {
    public static final String APP_SETTINGS_XML = "app-settings.xml";
    private static final Logger logger = LoggerFactory.getLogger(AppSettingsStore.class);
    private static final String CONFIG_FILE = "app-settings.xml";
    private static final String CONFIG_ELEMENT = "app-settings/config";
    private static final String CONFIG_ITEM_ELEMENT = "app-settings/config/add";
    private static final String DEFAULT_STAGE = "default";
    private static Map<String, AppSettingsStore> storeMap = new HashMap();
    private String stage = DEFAULT_STAGE;

    private Map<String, AppSettings> configMap = new HashMap<String, AppSettings>();


    private AppSettingsStore(String configFile) {
        this.reload(configFile);
        AppSettings defaultSettings = configMap.get(DEFAULT_STAGE);
        if (defaultSettings == null) {
            defaultSettings = new AppSettings();
            defaultSettings.setStage(DEFAULT_STAGE);
            configMap.put(DEFAULT_STAGE, defaultSettings);
        }

        for (Map.Entry<String, AppSettings> entry : configMap.entrySet()) {
            if (!DEFAULT_STAGE.equals(entry.getKey())) {
                for (String defaultConfigKey : defaultSettings.getConfigKeys()) {
                    if (entry.getValue().getSetting(defaultConfigKey, null) == null) {
                        entry.getValue().addSetting(defaultConfigKey, defaultSettings.getSetting(defaultConfigKey));
                    }
                }
            }
        }
    }

    public static synchronized AppSettingsStore getInstance() {
        return getInstance(APP_SETTINGS_XML);
    }

    public static synchronized AppSettingsStore getInstance(String configFile) {
        if (configFile == null) {
            configFile = APP_SETTINGS_XML;
        }

        if (storeMap.containsKey(configFile)) {
            return (AppSettingsStore) storeMap.get(configFile);
        } else {
            AppSettingsStore store = new AppSettingsStore(configFile);
            storeMap.put(configFile, store);
            return store;
        }
    }

    public void addStageConfig(AppSettings appSettings) {
        configMap.put(appSettings.getStage(), appSettings);
    }

    public void reload(String configFile) {
        synchronized (this) {
            this.stage = SystemUtil.getAppEnv();

           if(configFile.startsWith("/")) {
                configFile = configFile.substring(1);
            }

            URL configURL = ConfigurationUtils.locate((String) null, configFile);

           // InputStream inputStream = AppSettingsStore.class.getClassLoader().getResourceAsStream(configFile);

            Digester digester = new Digester();
            digester.setValidating(false);
            digester.push(this);

            digester.addObjectCreate(CONFIG_ELEMENT, AppSettings.class);
            digester.addSetProperties(CONFIG_ELEMENT, "stage", "stage");
            digester.addCallMethod(CONFIG_ITEM_ELEMENT, "addSetting", 2);
            digester.addCallParam(CONFIG_ITEM_ELEMENT, 0, "key");
            digester.addCallParam(CONFIG_ITEM_ELEMENT, 1, "value");
            digester.addSetNext(CONFIG_ELEMENT, "addStageConfig");

            try {
                digester.parse(configURL.openStream());
            } catch (Exception e) {
                logger.error("{}", e);
            }finally {
            }
        }
    }

    public AppSettings getAppSettings() {
        AppSettings settings = configMap.get(this.stage);
        if (settings == null) {
            logger.debug("do not find stage:" + stage+", use default");
            return configMap.get(DEFAULT_STAGE);
        }
        return settings;
    }
}
