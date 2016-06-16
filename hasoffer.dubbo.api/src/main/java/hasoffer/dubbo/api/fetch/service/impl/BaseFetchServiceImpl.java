package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.dubbo.api.fetch.po.FetchResult;
import hasoffer.dubbo.api.fetch.service.IFetchService;

public class BaseFetchServiceImpl implements IFetchService {
    @Override
    public FetchResult getProductsKeyWord(Website webSite, String keyword, int startIndex, int endIndex) {
        return null;
    }

    @Override
    public FetchResult getProductsByUrl(String webSite, String url) throws HttpFetchException, ContentParseException {
        return null;
    }

    @Override
    public FetchResult fetch(String queryStr) {
        return null;
    }

    @Override
    public void cache(FetchResult fetchResult) {

    }
}
