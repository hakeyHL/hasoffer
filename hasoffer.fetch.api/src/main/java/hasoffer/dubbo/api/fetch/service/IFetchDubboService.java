package hasoffer.dubbo.api.fetch.service;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.model.FetchCompareWebsiteResult;
import hasoffer.spider.model.FetchDealResult;
import hasoffer.spider.model.FetchResult;

public interface IFetchDubboService {

/***********************************deal 抓取相关*************************************************/
/*
    用途：用deal网站爬取一些deal数据
    要点：一般从某一个固定页面，返回一组可以封装为appdeal的数据
    注意：
            1.由于抓取的结果是存放在一个redis一个list型的数据结构中的，并且deal抓取一般要求选择符合时效性的数据，所以不需要去关心缓存时间的问题
            2.deal数据的重复，由客户端在分析抓取结果，生成deal数据的时候去重，抓取的服务端，只返回固定条件筛选过的结果
 */

    /**
     * 发送deal抓取请求
     * 此处不设置缓存时间
     */
    void sendDealTask(Website website, TaskLevel taskLevel);

    /**
     * 获取抓取的deal信息
     */
    FetchDealResult getDealInfo(Website website);

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
     * @param website
     * @param url
     * @param taskTarget 任务目标
     * @param taskLevel  任务级别
     */
    void sendUrlTask(Website website, String url, TaskTarget taskTarget, TaskLevel taskLevel);

    /**
     *
     * @param website
     * @param url
     * @param taskTarget
     * @param taskLevel
     * @param id      该参数用来记录一些客户端用来区分结果的id信息
     */
    void sendUrlTask(Website website, String url, TaskTarget taskTarget, TaskLevel taskLevel, long id);

    /**
     * 获取URL更新的结果
     *
     * @return
     */
    String popFetchUrlResult(TaskTarget taskTarget);

/*************************************************************************************************/


/***********************************比价网站的数据抓取********************************************/
/*
    用途：根据比价网站的一个product url，得到一个product和一个List<PtmCmpSku>的数据
    要点：url来源一般是一个比价网站，每一个productUrl都能获得一个ptmproduct和一组ptmcmpsku
    注意：
            1.由于抓取的结果是存放在一个redis一个list型的数据结构中的，而且比较网站详情页的数据抓取数据必须做到定期清除，所以暂不缓存时间的问题
            2.deal数据的重复，由客户端在分析抓取结果，生成deal数据的时候去重，抓取的服务端，只返回固定条件筛选过的结果
            3.此处设置的缓存只是为了避免在一定时间内，出现同样的url请求，浪费性能，这个时间暂时由客户端设置，之后可以考虑由服务端来控制
 */

    /**
     * 发送一个比较网站的详情页的url抓取请求
     *
     * @param website   比价网站的名称
     * @param url       比价网站的某个商品的详情页
     * @param taskLevel 任务优先级
     */
    void sendCompareWebsiteFetchTask(Website website, String url, TaskLevel taskLevel, long categoryId);


    FetchCompareWebsiteResult getCompareWebsiteFetchResult(Website webSite);

/*************************************************************************************************/
}
