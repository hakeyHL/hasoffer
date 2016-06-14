package hasoffer.base.utils;

import java.util.UUID;
import java.util.regex.Pattern;

public class IDUtil {
	public static String uuid() {
		return UUID.randomUUID().toString();
	}

	public static boolean isUUID(String target) {
		if (target == null) {
			return false;
		}
		Pattern pattern = Pattern.compile("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}");
		return pattern.matcher(target.toLowerCase()).matches();
	}

	public static boolean isSerialNo(String serialNo) {
		if (serialNo == null) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]+");
		return pattern.matcher(serialNo).matches();
	}

	public static void main(String[] args) {
		System.out.print(isUUID("0021edfd-d311-4ac6-86a-5a088dfa13d2"));
	}
}
