package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.app.vo.DealCommentVo;
import hasoffer.core.app.vo.DealVo;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.app.AppDealComment;
import hasoffer.core.persistence.po.app.AppDealThumb;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.solr.DealIndexServiceImpl;
import hasoffer.core.product.solr.DealModel;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.IAppService;
import hasoffer.core.utils.JsonHelper;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by hs on 2016/7/25.
 * 专用于Deal的Controller
 */
@Controller
@RequestMapping("deal")
public class AppDealController {
    Logger logger = LoggerFactory.getLogger(AppDealController.class);
    @Resource
    IDealService dealService;
    @Resource
    IAppService appService;
    @Resource
    DealIndexServiceImpl indexService;
    @Resource
    ICacheService<UrmUser> userICacheService;

    /**
     * 获取商品相关deal列表
     *
     * @return modelAndView
     */
    @RequestMapping("product")
    public ModelAndView getDealsByProductTitle(@RequestParam(defaultValue = "") String title,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int pageSize,
                                               HttpServletResponse response) {
        //TODO 从Solr搜索Deal列表
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        List<DealVo> deals = new ArrayList<DealVo>();
        PropertyFilter propertyFilter = JsonHelper.filterProperty(new String[]{"exp", "extra", "link", "priceDescription", "image"});
        //先展示与浏览商品同类的deal
        List<DealModel> dealModels = indexService.simpleSearch(title, page, pageSize);
//        System.out.println("search from solr dealModels  :" + dealModels.size());
        if (dealModels != null && dealModels.size() > 0) {
            for (DealModel dealModel : dealModels) {
                if (dealModel.getExpireTime().compareTo(new Date()) != 1 && dealModel.isDisplay()) {
                    DealVo dealVo = new DealVo();
                    dealVo.setLogoUrl(dealModel.getWebsite() == null ? "" : WebsiteHelper.getBiggerLogoUrl(Website.valueOf(dealModel.getWebsite())));
                    dealVo.setTitle(dealModel.getTitle());
                    dealVo.setWebsite(Website.valueOf(dealModel.getWebsite()).name());
                    dealVo.setId(dealModel.getId());
                    dealVo.setDiscount(dealModel.getDiscount());
                    dealVo.setDeepLink(dealModel.getLinkUrl() == null ? "" : WebsiteHelper.getDealUrlWithAff(Website.valueOf(dealModel.getWebsite()), dealModel.getLinkUrl(), new String[]{deviceInfo.getMarketChannel().name(), deviceId}));
                    deals.add(dealVo);
                }
            }
//            System.out.println("from solr get   :" + deals.size());
        }
        //再展示手机类deal id或parentid 为 5 level小于等于3
        PageableResult pageableResult = appService.getDeals(page + 0l, pageSize + 0l);
        if (pageableResult != null && pageableResult.getData() != null && pageableResult.getData().size() > 0) {
            List<AppDeal> list = pageableResult.getData();
//            System.out.println("search from mysql get   :" + list.size());
            List<DealVo> mobileDeals = new ArrayList<DealVo>();
            Iterator<AppDeal> dealIterator = list.iterator();
            while (dealIterator.hasNext()) {
                AppDeal appDeal = dealIterator.next();
                if (appDeal.getDealCategoryId() == 5) {
                    DealVo dealVo = new DealVo();
                    dealVo.setLogoUrl(appDeal.getWebsite() == null ? "" : WebsiteHelper.getBiggerLogoUrl(appDeal.getWebsite()));
                    dealVo.setTitle(appDeal.getTitle());
                    dealVo.setWebsite(appDeal.getWebsite().name());
                    dealVo.setId(appDeal.getId());
                    dealVo.setDiscount(appDeal.getDiscount());
                    dealVo.setDeepLink(appDeal.getLinkUrl() == null ? "" : WebsiteHelper.getDealUrlWithAff(appDeal.getWebsite(), appDeal.getLinkUrl(), new String[]{deviceInfo.getMarketChannel().name(), deviceId}));
                    mobileDeals.add(dealVo);
                    dealIterator.remove();
                }
            }
//            System.out.println("mobile  get   :" + mobileDeals.size());
            deals.addAll(mobileDeals);
//            System.out.println("current size   :" + deals.size());
            //其他deal按照点击次数排序
            Collections.sort(list, new Comparator<AppDeal>() {
                @Override
                public int compare(AppDeal o1, AppDeal o2) {
                    if (o1.getDealClickCount() > o2.getDealClickCount()) {
                        return -1;
                    } else if (o1.getDealClickCount() < o2.getDealClickCount()) {
                        return 1;
                    }
                    return 0;
                }
            });
//            System.out.println("last  list size   :" + list.size());
            for (AppDeal appDeal : list) {
                DealVo dealVo = new DealVo();
                dealVo.setLogoUrl(appDeal.getWebsite() == null ? "" : WebsiteHelper.getBiggerLogoUrl(appDeal.getWebsite()));
                dealVo.setTitle(appDeal.getTitle());
                dealVo.setWebsite(appDeal.getWebsite().name());
                dealVo.setId(appDeal.getId());
                dealVo.setDiscount(appDeal.getDiscount());
                dealVo.setDeepLink(appDeal.getLinkUrl() == null ? "" : WebsiteHelper.getDealUrlWithAff(appDeal.getWebsite(), appDeal.getLinkUrl(), new String[]{deviceInfo.getMarketChannel().name(), deviceId}));
                deals.add(dealVo);
            }
//            System.out.println("current  deals size   :" + deals.size());
        }
        Map map = new HashMap();
        map.put("deals", deals);
        jsonObject.put("data", JSONObject.toJSON(map));
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject, propertyFilter), response);
        return null;
    }

    @RequestMapping("info")
    public String getDealById(@RequestParam(defaultValue = "0") Long id, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        AppDeal appDeal = dealService.getDealById(id);
        Map hashMap = new HashMap<>();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        hashMap.put("provisions", "• Taxs are applicable.\n• This offer cannot be clubbed with any other ongoing offer.\n• Offer cannot be redeemed for cash.\n• No coupon code required.\n• Company has the right to end this offer without prior notice.\n");
        if (appDeal != null) {
//            logger.info("has this deal " + id);
            hashMap.put("description", appDeal.getDescription());
        }
        jsonObject.put("data", hashMap);
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    @RequestMapping("thumb")
    public String thumbDeal(@RequestParam(defaultValue = "0") String action,
                            @RequestParam(defaultValue = "0") Long dealId,
                            HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        Map hashMap = new HashMap<>();
        int intAction = Integer.parseInt(action);
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        UrmUser urmUser;
        Date currentDate = new Date();
        if (dealId != 0) {
            AppDeal dealDetail = appService.getDealDetail(dealId);
            if (dealDetail != null) {
                String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
                if (StringUtils.isNotBlank(userToken)) {
                    String key = "user_" + userToken;
                    urmUser = userICacheService.get(UrmUser.class, key, 0);
                    if (urmUser == null) {
                        urmUser = appService.getUserByUserToken(userToken);
                        if (urmUser != null) {
                            userICacheService.add(key, urmUser, TimeUtils.SECONDS_OF_1_DAY);
                        }
                    }
                    if (urmUser != null) {
                        //查出之前记录
                        AppDealThumb appDealThumb = dealService.getDealThumbByUidDid(urmUser.getId(), dealId);
                        if (appDealThumb != null) {
                            appDealThumb.setUpdateTime(currentDate.getTime());
                            switch (intAction) {
                                case 1:
                                    //点赞
                                    //之前有,判断之前的状态从而进行相应的操作
                                    if (appDealThumb.getAction() == 1) {
                                        appDealThumb.setAction(0);
                                    } else {
                                        appDealThumb.setAction(1);
                                    }
                                    break;
                                case 2:
                                    //点踩
                                    if (appDealThumb.getAction() == -1) {
                                        appDealThumb.setAction(0);
                                    } else {
                                        appDealThumb.setAction(-1);
                                    }
                                    break;
                                default:
                            }
                            //update
                            dealService.updateDealThumb(appDealThumb);
                        } else {
                            //之前没有,直接置状态
                            if (intAction != 0) {
                                appDealThumb = new AppDealThumb(urmUser.getId(), intAction, currentDate.getTime(), currentDate.getTime(), dealId);
                                //创建
                                dealService.createThumb(appDealThumb);
                            }
                        }
                    } else {
                        jsonObject.put("errorCode", "10000");
                        jsonObject.put("msg", "user record is not exist --" + userToken);
                    }
                }
            } else {
                jsonObject.put("errorCode", "10000");
                jsonObject.put("msg", "deal record is not exist --" + dealId);
            }
        } else {
            jsonObject.put("errorCode", "10000");
            jsonObject.put("msg", "dealId is not exist .");
        }
        jsonObject.put("data", hashMap);
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    @RequestMapping("comment")
    public String commentDeal(@RequestParam(defaultValue = "0") Long dealId,
                              @RequestParam(defaultValue = "0") String anonymous,
                              @RequestParam(value = "") String content,
                              HttpServletResponse response) {
        //默认不匿名
        JSONObject jsonObject = new JSONObject();
        Map hashMap = new HashMap<>();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        Date currentDate = new Date();
        UrmUser urmUser;
        if (StringUtils.isNotEmpty(content)) {
            if (dealId != 0) {
                AppDeal appDeal = dealService.getDealById(dealId);
                if (appDeal != null) {
                    String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
                    if (StringUtils.isNotBlank(userToken)) {
                        String key = "user_" + userToken;
                        urmUser = userICacheService.get(UrmUser.class, key, 0);
                        if (urmUser == null) {
                            urmUser = appService.getUserByUserToken(userToken);
                            if (urmUser != null) {
                                userICacheService.add(key, urmUser, TimeUtils.SECONDS_OF_1_DAY);
                            }
                        }
                        if (urmUser != null) {
                            dealService.createAppComment(new AppDealComment(currentDate.getTime(),
                                    urmUser.getId(),
                                    content,
                                    Integer.parseInt(anonymous),
                                    dealId));
                        } else {
                            jsonObject.put("errorCode", "10000");
                            jsonObject.put("msg", "no record  for this userToken  : " + userToken);
                        }
                    } else {
                        jsonObject.put("errorCode", "10000");
                        jsonObject.put("msg", "userToken is required ");
                    }
                } else {
                    jsonObject.put("errorCode", "10000");
                    jsonObject.put("msg", "no record for this id : " + dealId);
                }
            } else {
                jsonObject.put("errorCode", "10000");
                jsonObject.put("msg", "dealId is required.");
            }
        } else {
            jsonObject.put("errorCode", "10000");
            jsonObject.put("msg", "comment content is required .");
        }
        jsonObject.put("data", hashMap);
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    @RequestMapping("comments")
    public String getDealComments(@RequestParam(defaultValue = "0") Long dealId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "5") int pageSize,
                                  HttpServletResponse response) {
        //默认不匿名
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        List<DealCommentVo> dealCommentVos = new ArrayList<>();
        if (dealId == 0) {
            jsonObject.put("errorCode", "10000");
            jsonObject.put("msg", "dealId is required .");
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        AppDeal appDeal = dealService.getDealById(dealId);
        if (appDeal == null) {
            jsonObject.put("errorCode", "10000");
            jsonObject.put("msg", "deal record is not exist for dealId : " + dealId);
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }

        PageableResult<AppDealComment> pageAbleDealComment = dealService.getPageAbleDealComment(dealId, page, pageSize);
        if (pageAbleDealComment != null) {
            List<AppDealComment> comments = pageAbleDealComment.getData();
            for (AppDealComment appDealComment : comments) {
                Long userId = appDealComment.getUserId();
                if (userId > 0) {
                    UrmUser urmUser = appService.getUserById(userId);
                    if (urmUser != null) {
                        String userName = urmUser.getUserName();
                        if (appDealComment.getIsAnonymous() == 1 && StringUtils.isNotEmpty(userName)) {
                            userName = userName.charAt(0) + "**" + userName.charAt(userName.length() - 1);
                        }
                        dealCommentVos.add(new DealCommentVo(getDifference2Date(new Date(),
                                new Date(appDealComment.getCreateTime())),
                                StringUtils.isEmpty(userName) ? "" : userName,
                                urmUser.getAvatarPath() == null ? "" : urmUser.getAvatarPath(),
                                appDealComment.getContent() == null ? "" : appDealComment.getContent()));
                    }

                }
            }
            jsonObject.put("data", dealCommentVos);
        } else {
            jsonObject.put("errorCode", "10000");
            jsonObject.put("msg", "no data in database.");
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    public String getDifference2Date(Date maxDate, Date comparedDate) {
        Long tempResult = maxDate.getTime() - comparedDate.getTime();
        long nd = 1000 * 24 * 60 * 60;//一天的毫秒数
        long nh = 1000 * 60 * 60;//一小时的毫秒数
        long nm = 1000 * 60;//一分钟的毫秒数
        int day = BigDecimal.valueOf(tempResult).divide(BigDecimal.valueOf(nd), BigDecimal.ROUND_HALF_UP).intValue();//计算差多少天
        int hour = BigDecimal.valueOf(tempResult).divide(BigDecimal.valueOf(nh), BigDecimal.ROUND_HALF_UP).intValue();//计算差多少天
        int min = BigDecimal.valueOf(tempResult).divide(BigDecimal.valueOf(nm), BigDecimal.ROUND_HALF_UP).intValue();//计算差多少天
        return day <= 0 ? hour <= 0 ? min + " mins ago " : hour + " hours ago " : day + " days ago ";
    }
}
