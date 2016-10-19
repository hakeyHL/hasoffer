package hasoffer.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016年10月19日.
 * Time 14:28
 */
public class ApiUtils {
    /**
     * 在数据对象返回客户端之前检测其域是否都有值,除对象成员外都赋初始值
     *
     * @param object
     * @throws Exception
     */

    public static void resloveClass(Object object) {
        //加载这个类的成员
        //如果它是list且为null就初始化一下
        //如果是基本类型就算了,它们有初始值
        //如果是包装类型打破权限为其赋值
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field declaredField = declaredFields[i];
            declaredField.setAccessible(true);
            try {
                if (declaredField.getType().equals(List.class)) {
                    System.out.println("list");
                    declaredField.set(object, new ArrayList<>());
                }
                if (declaredField.getType().equals(Long.class)) {
                    System.out.println("Long ");
                    declaredField.set(object, 0l);
                }
                if (declaredField.getType().equals(Integer.class)) {
                    System.out.println("Long ");
                    declaredField.set(object, 0);
                }
                if (declaredField.getType().equals(Float.class)) {
                    System.out.println("Long ");
                    declaredField.set(object, 0f);
                }
            } catch (Exception e) {
                System.out.println("set field exception : " + e.getMessage());
            }
        }
        System.out.println("over");
    }
}
