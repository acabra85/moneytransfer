package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.model.Currency;
import org.sql2o.Connection;

public interface CurrencyDAO {

    boolean createCurrency(Currency currency);

}
