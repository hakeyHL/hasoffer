package hasoffer.fetch.helper;

import hasoffer.base.model.Website;

import java.util.Random;

/**
 * Created by hs on 2016年08月11日.
 * Time 10:30
 */
public class IdHelper {
    final static String[] FLIDS = new String[]{"affiliate357", "xyangryrg", "zhouxixi0", "harveyouo", "allenooou", "747306881", "hlhakeygm", "oliviersl", "wuningSFg"};
    final static String[] SNIDS = new String[]{"82856", "89037", "104658", "104664", "104663", "104705", "104659", "104717", "104726"};
    final static String[] SHIDS = new String[]{"none", "none", "none", "none", "none", "none", "none", "none", "none", "none", "none", "none"};

    public static String getAffiIds() {
        Random random = new Random();
        return new StringBuilder().append(FLIDS[random.nextInt(FLIDS.length)] + ",").append(SNIDS[random.nextInt(SNIDS.length)] + ",").append(SHIDS[random.nextInt(SHIDS.length)]).toString();
    }

    public static String getAffiIdByWebsite(Website website) {
        Random random = new Random();
        switch (website.name()) {
            case "FLIPKART":
                return FLIDS[random.nextInt(FLIDS.length)];
            case "SNAPDEAL":
                return FLIDS[random.nextInt(SNIDS.length)];
            case "SHOPCLUES":
                return FLIDS[random.nextInt(SHIDS.length)];
            default:
                return null;
        }
    }

    public static void main(String[] args) {
        String affiIds = IdHelper.getAffiIds();
        System.out.printf(affiIds);
    }
}
