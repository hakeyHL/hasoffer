package hasoffer.fetch.helper;

import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by hs on 2016年08月11日.
 * Time 10:30
 */
public class AffliIdHelper {

    final static String[] LEOMASTER_FLIDS = new String[]{"hlhakeygm", "oliviersl", "wuningSFg"};
    final static String[] NINEAPPS_FLIDS = new String[]{"Sunyukunj", "gczyfw201", "xyangryrg", "zhouxixi0", "harveyouo", "allenooou"};
    //    final static String[] SHANCHUAN_FLIDS = new String[]{"160082642", "286867656", "289063282", "514330076", "602074420", "943546560"};
    final static String[] SHANCHUAN_FLIDS = new String[]{"huangmint", "huangmint", "wangshuom", "514330076", "602074420", "943546560"};
    final static String[] GOOGLEPLAY_FLIDS = new String[]{"115377600"};
    final static String[] OTHER_FLIDS = new String[]{"120527343"};
    final static String[] ZUK_FLIDS = new String[]{"747306881"};
    final static String[] SNIDS = new String[]{"89037", "104658", "82856"};
    final static String[] SHIDS = new String[]{"none", "123"};
    static Map<MarketChannel, String> affAliaMap = new HashMap<>();

    static {
        affAliaMap.put(MarketChannel.OFFICIAL, "001");
        affAliaMap.put(MarketChannel.GOOGLEPLAY, "0012");
        affAliaMap.put(MarketChannel.PREASSEMBLE, "0013");
        affAliaMap.put(MarketChannel.YUANBIN, "0014");
        affAliaMap.put(MarketChannel.WANGMENG, "0015");
        affAliaMap.put(MarketChannel.SHANCHUAN, "0016");
        affAliaMap.put(MarketChannel.DUOBAO, "0017");
        affAliaMap.put(MarketChannel.TEST, "0018");
        affAliaMap.put(MarketChannel.LeoMaster, "0019");
        affAliaMap.put(MarketChannel.NINEAPPS, "00110");
        affAliaMap.put(MarketChannel.KUYU, "00111");
        affAliaMap.put(MarketChannel.ZUK, "00112");
        affAliaMap.put(MarketChannel.NONE, "00113");
    }

    public static String getAffiIds(MarketChannel marketChannel) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        sb.append(getFKid(marketChannel.name()) + ",");
        sb.append(SNIDS[random.nextInt(SNIDS.length)] + ",").append(SHIDS[random.nextInt(SHIDS.length)]).toString();
        return sb.toString();
    }

    public static String getAffiIdByWebsite(Website website, MarketChannel marketChannel) {
        if (marketChannel == null) {
            marketChannel = MarketChannel.TEST;
        }
        Random random = new Random();
        switch (website.name()) {
            case "FLIPKART":
                return getFKid(marketChannel.name());
            case "SNAPDEAL":
                return SNIDS[random.nextInt(SNIDS.length)];
            case "SHOPCLUES":
                return SHIDS[random.nextInt(SHIDS.length)];
            default:
                return null;
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10000; i++) {
//            String affiIds = AffliIdHelper.getAffiIdByWebsite(Website.SHOPCLUES);
//            System.out.printf(affiIds);
        }
    }

    public static String getFKid(String marketChannel) {
        Random random = new Random();
        switch (marketChannel) {
            case "LeoMaster":
                return LEOMASTER_FLIDS[random.nextInt(LEOMASTER_FLIDS.length)];
            case "NINEAPPS":
                return NINEAPPS_FLIDS[random.nextInt(NINEAPPS_FLIDS.length)];
            case "SHANCHUAN":
                return SHANCHUAN_FLIDS[random.nextInt(SHANCHUAN_FLIDS.length)];
            case "GOOGLEPLAY":
                return GOOGLEPLAY_FLIDS[0];
            case "ZUK":
                return ZUK_FLIDS[0];
            default:
                return OTHER_FLIDS[0];
        }
    }

    public static String getAffAlia(String marketChannel) {
        MarketChannel marketChannelName = null;
        MarketChannel[] values = MarketChannel.values();
        for (MarketChannel channel : values) {
            if (channel.name().equals(marketChannel)) {
                marketChannelName = channel;
                break;
            }
        }
        if (marketChannelName != null) {
            return affAliaMap.get(marketChannelName);
        }
        return "noMarketChannel";
    }

}
