package hasoffer.job.bean;

import hasoffer.base.thread.HasofferThreadFactory;
import hasoffer.data.redis.IRedisMapService;
import hasoffer.job.dmo.AliVPC;
import hasoffer.job.service.AliVPCLogService;
import hasoffer.job.service.AliVPCService;
import hasoffer.job.worker.EipTaskWorker;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EipJobBean extends QuartzJobBean {

    @Resource
    private AliVPCService aliVPCService;

    @Resource
    private AliVPCLogService aliVPCLogService;

    @Resource
    private IRedisMapService<String, String> mapService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        HasofferThreadFactory factory = new HasofferThreadFactory("FetchCompareWebsiteWorker");
        List<AliVPC> aliVPCList = aliVPCService.queryAllVPCList();
        ExecutorService es = Executors.newCachedThreadPool(factory);
        for (AliVPC vpc : aliVPCList) {
            try {
                TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                es.execute(new EipTaskWorker(vpc, aliVPCService, aliVPCLogService, mapService));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
