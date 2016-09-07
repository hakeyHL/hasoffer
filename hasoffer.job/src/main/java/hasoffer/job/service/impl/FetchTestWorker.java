package hasoffer.job.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasoffer.base.enums.TaskStatus;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.job.dto.FetchTestTaskDTO;
import hasoffer.spider.model.FetchUrlResult;

public class FetchTestWorker implements Runnable {

    private List<FetchTestTaskDTO> list;
    private IFetchDubboService fetchDubboService;
    private File file;
    private Logger logger = LoggerFactory.getLogger(FetchTestWorker.class);

    public FetchTestWorker(List<FetchTestTaskDTO> list, IFetchDubboService fetchDubboService, File file) {
        this.list = list;
        this.fetchDubboService = fetchDubboService;
        this.file = file;
    }

    @Override
    public void run() {
        while (list != null && !list.isEmpty()) {
            List<FetchTestTaskDTO> resultList = new ArrayList<FetchTestTaskDTO>();
            for (FetchTestTaskDTO ptmCmpSku : list) {
                TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl());
                if (TaskStatus.FINISH.equals(taskStatus)) {
                    FetchUrlResult fetchUrlResult = fetchDubboService.getProductsByUrl(ptmCmpSku.getId(),
                            ptmCmpSku.getWebsite(), ptmCmpSku.getUrl());
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
