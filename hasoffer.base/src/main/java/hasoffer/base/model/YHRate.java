package hasoffer.base.model;

import java.math.BigDecimal;

/**
 * Author:ya qi
 * Date:2015/3/18 2015/3/18
 * <p>
 * 雅虎汇率的model
 */
public class YHRate {
	private BigDecimal change;
	private BigDecimal chg_percent;
	private String name;
	private BigDecimal price;
	private String symbol;
	private String ts;
	private String type;
	private String utctime;
	private String volume;


	public BigDecimal getChange() {
		return change;
	}

	public void setChange(BigDecimal change) {
		this.change = change;
	}

	public BigDecimal getChg_percent() {
		return chg_percent;
	}

	public void setChg_percent(BigDecimal chg_percent) {
		this.chg_percent = chg_percent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUtctime() {
		return utctime;
	}

	public void setUtctime(String utctime) {
		this.utctime = utctime;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	@Override
	public String toString() {
		return "Resouce{" +
		       "change=" + change +
		       ", chg_percent=" + chg_percent +
		       ", name='" + name + '\'' +
		       ", ptm=" + price +
		       ", symbol='" + symbol + '\'' +
		       ", ts='" + ts + '\'' +
		       ", type='" + type + '\'' +
		       ", utctime='" + utctime + '\'' +
		       ", volume='" + volume + '\'' +
		       '}';
	}
}
