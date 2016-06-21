package hasoffer.dubbo.api.fetch.service;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchUrlResult;

public interface IFetchDubboService {

    FetchResult getProductsKeyWord(Website webSite, String keyword, int startIndex, int endIndex);

    FetchUrlResult getProductsByUrl(Website webSite, String url) throws HttpFetchException, ContentParseException;

}
