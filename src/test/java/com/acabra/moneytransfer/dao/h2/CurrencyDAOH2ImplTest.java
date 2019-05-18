package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.model.Currency;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

public class CurrencyDAOH2ImplTest {

    private Sql2o sql2o;
    CurrencyH2DAOImpl underTest;

    @Before
    public void setup() {
        sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        underTest = new CurrencyH2DAOImpl(sql2o);
    }

    @Test(expected = Sql2oException.class)
    public void should_fail_create_existent_currency() {
        //given
        Currency euro = Currency.EUR;

        //when
        underTest.createCurrency(euro);
    }

    @Test
    public void should_return_all_valid_currencies() {
        //given
        List<Currency> availableCurrencies = Arrays.asList(Currency.values());

        //when
        List<String> storedCurrencies;
        try(Connection cx = sql2o.open()) {
            storedCurrencies = cx.createQuery(CurrencyH2DAOImpl.SELECT_ALL_CURRENCIES)
                    .executeAndFetch(String.class);
        }

        //then
        availableCurrencies.forEach(currency -> Assert.assertTrue(storedCurrencies.contains(currency.code)));
        Assert.assertEquals(Currency.values().length, storedCurrencies.size());
    }
}
