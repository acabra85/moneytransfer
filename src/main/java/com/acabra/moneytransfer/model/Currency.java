package com.acabra.moneytransfer.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum Currency {
    GBP("GBP", "Great British Pounds"),
    EUR("EUR", "Euros"),
    USD("USD", "United States Dollars"),
    PLN("PLN", "New Polish Zloty"),
    COP("COP", "Colombian Peso");

    private static final Map<String, Currency> CODE_TO_CURRENCY_MAP = Collections.unmodifiableMap(new HashMap<String, Currency>(){{
        for (Currency currency: Currency.values()) {
            this.put(currency.code, currency);
        }
    }});

    public String code;
    public String description;

    Currency(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Currency getCurrencyFromCode(String code) {
        Currency currency = CODE_TO_CURRENCY_MAP.get(code.toUpperCase());
        if (currency == null) {
            throw new NoSuchElementException("The given currency code is not available in the system: "+ code);
        }
        return currency;
    }
}
