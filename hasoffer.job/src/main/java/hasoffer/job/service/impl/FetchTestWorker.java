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
import java.util.ArrayList;
import java.util.List;

public class FetchTestWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchTestWorker.class);

    private List<FetchTestTaskDTO> list;
    private IFetchDubboService fetchDubboService;
    private File file;

    public FetchTestWorker(List<FetchTestTaskDTO> list, IFetchDubboService fetchDubboService, File file) {
        this.list = list;
        this.fetchDubboService = fetchDubboService;
        this.file = file;
    }

    @Override
    public void run() {
        long expireSeconds = 20 * 60;
        while (list != null && !list.isEmpty()) {
            List<FetchTestTaskDTO> resultList = new ArrayList<FetchTestTaskDTO>();
            for (FetchTestTaskDTO ptmCmpSku : list) {
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

                    resultList.add(ptmCmpSku);
                }
            }
            if (!resultList.isEmpty()) {
                list.removeAll(resultList);
                resultList.clear();
            }
        }
    }
}
