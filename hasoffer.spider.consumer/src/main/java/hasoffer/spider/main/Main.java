package hasoffer.spider.main;

import hasoffer.spider.task.service.SpiderTaskService;
import hasoffer.spring.context.SpringContextHolder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{
                        "classpath:/spring/application.xml",
                        "classpath:/spring-beans.xml",
                        "classpath:/spring/spring-context-holder.xml",
                        "classpath:/spring/spring-dubbo-provider.xml"
                });
        context.start();

        System.out.println("start finish!");

        Thread.sleep(10000);

        while (true) {
            SpiderTaskService taskInitContext = SpringContextHolder.getBean(SpiderTaskService.class);
            taskInitContext.initTask();
            TimeUnit.HOURS.sleep(1);
        }
    }
}
