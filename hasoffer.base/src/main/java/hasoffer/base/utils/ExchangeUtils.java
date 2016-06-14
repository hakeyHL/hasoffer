package hasoffer.base.utils;

import hasoffer.base.model.CurrencyCode;
import hasoffer.base.model.ExchangeRate;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.model.YHRate;
import hasoffer.base.utils.http.HttpUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chengwei Zhang
 * Date : 2015/7/20 16:36
 */
public class ExchangeUtils {
	public static Map<CurrencyCode, BigDecimal> exchangeMap = new HashMap<CurrencyCode, BigDecimal>();

	static {
		exchangeMap.put(CurrencyCode.FJD, new BigDecimal("2.1487"));
		exchangeMap.put(CurrencyCode.STD, new BigDecimal("22513.5"));
		exchangeMap.put(CurrencyCode.MXN, new BigDecimal("17.0"));
		exchangeMap.put(CurrencyCode.SCR, new BigDecimal("12.99345"));
		exchangeMap.put(CurrencyCode.LVL, new BigDecimal("0.62055"));
		exchangeMap.put(CurrencyCode.CDF, new BigDecimal("928.0"));
		exchangeMap.put(CurrencyCode.BBD, new BigDecimal("2.0"));
		exchangeMap.put(CurrencyCode.GTQ, new BigDecimal("7.6075"));
		exchangeMap.put(CurrencyCode.CLP, new BigDecimal("706.094971"));
		exchangeMap.put(CurrencyCode.UGX, new BigDecimal("3305.0"));
		exchangeMap.put(CurrencyCode.HNL, new BigDecimal("22.249599"));
		exchangeMap.put(CurrencyCode.ZAR, new BigDecimal("14.57425"));
		exchangeMap.put(CurrencyCode.TND, new BigDecimal("2.01835"));
		exchangeMap.put(CurrencyCode.BSD, new BigDecimal("0.997905"));
		exchangeMap.put(CurrencyCode.SLL, new BigDecimal("4240.0"));
		exchangeMap.put(CurrencyCode.SDG, new BigDecimal("6.0934"));
		exchangeMap.put(CurrencyCode.IQD, new BigDecimal("1107.099976"));
		exchangeMap.put(CurrencyCode.CUP, new BigDecimal("1.0"));
		exchangeMap.put(CurrencyCode.GMD, new BigDecimal("39.43"));
		exchangeMap.put(CurrencyCode.TWD, new BigDecimal("32.867001"));
		exchangeMap.put(CurrencyCode.RSD, new BigDecimal("111.514999"));
		exchangeMap.put(CurrencyCode.DOP, new BigDecimal("45.275002"));
		exchangeMap.put(CurrencyCode.KMF, new BigDecimal("450.313751"));
		exchangeMap.put(CurrencyCode.MYR, new BigDecimal("4.2635"));
		exchangeMap.put(CurrencyCode.FKP, new BigDecimal("0.6632"));
		exchangeMap.put(CurrencyCode.XOF, new BigDecimal("600.418335"));
		exchangeMap.put(CurrencyCode.GEL, new BigDecimal("2.38"));
		exchangeMap.put(CurrencyCode.UYU, new BigDecimal("29.635"));
		exchangeMap.put(CurrencyCode.MAD, new BigDecimal("9.88295"));
		exchangeMap.put(CurrencyCode.CVE, new BigDecimal("101.370003"));
		exchangeMap.put(CurrencyCode.TOP, new BigDecimal("2.205214"));
		exchangeMap.put(CurrencyCode.AZN, new BigDecimal("1.04625"));
		exchangeMap.put(CurrencyCode.PGK, new BigDecimal("2.98655"));
		exchangeMap.put(CurrencyCode.OMR, new BigDecimal("0.38495"));
		exchangeMap.put(CurrencyCode.KES, new BigDecimal("101.9095"));
		exchangeMap.put(CurrencyCode.SEK, new BigDecimal("8.483"));
		exchangeMap.put(CurrencyCode.CNH, new BigDecimal("6.48955"));
		exchangeMap.put(CurrencyCode.UAH, new BigDecimal("22.765499"));
		exchangeMap.put(CurrencyCode.BTN, new BigDecimal("66.737503"));
		exchangeMap.put(CurrencyCode.GNF, new BigDecimal("7724.850098"));
		exchangeMap.put(CurrencyCode.MZN, new BigDecimal("51.5"));
		exchangeMap.put(CurrencyCode.ERN, new BigDecimal("16.780001"));
		exchangeMap.put(CurrencyCode.SVC, new BigDecimal("8.7282"));
		exchangeMap.put(CurrencyCode.ARS, new BigDecimal("9.6984"));
		exchangeMap.put(CurrencyCode.QAR, new BigDecimal("3.6417"));
		exchangeMap.put(CurrencyCode.IRR, new BigDecimal("30106.0"));
		exchangeMap.put(CurrencyCode.MRO, new BigDecimal("304.0"));
		exchangeMap.put(CurrencyCode.THB, new BigDecimal("35.910999"));
		exchangeMap.put(CurrencyCode.XPF, new BigDecimal("109.228104"));
		exchangeMap.put(CurrencyCode.CNY, new BigDecimal("6.42585"));
		exchangeMap.put(CurrencyCode.UZS, new BigDecimal("2750.429932"));
		exchangeMap.put(CurrencyCode.MXV, new BigDecimal("2.81"));
		exchangeMap.put(CurrencyCode.BDT, new BigDecimal("77.989952"));
		exchangeMap.put(CurrencyCode.LYD, new BigDecimal("1.3928"));
		exchangeMap.put(CurrencyCode.BMD, new BigDecimal("1.00005"));
		exchangeMap.put(CurrencyCode.PHP, new BigDecimal("47.185501"));
		exchangeMap.put(CurrencyCode.KWD, new BigDecimal("0.30365"));
		exchangeMap.put(CurrencyCode.RUB, new BigDecimal("69.192497"));
		exchangeMap.put(CurrencyCode.PYG, new BigDecimal("5767.464844"));
		exchangeMap.put(CurrencyCode.ISK, new BigDecimal("129.270004"));
		exchangeMap.put(CurrencyCode.JMD, new BigDecimal("119.790001"));
		exchangeMap.put(CurrencyCode.COP, new BigDecimal("3301.550049"));
		exchangeMap.put(CurrencyCode.MKD, new BigDecimal("56.375"));
		exchangeMap.put(CurrencyCode.USD, new BigDecimal("1"));
		exchangeMap.put(CurrencyCode.DZD, new BigDecimal("107.239998"));
		exchangeMap.put(CurrencyCode.PAB, new BigDecimal("0.99798"));
		exchangeMap.put(CurrencyCode.SGD, new BigDecimal("1.40405"));
		exchangeMap.put(CurrencyCode.ETB, new BigDecimal("21.091"));
		exchangeMap.put(CurrencyCode.ECS, new BigDecimal("25000.0"));
		exchangeMap.put(CurrencyCode.VEF, new BigDecimal("6.35"));
		exchangeMap.put(CurrencyCode.SOS, new BigDecimal("627.200012"));
		exchangeMap.put(CurrencyCode.VUV, new BigDecimal("112.709999"));
		exchangeMap.put(CurrencyCode.KGS, new BigDecimal("75.873802"));
		exchangeMap.put(CurrencyCode.LAK, new BigDecimal("8149.950195"));
		exchangeMap.put(CurrencyCode.BND, new BigDecimal("1.40455"));
		exchangeMap.put(CurrencyCode.XAF, new BigDecimal("600.418335"));
		exchangeMap.put(CurrencyCode.LRD, new BigDecimal("84.660004"));
		exchangeMap.put(CurrencyCode.ITL, new BigDecimal("1700.272217"));
		exchangeMap.put(CurrencyCode.CHF, new BigDecimal("0.991895"));
		exchangeMap.put(CurrencyCode.HRK, new BigDecimal("6.9903"));
		exchangeMap.put(CurrencyCode.DJF, new BigDecimal("177.675003"));
		exchangeMap.put(CurrencyCode.ALL, new BigDecimal("126.699501"));
		exchangeMap.put(CurrencyCode.ZMW, new BigDecimal("10.83"));
		exchangeMap.put(CurrencyCode.TZS, new BigDecimal("2156.399902"));
		exchangeMap.put(CurrencyCode.VND, new BigDecimal("22475.0"));
		exchangeMap.put(CurrencyCode.AUD, new BigDecimal("1.382801"));
		exchangeMap.put(CurrencyCode.ILS, new BigDecimal("3.86605"));
		exchangeMap.put(CurrencyCode.GHS, new BigDecimal("3.83"));
		exchangeMap.put(CurrencyCode.KPW, new BigDecimal("900.0"));
		exchangeMap.put(CurrencyCode.GYD, new BigDecimal("207.210007"));
		exchangeMap.put(CurrencyCode.BOB, new BigDecimal("6.9"));
		exchangeMap.put(CurrencyCode.MDL, new BigDecimal("19.969999"));
		exchangeMap.put(CurrencyCode.KHR, new BigDecimal("4042.5"));
		exchangeMap.put(CurrencyCode.IDR, new BigDecimal("13793.0"));
		exchangeMap.put(CurrencyCode.KYD, new BigDecimal("0.82"));
		exchangeMap.put(CurrencyCode.AMD, new BigDecimal("484.600006"));
		exchangeMap.put(CurrencyCode.BWP, new BigDecimal("10.88195"));
		exchangeMap.put(CurrencyCode.SHP, new BigDecimal("0.6663"));
		exchangeMap.put(CurrencyCode.TRY, new BigDecimal("2.90815"));
		exchangeMap.put(CurrencyCode.LBP, new BigDecimal("1506.5"));
		exchangeMap.put(CurrencyCode.CYP, new BigDecimal("0.51955"));
		exchangeMap.put(CurrencyCode.TJS, new BigDecimal("6.7932"));
		exchangeMap.put(CurrencyCode.JOD, new BigDecimal("0.7095"));
		exchangeMap.put(CurrencyCode.AED, new BigDecimal("3.67295"));
		exchangeMap.put(CurrencyCode.RWF, new BigDecimal("745.25"));
		exchangeMap.put(CurrencyCode.HKD, new BigDecimal("7.75018"));
		exchangeMap.put(CurrencyCode.EUR, new BigDecimal("0.915457"));
		exchangeMap.put(CurrencyCode.LSL, new BigDecimal("14.58025"));
		exchangeMap.put(CurrencyCode.DKK, new BigDecimal("6.8308"));
		exchangeMap.put(CurrencyCode.CAD, new BigDecimal("1.35575"));
		exchangeMap.put(CurrencyCode.BGN, new BigDecimal("1.79395"));
		exchangeMap.put(CurrencyCode.MMK, new BigDecimal("1287.939941"));
		exchangeMap.put(CurrencyCode.NOK, new BigDecimal("8.73395"));
		exchangeMap.put(CurrencyCode.MUR, new BigDecimal("36.150002"));
		exchangeMap.put(CurrencyCode.SYP, new BigDecimal("221.005997"));
		exchangeMap.put(CurrencyCode.ZWL, new BigDecimal("322.355011"));
		exchangeMap.put(CurrencyCode.GIP, new BigDecimal("0.6663"));
		exchangeMap.put(CurrencyCode.RON, new BigDecimal("4.10745"));
		exchangeMap.put(CurrencyCode.LKR, new BigDecimal("142.940002"));
		exchangeMap.put(CurrencyCode.NGN, new BigDecimal("199.25"));
		exchangeMap.put(CurrencyCode.IEP, new BigDecimal("0.699154"));
		exchangeMap.put(CurrencyCode.CRC, new BigDecimal("530.919983"));
		exchangeMap.put(CurrencyCode.CZK, new BigDecimal("24.74"));
		exchangeMap.put(CurrencyCode.PKR, new BigDecimal("103.574997"));
		exchangeMap.put(CurrencyCode.XCD, new BigDecimal("2.7"));
		exchangeMap.put(CurrencyCode.ANG, new BigDecimal("1.79"));
		exchangeMap.put(CurrencyCode.HTG, new BigDecimal("56.3871"));
		exchangeMap.put(CurrencyCode.BHD, new BigDecimal("0.37723"));
		exchangeMap.put(CurrencyCode.SIT, new BigDecimal("216.486755"));
		exchangeMap.put(CurrencyCode.SRD, new BigDecimal("4.0"));
		exchangeMap.put(CurrencyCode.KZT, new BigDecimal("307.34494"));
		exchangeMap.put(CurrencyCode.SZL, new BigDecimal("14.58025"));
		exchangeMap.put(CurrencyCode.TTD, new BigDecimal("6.38465"));
		exchangeMap.put(CurrencyCode.LTL, new BigDecimal("3.0487"));
		exchangeMap.put(CurrencyCode.SAR, new BigDecimal("3.75075"));
		exchangeMap.put(CurrencyCode.YER, new BigDecimal("214.889999"));
		exchangeMap.put(CurrencyCode.MVR, new BigDecimal("15.37"));
		exchangeMap.put(CurrencyCode.AFN, new BigDecimal("66.510002"));
		exchangeMap.put(CurrencyCode.INR, new BigDecimal("66.755447"));
		exchangeMap.put(CurrencyCode.KRW, new BigDecimal("1178.994995"));
		exchangeMap.put(CurrencyCode.NPR, new BigDecimal("106.779999"));
		exchangeMap.put(CurrencyCode.AWG, new BigDecimal("1.79"));
		exchangeMap.put(CurrencyCode.JPY, new BigDecimal("122.713997"));
		exchangeMap.put(CurrencyCode.MNT, new BigDecimal("1994.0"));
		exchangeMap.put(CurrencyCode.PLN, new BigDecimal("3.97725"));
		exchangeMap.put(CurrencyCode.AOA, new BigDecimal("135.274994"));
		exchangeMap.put(CurrencyCode.SBD, new BigDecimal("8.097697"));
		exchangeMap.put(CurrencyCode.GBP, new BigDecimal("0.664958"));
		exchangeMap.put(CurrencyCode.HUF, new BigDecimal("287.359985"));
		exchangeMap.put(CurrencyCode.BYR, new BigDecimal("18218.0"));
		exchangeMap.put(CurrencyCode.BIF, new BigDecimal("1562.900024"));
		exchangeMap.put(CurrencyCode.MWK, new BigDecimal("618.200012"));
		exchangeMap.put(CurrencyCode.MGA, new BigDecimal("3192.199951"));
		exchangeMap.put(CurrencyCode.XDR, new BigDecimal("0.72075"));
		exchangeMap.put(CurrencyCode.BZD, new BigDecimal("1.995"));
		exchangeMap.put(CurrencyCode.DEM, new BigDecimal("1.71745"));
		exchangeMap.put(CurrencyCode.BAM, new BigDecimal("1.79025"));
		exchangeMap.put(CurrencyCode.MOP, new BigDecimal("7.98265"));
		exchangeMap.put(CurrencyCode.EGP, new BigDecimal("7.8255"));
		exchangeMap.put(CurrencyCode.NAD, new BigDecimal("14.58025"));
		exchangeMap.put(CurrencyCode.NIO, new BigDecimal("27.8463"));
		exchangeMap.put(CurrencyCode.PEN, new BigDecimal("3.3658"));
		exchangeMap.put(CurrencyCode.WST, new BigDecimal("2.597497"));
		exchangeMap.put(CurrencyCode.NZD, new BigDecimal("1.505729"));
		exchangeMap.put(CurrencyCode.TMT, new BigDecimal("3.5"));
		exchangeMap.put(CurrencyCode.FRF, new BigDecimal("5.7601"));
		exchangeMap.put(CurrencyCode.CLF, new BigDecimal("0.0246"));
		exchangeMap.put(CurrencyCode.BRL, new BigDecimal("3.8167"));
		exchangeMap.put(CurrencyCode.USD, BigDecimal.ONE);
	}

