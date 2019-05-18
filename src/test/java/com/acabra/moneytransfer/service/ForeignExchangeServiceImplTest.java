package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.model.Currency;
import com.acabra.moneytransfer.utils.TestUtils;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;

public class ForeignExchangeServiceImplTest {

    ForeignExchangeServiceImpl underTest;

    @Before
    public void before() {
        underTest = ForeignExchangeServiceImpl.getInstance();
    }

    @Test(expected = NoSuchElementException.class)
    public void should_fail_no_exchange_rate_found() {
        //when
        underTest.retrieveExchangeRate(Currency.COP, Currency.PLN);
    }

    @Test
    public void should_retrieve_last_updated_value() {
        //given
        BigDecimal initial = BigDecimal.ONE;
        BigDecimal second  = BigDecimal.TEN;
        BigDecimal expected = new BigDecimal("3.8");
        Currency source = Currency.EUR;
        Currency destination = Currency.GBP;

        //when
        underTest.updateExchangeRate(source, destination, initial);
        underTest.updateExchangeRate(source, destination, second);
        underTest.updateExchangeRate(source, destination, expected);

        //then
        TestUtils.assertBigDecimalEquals(expected, underTest.retrieveExchangeRate(source, destination));
    }

    @Test
    public void should_return_one_for_same_currencies_exchange_rate() {
        //given
        BigDecimal expected = BigDecimal.ONE;
        Currency source = Currency.EUR;
        Currency destination = Currency.EUR;

        //when
        BigDecimal exchangeRate = underTest.retrieveExchangeRate(source, destination);

        //then
        TestUtils.assertBigDecimalEquals(expected, exchangeRate);
    }

    @Test
    public void should_return_same_amount_for_convert_between_same_currency() {
        //given
        BigDecimal expected = new BigDecimal("120.67");
        Currency source = Currency.EUR;
        Currency destination = Currency.EUR;

        //when
        BigDecimal convertedAmount = underTest.convertAmount(source, destination, expected);

        //then
        TestUtils.assertBigDecimalEquals(expected, convertedAmount);
    }

    @Test
    public void should_convert_amount_for_different_currencies() {
        //given
        BigDecimal amount = new BigDecimal("100");
        BigDecimal expectedAmount = new BigDecimal("165");
        Currency source = Currency.GBP;
        Currency destination = Currency.EUR;
        underTest.updateExchangeRate(source, destination, new BigDecimal("1.65"));

        //when
        BigDecimal convertedAmount = underTest.convertAmount(source, destination, amount);

        //then
        TestUtils.assertBigDecimalEquals(expectedAmount, convertedAmount);
    }

}
