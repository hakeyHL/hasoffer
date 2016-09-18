package hasoffer.spider.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger("spider.consumer");

    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{
                        "classpath:/spring/application.xml",
                        "classpath:/spring-beans.xml",
                        "classpath:/spring/spring-context-holder.xml",
                        "classpath:/spring/spring-dubbo-result.xml"
                });
        context.start();
        logger.info("start finish!");
        while(true){

        }
    }


}