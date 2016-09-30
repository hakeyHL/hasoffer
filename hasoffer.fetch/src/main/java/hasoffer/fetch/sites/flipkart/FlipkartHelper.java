package hasoffer.fetch.sites.flipkart;

import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.UrlUtils;
import hasoffer.fetch.helper.AffliIdHelper;

import java.util.Random;

/**
 * Date:2015/12/30
 */
public class FlipkartHelper {

    public static String getProductIdByUrl(String pageUrl) {

        pageUrl = getCleanUrl(pageUrl);

        int index = pageUrl.indexOf("?");
        if (index > 0) {
            pageUrl = pageUrl.substring(0, index);
        }

        return pageUrl.substring(pageUrl.lastIndexOf("/") + 1);
    }

    public static String getSkuIdByUrl(String pageUrl) {

        pageUrl = getCleanUrl(pageUrl);

        return UrlUtils.getParam(pageUrl, "pid");
    }

    public static String getCleanUrl(String url) {
        int index = url.indexOf("?");

        if (index < 0) {
            return url;
        }

        String pid = UrlUtils.getParam(url, "pid");
        if (StringUtils.isEmpty(pid)) {
            //如果不存在pid直接返回
            return url;
        } else {
            return url.substring(0, index) + "?pid=" + pid;
        }
    }

    /**
     * #目标
     * http://dl.flipkart.com/dl/alcatel-onetouch-idol-X-6040d/p/itmdthghx79dtzwf?affid=affiliate357
     * <p/>
     * #可能的情况
     * http://www.flipkart.com/karbonn-k106s/p/itmeabrpjprkbmx5
     * http://dl.flipkart.com/dl/karbonn-k106s/p/itmeabrpjprkbmx5
     *
     * @param url
     * @return
     */
    public static String getDeeplink(final String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        String dl = getCleanUrl(url);

        if (dl.contains("www.flipkart.com")) {
            dl = dl.replace("www.flipkart.com", "dl.flipkart.com/dl");
        }

        return dl;
    }

    public static String getUrlByDeeplink(final String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        String dl = url;

        if (url.contains("dl.flipkart.com/dl")) {
            return dl.replace("dl.flipkart.com/dl", "www.flipkart.com");
        }

        return url;
    }

    public static String getUrlWithAff(String url, String[] affs) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        url = getCleanUrl(url);

        return appendAff(url, affs);
    }

    public static String getDealUrlWithAff(String url, String[] affs) {
        url = getDeeplink(url);
        return appendAff(url, affs);
    }

    private static String appendAff(String url, String[] affs) {
        MarketChannel marketChannelName = null;
        for (String str : affs) {
            MarketChannel[] values = MarketChannel.values();
            for (MarketChannel marketChannel : values) {
                if (marketChannel.name().equals(str)) {
                    marketChannelName = marketChannel;
                    break;
                }
            }
        }
        StringBuffer sb = new StringBuffer(url);

        String affid = AffliIdHelper.getAffiIdByWebsite(Website.FLIPKART, marketChannelName);
        //在这里设置一个随机,网盟占1/10 概率
        int randomInt = new Random().nextInt(10);
        if (randomInt == 5) {
            //https://dl.flipkart.com/dl/yonex-carbonex-6000df-g4-strung-badminton-racquet/p/itmdfyr9jwgzd9dv?pid=RAQDFYR9JWGZD9DV&affid=raymondzh&affExtParam1=103662&affExtParam2=channel_deviceId_userid
            //raymondzh  103662  channel_deviceId_userid
            affid = "raymondzh";
            if (sb.indexOf("?") > 0) {
                sb.append("&affid=").append(affid);
            } else {
                sb.append("?affid=").append(affid);
            }
            if (affs != null && affs.length >= 1) {
                //affExtParam1 目前是固定的 103662
                //affExtParam2 是 channel_deviceId_userid

                sb.append("&affExtParam1=").append("103662");

                String channel_deviceId_userid = "";
                //按照第一个是渠道,第二个是设备id,第三个是用户id去拼接,如果不存在就是0
                //渠道要用特殊符号表示

                //要判断第一个是什么,第二个是什么,第三个是什么

                //暂时按照约定顺序 1 渠道 2设备 3  用户id

                //如果size 是2 是没有用户id
                switch (affs.length) {
                    case 2:
                        channel_deviceId_userid += AffliIdHelper.getAffAlia(affs[0]) + "_" + affs[1] + "_0";
                        break;
                    case 3:
                        channel_deviceId_userid += AffliIdHelper.getAffAlia(affs[0]) + "_" + affs[1] + "_" + affs[2];
                        break;
                    default:
                }
                sb.append("&affExtParam2=").append(channel_deviceId_userid);
            /*    for (String aff : affs) {
                    channel_deviceId_userid += aff + "_";
                }
                channel_deviceId_userid = channel_deviceId_userid.substring(0, channel_deviceId_userid.lastIndexOf("_"));
                sb.append("&affExtParam2=").append(channel_deviceId_userid);*/
            }

        } else {
            if (sb.indexOf("?") > 0) {
                sb.append("&affid=").append(affid);
            } else {
                sb.append("?affid=").append(affid);
            }

            if (affs != null && affs.length >= 1) {
//            int i = 1;
//            for (String aff : affs) {
//                sb.append("&affExtParam").append(i++).append("=").append(aff);
//            }
                sb.append("&affExtParam1=").append(affs[0]);

                if (affs.length >= 2) {
                    String deviceUser = affs[1];
                    if (affs.length == 3) {
                        deviceUser += "_" + affs[2];
                    }
                    sb.append("&affExtParam2=").append(deviceUser);
                }
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String url = "https://www.flipkart.com/philips-qt4000-15-pro-skin-advanced-trimmer-men/p/itmdpgxmahh9kujg?pid=SHVDPGXHU2XNTYHC&fm=merchandising&iid=M_85bae8f1-8b0c-4279-86d0-75a3423cef65.2644f0e4-5ed4-4f66-be7f-54cc5e3478fd&otracker=hp_omu_Flipkart+Assured_1_2644f0e4-5ed4-4f66-be7f-54cc5e3478fd_2644f0e4-5ed4-4f66-be7f-54cc5e3478fd_1";
        String[] ss = new String[]{"a", "b", "c"};
        String s = getUrlWithAff(url, ss);
        System.out.println(s);
    }
}
