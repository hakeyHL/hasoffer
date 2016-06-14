package hasoffer.base.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
	public static final BigDecimal MAX = BigDecimal.valueOf(1000000000000d);//10000亿
	public static final BigDecimal MIN = BigDecimal.valueOf(0d);

	public static String price(BigDecimal decimal) {
		if (decimal == null) {
			return "0.00";
		}
		return decimal.setScale(2, RoundingMode.FLOOR).toPlainString();
	}

	public static int compare(BigDecimal b1, BigDecimal b2) {
		return b1.compareTo(b2);
	}

	public static boolean biggerThanZero(BigDecimal b1) {
		return b1.compareTo(BigDecimal.ZERO) > 0;
	}

	public static String price(String value) {
		return price(new BigDecimal(value));
	}

	public static BigDecimal parse(String value) {
		return new BigDecimal(value).setScale(2, RoundingMode.FLOOR);
	}

	public static void main(String[] args) {
		System.out.println(parse("1.200000001"));
	}

	public static String price(BigDecimal b1, BigDecimal exchangeRate) {
		return price(b1.multiply(exchangeRate));
	}

	public static BigDecimal multiply(BigDecimal b1, int i1) {
		return b1.multiply(BigDecimal.valueOf(i1)).setScale(2, BigDecimal.ROUND_FLOOR);
	}

	public static BigDecimal multiply(BigDecimal b1, BigDecimal b2) {
		return b1.multiply(b2).setScale(2, BigDecimal.ROUND_FLOOR);
	}

	public static BigDecimal add(BigDecimal b1, String b2) {
		return b1.add(new BigDecimal(b2)).setScale(2, RoundingMode.FLOOR);
	}

	public static BigDecimal add(BigDecimal... bs) {
		BigDecimal result = BigDecimal.ZERO;

		if (bs != null) {
			for (BigDecimal b : bs) {
				result = result.add(b);
			}
		}

		return result.setScale(2, RoundingMode.FLOOR);
	}

	public static BigDecimal subtract(BigDecimal b1, BigDecimal b2) {
		return b1.subtract(b2).setScale(2, RoundingMode.FLOOR);
	}

	public static BigDecimal divide(BigDecimal b1, BigDecimal b2) {
		return b1.divide(b2, 2, RoundingMode.FLOOR).setScale(2, RoundingMode.FLOOR);
	}

}
