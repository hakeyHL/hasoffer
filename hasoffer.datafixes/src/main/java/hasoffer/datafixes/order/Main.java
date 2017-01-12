package hasoffer.datafixes.order;

import hasoffer.base.enums.MarketChannel;
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
        //logger.info("args:{}", args);
        if (args.length != 3) {
            logger.error("Args is error. please input channelStr, orderDateStr, orderNumStr");
            return;
        }
        String channelStr = args[0];
        String orderDateStr = args[1];
        String orderNumStr = args[2];


        MarketChannel marketChannel = MarketChannel.valueOfString(channelStr);
        if (MarketChannel.NONE.equals(marketChannel)) {
            logger.error("Parse channel is error. the channelStr:{}", channelStr);
            return;
        }

        Date orderDate;
        try {
            orderDate = DateUtils.parseDate(orderDateStr, "yyyy-MM-dd");
        } catch (ParseException e) {
            logger.error("Parse orderDateStr is error. the orderDateStr:{}", orderDateStr, e);
            return;
        }
        Integer orderNum;
        try {
            orderNum = Integer.valueOf(orderNumStr);
        } catch (Exception e) {
            logger.error("Parse orderNumStr is error. the orderNumStr:{}", orderNumStr, e);
            return;
        }
        logger.info("Job start.");
        OrderFixWorker bean = context.getBean(OrderFixWorker.class);
        bean.runTask(marketChannel, orderDate, orderNum);
        logger.info("Job finish.");
    }
}
