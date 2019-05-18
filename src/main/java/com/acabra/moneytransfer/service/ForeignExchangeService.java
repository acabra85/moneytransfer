package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.model.Currency;
import java.math.BigDecimal;

/**
 * This interface represents the exchange service in charge
 * of providing exchange rates between supported currencys.
 */
public interface ForeignExchangeService {

    BigDecimal retrieveExchangeRate(Currency source, Currency destination);

    void updateExchangeRate(Currency source, Currency destination, BigDecimal exchangeRate);

    BigDecimal convertAmount(Currency source, Currency destination, BigDecimal amount);
}
