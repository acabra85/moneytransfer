package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.CurrencyDAO;
import com.acabra.moneytransfer.model.Currency;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class CurrencyH2DAOImpl implements CurrencyDAO {

    //DDL
    static final String CREATE_TABLE_CURRENCY =
            "CREATE TABLE currency (" +
                    "currency_code VARCHAR(3) PRIMARY KEY, " +
                    "currency_description VARCHAR(100)" +
                    ")";

    //DML
    static final String INSERT_NEW_CURRENCY =
            "INSERT INTO currency(currency_code, currency_description) " +
                    "VALUES (:currency_code, :currency_description)";

    public static final String SELECT_ALL_CURRENCIES =
            "SELECT currency_code " +
              "FROM currency " +
          "ORDER BY currency_code ASC";

    private final Sql2o sql2o;

    public CurrencyH2DAOImpl(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public boolean createCurrency(Currency currency) {
        try (Connection cx = sql2o.open()) {
            cx.createQuery(INSERT_NEW_CURRENCY)
                    .addParameter("currency_code", currency.code)
                    .addParameter("currency_description", currency.description)
                    .executeUpdate();
            return true;
        }
    }
}
