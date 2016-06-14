package hasoffer.core.utils;


import hasoffer.base.utils.BigDecimalUtil;
import hasoffer.base.utils.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/10/19
 */
public class PriceUtil {

    public static BigDecimal getDiscount(BigDecimal oriPrice, BigDecimal curPrice) {
        if (oriPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimalUtil.subtract(oriPrice, curPrice).divide(oriPrice, 2);
    }

    public static BigDecimal getPrice(BigDecimal price) {
        return price.setScale(2, BigDecimal.ROUND_FLOOR);
    }

    public static int compare(BigDecimal price1, BigDecimal price2) {
        return getPrice(price1).compareTo(getPrice(price2));
    }

    public static float getPrice(String priceStr) {
        if (NumberUtils.isNumber(priceStr)) {
            return Float.valueOf(priceStr);
        }
        priceStr = StringUtils.filterAndTrim(priceStr, Arrays.asList(","));
        Pattern pattern = Pattern.compile("[R|r]s\\.??\\s*?(\\d+\\.??\\d+)");
        Matcher m = pattern.matcher(priceStr);
        if (m.find()) {
            String p = m.group(1);
            return Float.valueOf(p);
        }
        return 0.0f;
    }

    public static void main(String[] args) {
        String price = "rs. 569.23";//Rs.56.2 Rs. 8974.22
        System.out.println(getPrice(price));
    }
}
