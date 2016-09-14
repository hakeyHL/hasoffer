package hasoffer.spider.service;

import hasoffer.base.model.Website;
import hasoffer.spider.model.SpiderConfig;

public interface ISpiderConfigService {

    SpiderConfig findById(long id);

    SpiderConfig findByWebsite(Website website);

}
