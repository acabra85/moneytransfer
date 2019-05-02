package com.acabra.moneytransfer.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import org.junit.Assert;

public class TestUtils {

    public static void assertBigDecimalEquals(String expectedStr, BigDecimal actual) {
        BigDecimal expected = new BigDecimal(expectedStr);
        DecimalFormat dc = new DecimalFormat("0.00");
        dc.setMaximumFractionDigits(2);
        Assert.assertEquals(dc.format(expected), dc.format(actual));
        Assert.assertEquals(0, expected.compareTo(actual));
    }

    public static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertBigDecimalEquals(expected.toString(), actual);
    }
}
