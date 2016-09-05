package hasoffer.api.controller.test;

import java.math.BigDecimal;

/**
 * Created by hs on 2016年09月05日.
 * Time 23:59
 */
public class T {
    public static void main(String[] args) {
        double minPrice = 92;
        double maxPrice = 98;
        double middlePrice = minPrice / 2 + maxPrice / 2;
        BigDecimal of3 = (BigDecimal.valueOf(middlePrice).subtract(BigDecimal.valueOf(minPrice))).multiply(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP));
        BigDecimal a = BigDecimal.valueOf(minPrice).subtract(of3);
        BigDecimal b = BigDecimal.valueOf(minPrice).add(of3);

        System.out.println(middlePrice);
        System.out.println(a.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
        System.out.println(b.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
    }
}
