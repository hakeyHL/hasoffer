package hasoffer.dubbo.api.fetch.service;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedDealInfo;

import java.util.List;

public interface IFetchDubboService {

    /**
     * 发送deal抓取请求
     */
    void sendDealTask(Website website, long cacheSeconds, TaskLevel taskLevel);

    /**
     * 获取deal抓取的状态
     */
    TaskStatus getDealTaskStatus(Website website, long expireSeconds);

    /**
     * 获取抓取的deal信息
     */
    List<FetchedDealInfo> getDesidimeDealInfo();

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
     * 提交URL更新任务
     *
     * @param website
     * @param url
     * @param seconds   缓存时间是多少
     * @param taskLevel 任务优先度，分为5级，TaskLevel.LEVEL_1(最高)，TaskLevel.LEVEL_5(最低)。
     */
    void sendUrlTask(Website website, String url, long seconds, TaskLevel taskLevel);

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


}
