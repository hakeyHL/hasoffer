package hasoffer.api.worker;

import hasoffer.core.system.IAppService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by HL on 2016/10/17.
 */
public class UrmSignAlertWorker implements Runnable {
    private IAppService appService;

    public UrmSignAlertWorker(IAppService appService) {
        this.appService = appService;
    }

    @Override
    public void run() {
        System.out.println("execute user sign alert .");
        System.out.println("ThreadId is :" + Thread.currentThread().getId() + "  and current time is :  " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        appService.checkAndAlertUser2Sign();
    }
}
