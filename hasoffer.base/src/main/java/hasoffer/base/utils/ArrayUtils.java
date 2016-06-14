package hasoffer.base.utils;

import java.util.Collection;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/10/28
 */
public class ArrayUtils {

	public static boolean isNullOrEmpty(Collection collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean hasObjs(Collection collection) {
		return !isNullOrEmpty(collection);
	}
}
