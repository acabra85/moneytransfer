package com.acabra.moneytransfer.model;

import java.util.NoSuchElementException;
import org.junit.Test;

public class CurrencyTest {

    @Test (expected = NoSuchElementException.class)
    public void should_fail_currency_code_not_found() {
        Currency.getCurrencyFromCode("XXXX");
    }
}
