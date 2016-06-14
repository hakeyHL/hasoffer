package hasoffer.webcommon.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicLong;

public class ServerSessionListener implements HttpSessionListener {

    private AtomicLong curCount = new AtomicLong(0);

    private Logger logger = LoggerFactory.getLogger(ServerSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        curCount.addAndGet(1);

        show();
    }

    private void show() {
        long count = curCount.get();
        logger.info("current session count : " + count);
//        if (count % 10 == 0) {
//            logger.info("current session count : " + count);
//        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        curCount.addAndGet(-1);

        show();
    }
}
