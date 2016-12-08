package hasoffer.job.bean;

import hasoffer.alivpc.dmo.AliVPCDMO;
import hasoffer.alivpc.service.AliVPCLogService;
import hasoffer.alivpc.service.AliVPCService;
import hasoffer.base.thread.HasofferThreadFactory;
import hasoffer.data.redis.IRedisMapService;
import hasoffer.job.worker.EipTaskWorker;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class EipJobBean extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger(EipJobBean.class);

    @Resource
    private AliVPCService aliVPCService;

    @Resource
    private AliVPCLogService aliVPCLogService;

    @Resource
    private IRedisMapService<String, String> mapService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        HasofferThreadFactory factory = new HasofferThreadFactory("FetchCompareWebsiteWorker");
        List<AliVPCDMO> aliVPCDMOList = aliVPCService.queryAllVPCList();
        List<Future<Boolean>> resultList = new ArrayList<>();
        ExecutorService es = Executors.newCachedThreadPool(factory);
        for (AliVPCDMO vpc : aliVPCDMOList) {
            try {
                TimeUnit.SECONDS.sleep(new Random().nextInt(5));
            } catch (Exception e) {
                logger.error("", e);
            }
            //使用ExecutorService执行Callable类型的任务，并将结果保存在future变量中
            Future<Boolean> future = es.submit(new EipTaskWorker(vpc, aliVPCService, aliVPCLogService, mapService));
            //将任务执行结果存储到List中
            resultList.add(future);
        }

        //遍历任务的结果
        for (Future<Boolean> fs : resultList) {
            try {
                logger.info(fs.get().toString()); //打印各个线程（任务）执行的结果
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } finally {
                //启动一次顺序关闭，执行以前提交的任务，但不接受新任务。如果已经关闭，则调用没有其他作用。
                es.shutdown();
            }
        }
    }
}
