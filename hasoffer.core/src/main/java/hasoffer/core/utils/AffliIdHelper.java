package hasoffer.core.utils;

import hasoffer.base.enums.MarketChannel;

import java.util.Random;

/**
 * Created by hs on 2016年08月11日.
 * Time 10:30
 */
public class AffliIdHelper {

    //
    final static String[] LEOMASTER_FLIDS = new String[]{"hlhakeygm", "oliviersl", "wuningSFg"};
    final static String[] SHANCHUAN_FLIDS = new String[]{"160082642", "286867656", "289063282", "514330076", "602074420", "943546560"};
    final static String[] NINEAPPS_FLIDS = new String[]{"Sunyukunj", "gczyfw201", "xyangryrg", "zhouxixi0", "harveyouo", "allenooou"};
    final static String[] GOOGLEPLAY_FLIDS = new String[]{"115377600"};
    final static String[] OTHER_FLIDS = new String[]{"120527343"};
    final static String[] ZUK_FLIDS = new String[]{"747306881"};
    final static String[] SNIDS = new String[]{"89037", "104658", "82856"};
    final static String[] SHIDS = new String[]{"none", "123"};

    public static String getAffiIds(MarketChannel marketChannel) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        if (marketChannel == null) {
            marketChannel = MarketChannel.TEST;
        }
        sb.append(getFKid(marketChannel.name()) + ",");
        sb.append(SNIDS[random.nextInt(SNIDS.length)] + ",").append(SHIDS[random.nextInt(SHIDS.length)]).toString();
        return sb.toString();
    }

    //public static String getAffiIdByWebsite(Website website, MarketChannel marketChannel) {
    //    Random random = new Random();
    //    switch (website.name()) {
    //        case "FLIPKART":
    //            return getFKid(marketChannel.name());
    //        case "SNAPDEAL":
    //            return SNIDS[random.nextInt(SNIDS.length)];
    //        case "SHOPCLUES":
    //            return SHIDS[random.nextInt(SHIDS.length)];
    //        default:
    //            return null;
    //    }
    //}

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            String shanchuan = getFKid("NINEAPPS");
            System.out.println(shanchuan);
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
}
