package hasoffer.spider.service;

import hasoffer.base.model.Website;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.model.SpiderConfig;

import java.util.List;

public interface ISpiderConfigService {

    List<Website> findSupportWebSite(PageType pageType);


    SpiderConfig findByWebsite(Website website, PageType pageType);

    /**
     * @param pageType
     * @param apply    "Y" or "N"
     * @return
     */
    List<SpiderConfig> findByPageType(PageType pageType, String apply);

}