	public static void init() {
		List<ExchangeRate> exchangeRates = getAllExchangeRate();
		if (ArrayUtils.isNullOrEmpty(exchangeRates)) {
			return;
		}

		for (ExchangeRate rate : exchangeRates) {
			exchangeMap.put(rate.getCurrencyCode(), rate.getExchange());
		}
	}

	public static float getPrice(float price, String currencyCodeStr) {
		CurrencyCode currencyCode = CurrencyCode.valueOf(currencyCodeStr);
		return getPrice(price, currencyCode);
	}

	public static float getPrice(float price, CurrencyCode currencyCode) {
		BigDecimal decimalRate = exchangeMap.get(currencyCode);

		if (decimalRate == null) {
			return 0.0F;
		}

		float rate = decimalRate == null ? 1f : decimalRate.floatValue();

		return price / rate;
	}

	/**
	 * 抓取美元汇率
	 * key:货币符号
	 *
	 * @return
	 */
	public static List<ExchangeRate> getAllExchangeRate() {
		List<ExchangeRate> exchangeRates = new ArrayList<ExchangeRate>();
		try {
			String url = "http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote";
			HttpResponseModel responseModel = HttpUtils.get(url, null);
			String responseStr = responseModel.getBodyString();
			List<YHRate> YHRates = readXml(responseStr);
			for (YHRate r : YHRates) {
				if (r.getName() != null && r.getName().contains("USD/")) {
					addExchange(r, exchangeRates);
				}
			}
		} catch (Exception e) {
			return exchangeRates;
		}
		return exchangeRates;
	}

