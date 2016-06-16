package hasoffer.dubbo.api.fetch.service;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.dubbo.api.fetch.po.FetchResult;

public interface IFetchService {

    FetchResult getProductsKeyWord(Website webSite, String keyword, int startIndex, int endIndex);

    FetchResult getProductsByUrl(String webSite, String url) throws HttpFetchException, ContentParseException;

    FetchResult fetch(String queryStr);

    void cache(FetchResult fetchResult);
}
