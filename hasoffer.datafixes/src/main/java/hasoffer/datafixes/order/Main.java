package hasoffer.datafixes.order;

import hasoffer.datafixes.order.work.OrderFixWorker;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.text.ParseException;
import java.util.Date;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("hasoffer.orderfix");

    public static void main(String[] args) throws InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "classpath:/spring-beans.xml",
                "classpath:/spring/spring-init.xml");
        context.start();
        logger.info("args:{}", args);
        if (args.length != 2) {
            logger.error("Args is error. please input start time and end time, 'yyyy-mm-dd'");
            return;
        }
        String arg0 = args[0];
        String arg1 = args[1];
        Date startDate;
        Date endDate;
        try {
            startDate = DateUtils.parseDate(arg0, "yyyy-MM-dd");
        } catch (ParseException e) {
            logger.error("Parse date is error. the startDate:{}", arg0, e);
            return;
        }
        try {
            endDate = DateUtils.parseDate(arg1, "yyyy-MM-dd");
        } catch (ParseException e) {
            logger.error("Parse date is error. the endDate:{}", arg1, e);
            return;
        }
        logger.info("Job start.");
        OrderFixWorker bean = context.getBean(OrderFixWorker.class);
        bean.runTask(startDate, endDate);
        logger.info("Job finish.");
    }
}
