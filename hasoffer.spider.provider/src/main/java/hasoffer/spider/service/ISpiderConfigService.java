package hasoffer.spider.service;

import hasoffer.base.model.Website;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.model.SpiderConfig;

public interface ISpiderConfigService {

    SpiderConfig findByWebsite(Website website, PageType pageType);

}