	private static void addExchange(YHRate r, List<ExchangeRate> exchangeRates) {
		String currencyCode = r.getName().split("/")[1];
		ExchangeRate exchangeRate = new ExchangeRate();
		exchangeRate.setCurrencyCode(currencyCode);
		exchangeRate.setExchange(r.getPrice());
		exchangeRates.add(exchangeRate);
//		exchangeMap.put(currencyCode, exchangeRate.getExchange());
	}

	/**
	 * 读取汇率XML文本
	 *
	 * @param xml
	 * @return
	 */
	private static List<YHRate> readXml(String xml) {
		StringReader reader = new StringReader(xml);
		InputSource source = new InputSource(reader);
		SAXBuilder sb = new SAXBuilder();
		List<YHRate> resources = new ArrayList<YHRate>();
		try {
			Document doc = sb.build(source);
			Element root = doc.getRootElement();
			List<Element> childrens = root.getChildren("resources");
			for (Element c : childrens) {
				List<Element> resourceList = c.getChildren();
				for (Element re : resourceList) {
					resources.add(convert2YHRate(re));
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resources;
	}

	private static YHRate convert2YHRate(Element resource) {
		YHRate r = new YHRate();
		for (Element e : resource.getChildren()) {
			String attName = e.getAttribute("name").getValue();
			String value = e.getText();
			if ("change".equals(attName)) {
				r.setChange(BigDecimal.valueOf(Double.valueOf(value)));
			} else if ("chg_percent".equals(attName)) {
				r.setChange(BigDecimal.valueOf(Double.valueOf(value)));
			} else if ("name".equals(attName)) {
				r.setName(value);
			} else if ("ptm".equals(attName)) {
				r.setPrice(BigDecimal.valueOf(Double.valueOf(value)));
			} else if ("symbol".equals(attName)) {
				r.setSymbol(value);
			} else if ("ts".equals(attName)) {
				r.setTs(value);
			} else if ("type".equals(attName)) {
				r.setType(value);
			} else if ("utctime".equals(attName)) {
				r.setUtctime(value);
			} else if ("volume".equals(attName)) {
				r.setVolume(value);
			}
		}
		return r;
	}

}
