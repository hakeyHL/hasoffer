package hasoffer.job.service.impl;

import hasoffer.base.enums.TaskStatus;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.job.dto.FetchTestTaskDTO;
import hasoffer.spider.model.FetchUrlResult;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class FetchTestWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(FetchTestWorker.class);

    private Queue<FetchTestTaskDTO> queue;
    private IFetchDubboService fetchDubboService;
    private File file;

    public FetchTestWorker(List<FetchTestTaskDTO> list, IFetchDubboService fetchDubboService, File file) {
        this.queue = new ArrayDeque<>(list);
        this.fetchDubboService = fetchDubboService;
        this.file = file;
    }

    @Override
    public void run() {
        long expireSeconds = 20 * 60;
        while (queue.size() > 0) {
            FetchTestTaskDTO ptmCmpSku = queue.poll();
            if (ptmCmpSku != null) {
                TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), expireSeconds);
                if (TaskStatus.FINISH.equals(taskStatus)) {
                    FetchUrlResult fetchUrlResult = fetchDubboService.getProductsByUrl(
                            ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), expireSeconds);
                    try {
                        FileUtils.write(file,
                                fetchUrlResult.toString().replace("\r", " ").replace("\n", " ") + "\r\n", "utf-8",
                                true);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }

                } else if (TaskStatus.EXCEPTION.equals(taskStatus)||TaskStatus.STOPPED.equals(taskStatus)) {
                    FetchUrlResult fetchUrlResult = fetchDubboService.getProductsByUrl(
                            ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), expireSeconds);
                    logger.error(fetchUrlResult.toString());
                }else{
                    queue.add(ptmCmpSku);
                }
            }
        }
    }
}
