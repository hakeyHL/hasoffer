package hasoffer.base.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 */
public class ExchangeRate implements Serializable {
	private CurrencyCode currencyCode;
	private BigDecimal exchange;

	public ExchangeRate() {
	}

	public ExchangeRate(CurrencyCode currencyCode, BigDecimal exchange) {
		this.currencyCode = currencyCode;
		this.exchange = exchange;
	}

	public CurrencyCode getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = CurrencyCode.valueOf(currencyCode.toUpperCase());
	}

	public void setCurrencyCode(CurrencyCode currencyCode) {
		this.currencyCode = currencyCode;
	}

	public BigDecimal getExchange() {
		return exchange;
	}

	public void setExchange(BigDecimal exchange) {
		this.exchange = exchange;
	}
}
