package hasoffer.base.config;

/**
 * Created by glx on 2015/6/2.
 */
public class TestConfig {
	public static void main(String[] args){
		AppSettings settings = AppSettingsStore.getInstance("test-settings.xml").getAppSettings();
		System.out.println(settings.getConfigKeys().length);
		System.out.println(settings.getSetting("a"));
		System.out.println(settings.getSetting("b"));
		System.out.println(settings.getSetting("c"));

	}
}
