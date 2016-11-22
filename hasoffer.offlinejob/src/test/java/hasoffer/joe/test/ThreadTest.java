package hasoffer.joe.test;

import hasoffer.base.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {

    private static Logger logger = LoggerFactory.getLogger(ThreadTest.class);

    private static int num;

    public static void main(String[] args) {

        final LinkedBlockingQueue<String> cmpSkuQueue = new LinkedBlockingQueue<>();

        final AtomicBoolean loadTaskFinished = new AtomicBoolean(false);

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {
            @Override
            public void run() {
                loadImageDownLoadTasks(cmpSkuQueue);
                loadTaskFinished.set(true);
            }
        });

        int processCount = 10;
        final AtomicInteger processorCount = new AtomicInteger(0);

        for (int i = 0; i < processCount; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    processorCount.addAndGet(1);

                    //标记线程的起始时间
                    long startTime = TimeUtils.now();

                    while (true) {

                        //该任务每隔俩个小时启动一次，设置100分钟线程自动结束
                        if (TimeUtils.now() - startTime > TimeUtils.MILLISECONDS_OF_1_MINUTE) {
                            logger.info("sku image download job bean live above 100 minutes");
                            break;
                        }

                        String t = cmpSkuQueue.poll();

                        if (t == null) {
                            break;
                        }

                        downloadImage2(t);
                    }

                    processorCount.addAndGet(-1);
                }

                private void downloadImage2(String t) {
                    logger.info("download: {}", t);
                }
            });
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                break;
            }

//            为了保证线程正常结束，注册掉该部分
//            if (cmpSkuQueue.size() > 0) {
//                logger.info("queue size = " + cmpSkuQueue.size());
//                continue;
//            }

            if (processorCount.get() > 0) {
                logger.info("processorCount = " + processorCount.get());
                continue;
            }

            if (!loadTaskFinished.get()) {
                continue;
            }

            break;
        }

        logger.info("sku image download job bean all jobs finished.");
    }

    private static void loadImageDownLoadTasks(LinkedBlockingQueue<String> cmpSkuQueue) {
        while (num < 10000000) {
            cmpSkuQueue.add("t:" + num++);
        }

    }
}
