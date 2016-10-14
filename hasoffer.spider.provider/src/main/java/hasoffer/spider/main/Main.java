package hasoffer.spider.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger("spider.provider");

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

        logger.info("start finish.");
        while (true) {
            Thread.sleep(60000);
            logger.info("continue");
        }
    }
}
