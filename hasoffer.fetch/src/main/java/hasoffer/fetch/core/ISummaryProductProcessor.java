package hasoffer.fetch.core;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.fetch.model.FetchedProduct;

/**
 * Created on 2016/2/29.
 */
public interface ISummaryProductProcessor {

    //urlModel:      http://www.xxx.com/.....
    FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException;

}
