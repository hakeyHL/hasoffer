package hasoffer.test;

import hasoffer.base.utils.TimeUtils;
import hasoffer.spider.task.service.SpiderTaskService;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger("spider.consumer");

    public static void main(String[] args) throws InterruptedException {
        Timer timer = new Timer();
        timer.schedule(new SpiderTask(), 1000, 2000);
    }

    private static class SpiderTask extends TimerTask {
        @Override
        public void run() {
            Map<String, String> x = new HashMap<>();
            x.put("a", "A");
            x.put("b", "B");
            System.out.println(x.values());
            System.out.println("ttt");
        }
    }
}
