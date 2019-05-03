package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.model.Currency;
import org.sql2o.Connection;

public interface CurrencyDAO {

    void createCurrency(Currency currency);

    void createCurrencyTransactional(Currency currency, Connection tx);
}
