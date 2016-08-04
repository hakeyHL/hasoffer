package hasoffer.admin.fetch.service.impl;

import hasoffer.core.product.ICmpSkuService;
import hasoffer.dubbo.api.fetch.service.IFetchUpdateService;
import hasoffer.spider.model.FetchUrlResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created on 2016/8/4.
 */
@Service
public class FetchUpdateServiceImpl implements IFetchUpdateService {

    @Resource
    ICmpSkuService cmpSkuService;

    @Override
    public void updatePtmCmpSkuInfo(FetchUrlResult fetchUrlResult) {

//        fetchUrlResult.

    }
}
