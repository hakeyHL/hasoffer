package hasoffer.third.context;

import hasoffer.base.utils.TimeUtils;
import hasoffer.third.task.ProxyIPTimerTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProxyIPInitBean {


    public void runTask() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new ProxyIPTimerTask(), 1000, TimeUtils.MILLISECONDS_OF_1_MINUTE * 1, TimeUnit.MILLISECONDS);
    }

}
