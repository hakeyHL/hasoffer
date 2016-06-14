package hasoffer.base.config;


import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by glx on 2015/6/2.
 */
public class AppSettings implements Serializable {
	private String stage;
	private Map<String, String> configs = new HashMap<String, String>();

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public void addSetting(String key, String value) {
		configs.put(key, value);
	}

	public String getSetting(String itemName, String defaultValue) {
		String value = configs.get(itemName);
		if (value == null) {
			return defaultValue;
		}
		return StringUtils.trimToEmpty(value);
	}

	public String getSetting(String itemName) {
		return StringUtils.trimToEmpty(configs.get(itemName));
	}

	public int getSettingInt(String itemName, int defaultValue) {
		String value = configs.get(itemName);
		if (value == null) {
			return defaultValue;
		}
		int intValue;
		try {
			intValue = Integer.parseInt(StringUtils.trimToEmpty(value));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
		return intValue;
	}

	public long getSettingLong(String itemName) {
		return getSettingLong(itemName, 0);
	}

	public long getSettingLong(String itemName, int defaultValue) {
		String value = configs.get(itemName);
		if (value == null) {
			return defaultValue;
		}
		long longValue;
		try {
			longValue = Long.parseLong(StringUtils.trimToEmpty(value));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
		return longValue;
	}

	public int getSettingInt(String itemName) {
		return getSettingInt(itemName, 0);
	}

	public boolean getSettingBoolean(String itemName, boolean defaultValue) {
		String value = configs.get(itemName);
		if (value == null) {
			return defaultValue;
		}
		boolean booleanValue;
		try {
			booleanValue = Boolean.parseBoolean(StringUtils.trimToEmpty(value));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
		return booleanValue;
	}

	public boolean getSettingBoolean(String itemName) {
		return getSettingBoolean(itemName, false);
	}

	public String[] getConfigKeys() {
		return this.configs.keySet().toArray(new String[]{});
	}
}
