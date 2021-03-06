package hasoffer.thread;

import hasoffer.base.thread.HasofferThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {

    public static void main(String[] args) {
        HasofferThreadFactory factory = new HasofferThreadFactory("FetchUrlWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);

        for (int i = 0; i < 100; i++) {
            es.execute(new ThreadWorker());
        }
    }

    private static class ThreadWorker implements Runnable {

        @Override
        public void run() {
         /*   System.out.println(new Date() + ":" + Thread.currentThread().getName() + " start");
            IndiaAmazonPageProcessor pageProcessor = new IndiaAmazonPageProcessor();
            SpiderLogger.debugFetchFlow("AmazonThirdSummaryProductProcessor.cookie is:{}", pageProcessor.getSite().getCookies().toString());

            OnErrorListener spiderListener = new OnErrorListener() {

                @Override
                public void onError(Request request) {
                    System.out.println("onError     " + request.getExtra("statusCode") + ", url=" + request.getUrl() + ", WEB-SITE=" + request.getExtra("WEB-SITE"));
                }
            };
            pageProcessor.getSite().setRedirectsEnabled(false);
            Spider spider = Spider.create(pageProcessor);
            Request request = new Request();
            request.putExtra("WEB-SITE", Website.AMAZON);
            request.setUrl("http://www.ebay.in/itm/Celkon-Campus-One-A354C-Black-Red-512MB-/201493847741");
            spider.addRequest(request)
                    //.addPipeline(new RedisPagePipeline())
                    //.addErrorPipeline(new ErrorRedisPagePipeline())
                    .addPipeline(new ConsolePagePipeline())
                    .thread(1);
            spider.addOnErrorListener(spiderListener);
            spider.run();

            System.out.println(new Date() + ":" + Thread.currentThread().getName() + " end");*/
        }
    }
}

