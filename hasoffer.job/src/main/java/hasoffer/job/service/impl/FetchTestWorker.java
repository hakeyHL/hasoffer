package hasoffer.job.service.impl;

import hasoffer.base.enums.TaskStatus;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.model.FetchUrlResult;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FetchTestWorker implements Runnable {

    List<PtmCmpSku> list;
    IFetchDubboService fetchDubboService;
    File file;
    private Logger logger = LoggerFactory.getLogger(FetchTestWorker.class);

    public FetchTestWorker(List<PtmCmpSku> list, IFetchDubboService fetchDubboService, File file) {
        this.list = list;
        this.fetchDubboService = fetchDubboService;
        this.file = file;
    }

    @Override
    public void run() {
        while (list != null && !list.isEmpty()) {
            List<PtmCmpSku> resultList = new ArrayList<PtmCmpSku>();
            for (PtmCmpSku ptmCmpSku : list) {
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
