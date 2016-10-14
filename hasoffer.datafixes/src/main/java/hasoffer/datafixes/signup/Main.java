package hasoffer.datafixes.signup;

import hasoffer.datafixes.signup.work.FixWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 修复签到的统计问题
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger("hasoffer.datafixes");

    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "classpath:/spring-beans.xml",
                "classpath:/spring/spring-init.xml");
        context.start();
        context.getBean(FixWorker.class);
        logger.info("start finish.");
        //while (true) {
        //    Thread.sleep(60000);
        //    logger.info("continue");
        //}
    }


}
