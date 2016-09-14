package hasoffer.spider.main;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{
                        "classpath:/spring/application.xml",
                        "classpath:/spring/spring-context-holder.xml",
                        "classpath:/spring/spring-redis.xml",
                        "classpath:/spring/spring-dubbo-provider.xml",
                        "classpath:/spring/spring-spider-holder.xml"
                });
        context.start();

        System.out.println("start finish.");
        while (true) {
            Thread.sleep(1000);
        }
    }
}
