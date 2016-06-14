package hasoffer.base.utils;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by glx on 2015/4/21.
 */
public class BeanUtil {
	public static Map<String, Object> objectToMap(Object obj) throws InvocationTargetException, IllegalAccessException {
		Map<String, Object> map = new HashMap<String, Object>();
		Class<?> productModelClass = obj.getClass();
		Field[] fields = productModelClass.getDeclaredFields();
		for(Field field :  fields) {
			field.setAccessible(true);
			String fieldName = field.getName();
			String key = fieldName;
			Object value = field.get(obj);
			if(value!=null) {
				map.put(key, value);
			}
		}

		return map;
	}

	public static void mapToObj(Map<String, Object> map, Object obj) throws InvocationTargetException, IllegalAccessException {
		Class<?> productModelClass = obj.getClass();
		Field[] fields = productModelClass.getDeclaredFields();
		for(Field field :  fields) {
			String fieldName = field.getName();
			field.setAccessible(true);
			field.set(obj, map.get(fieldName));
		}
	}
}
