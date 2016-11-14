package hasoffer.dubbo.api.fetch.service;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.model.FetchCompareWebsiteResult;
import hasoffer.spider.model.FetchDealResult;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchUrlResult;

public interface IFetchDubboService {

/***********************************deal 抓取相关*************************************************/
/*
    用途：用deal网站爬取一些deal数据
    要点：一般从某一个固定页面，返回一组可以封装为appdeal的数据
 */

    /**
     * 发送deal抓取请求
     * 此处不设置缓存时间
     */
    void sendDealTask(Website website, TaskLevel taskLevel);

    /**
     * 获取抓取的deal信息
     */
    FetchDealResult getDealInfo(Website website, long expireSeconds, TaskLevel taskLevel);

/*************************************************************************************************/


/***********************************关键字抓取相关************************************************/
/*
    用途：根据某个关键字，从各个网站搜索得到结果
    要点：解析搜索结果页面，得到一个List<PtmCmpSku>的数据，其中包含各个网站的数据
 */

    /**
     * 获取结果
     *
     * @param webSite
     * @param keyword
     * @return
     */
    FetchResult getProductsKeyWord(Website webSite, String keyword);

    /**
     * 提交关键词任务
     *
     * @param website
     * @param keyword
     */
    void sendKeyWordTask(Website website, String keyword);

    /**
     * 获取关键词任务对应的状态
     *
     * @param webSite
     * @param keyword
     * @return
     */
    TaskStatus getKeyWordTaskStatus(Website webSite, String keyword);

/*************************************************************************************************/

/***********************************url更新相关***************************************************/
/*
    用途：根据某个url，从响应的页面得到单个sku的信息
    要点：一个url只会对应一个ptmcmpsku,更新数据的时候按照url的md5值去更新
 */

    /**
     * 提交URL更新任务，该任务级别默认为TaskLevel.LEVEL_5(最低)。
     *
     * @param website
     * @param url
     */
    void sendUrlTask(Website website, String url);

    /**
     * 提交URL更新任务
     *
     * @param website
     * @param url
     * @param taskLevel 任务优先度，分为5级，TaskLevel.LEVEL_1(最高)，TaskLevel.LEVEL_5(最低)。
     */
    void sendUrlTask(Website website, String url, TaskLevel taskLevel);


    /**
     * @param website
     * @param url
     * @param expireSeconds
     * @param taskTarget    任务目标
     * @param taskLevel     任务级别
     */
    void sendUrlTask(Website website, String url, Long expireSeconds, TaskTarget taskTarget, TaskLevel taskLevel);

    /**
     * 提交URL更新任务
     *
     * @param website
     * @param url
     * @param seconds   缓存时间是多少
     * @param taskLevel 任务优先度，分为5级，TaskLevel.LEVEL_1(最高)，TaskLevel.LEVEL_5(最低)。
     */
    void sendUrlTask(Website website, String url, long seconds, TaskLevel taskLevel);

    /**
     * 获取URL更新的结果
     *
     * @return
     */
    String popFetchUrlResult(TaskTarget taskTarget);

    /**
     * 获取URL任务的状态。
     *
     * @param website
     * @param url
     * @return
     */
    TaskStatus getUrlTaskStatus(Website website, String url, long expireSeconds);

    /**
     * 获取URL任务的结果
     *
     * @param webSite
     * @param url
     * @param expireSeconds 过期时间
     * @return
     */
    FetchUrlResult getProductsByUrl(Website webSite, String url, long expireSeconds);

/*************************************************************************************************/

    /***********************************
     * 比价网站的数据抓取
     ********************************************/
/*
    用途：根据比价网站的一个product url，得到一个product和一个List<PtmCmpSku>的数据
    要点：url来源一般是一个比价网站，每一个productUrl都能获得一个ptmproduct和一组ptmcmpsku
 */

    void sendCompareWebsiteFetchTask(Website website, String url, TaskLevel taskLevel, long expireSeconds);


    FetchCompareWebsiteResult getCompareWebsiteFetchResult(Website webSite, String url, long expireSeconds);

/*************************************************************************************************/
}
