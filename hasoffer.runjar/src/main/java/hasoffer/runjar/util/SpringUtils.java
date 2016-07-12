package hasoffer.runjar.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by chevy on 2016/7/12.
 */
public class SpringUtils {

    private static ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-beans.xml");

    public static <T> T getBean(Class<T> clazz) {
        return (T) ctx.getBean(clazz);
    }

}
