package hasoffer.dubbo.api.fetch.service;

import hasoffer.base.model.Website;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchUrlResult;

public interface IFetchDubboService {

    FetchResult getProductsKeyWord(Website webSite, String keyword, int startIndex, int endIndex);

    FetchUrlResult getProductsByUrl(Long skuId, Website webSite, String url);

}
