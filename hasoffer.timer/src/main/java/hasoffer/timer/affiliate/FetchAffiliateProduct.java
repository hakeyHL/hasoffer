package hasoffer.timer.affiliate;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * Created on 2016/3/8.
 */
//@Component
public class FetchAffiliateProduct implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(FetchAffiliateProduct.class);

    @Resource
    IDataBaseManager dbm;

    @Override
    public void run() {
        while(true){

            //todo 1-----------------n---------------n8n----
            //todo prodcutUrl     nextUrl         thdProduct

        }
    }
}
