package hasoffer.job.service.impl;

import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class FetchTestWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(FetchTestWorker.class);

    private Queue<PtmCmpSku> queue;
    private IFetchDubboService fetchDubboService;
    private File file;

    public FetchTestWorker(List<PtmCmpSku> list, IFetchDubboService fetchDubboService, File file) {
        this.queue = new ArrayDeque<>(list);
        this.fetchDubboService = fetchDubboService;
        this.file = file;
    }

    @Override
    public void run() {
        //long expireSeconds = 20 * 60;
        //String lineSeparator = System.getProperty("line.separator");
        while (!queue.isEmpty()) {
            PtmCmpSku ptmCmpSku = queue.poll();
            if (ptmCmpSku != null) {
                //TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), expireSeconds);
                //if (TaskStatus.FINISH.equals(taskStatus)) {
                //    FetchUrlResult fetchUrlResult = fetchDubboService.getProductsByUrl(
                //            ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), expireSeconds);
                //    try {
                //        // taskId getTaskStatus url	skuId	price	website
                //        FileUtils.write(file,
                //                fetchUrlResult.getTaskId() + "\t" + fetchUrlResult.getTaskStatus().toString() + "\t" + ptmCmpSku.getUrl() + "\t" + ptmCmpSku.getId() + "\t" + fetchUrlResult.getFetchProduct().getPrice() + "\t" + fetchUrlResult.getWebsite().toString() + lineSeparator, "utf-8",
                //                true);
                //    } catch (IOException e) {
                //        logger.error(e.getMessage());
                //    }
                //
                //} else if (TaskStatus.EXCEPTION.equals(taskStatus) || TaskStatus.STOPPED.equals(taskStatus)) {
                //    FetchUrlResult fetchUrlResult = fetchDubboService.getProductsByUrl(
                //            ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), expireSeconds);
                //    logger.error(fetchUrlResult.toString());
                //} else {
                //    queue.add(ptmCmpSku);
                //}
            }
        }
    }
}
