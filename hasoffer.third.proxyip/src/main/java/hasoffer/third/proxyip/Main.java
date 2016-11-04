package hasoffer.third.proxyip;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {


    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{
                        "classpath:/spring/application.xml",
                        "classpath:/spring/spring-context-holder.xml",
                        "classpath:/spring/spring-redis.xml",
                        "classpath:/spring/spring-init.xml"
                });
        context.start();

    }


}
