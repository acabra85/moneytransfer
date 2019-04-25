package com.acabra.moneystransfer.model;

import com.acabra.moneytransfer.exception.InvalidOperationException;
import com.acabra.moneytransfer.model.Account;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class AccountTest {

    @Test(expected = InvalidOperationException.class)
    public void should_fail_withdrawal_amount_greater_than_balance() {
        Account account = new Account(1);
        account.withdraw(BigDecimal.TEN);
    }

    @Test(expected = InvalidOperationException.class)
    public void should_fail_deposit_negative_amount() {
        Account account = new Account(1);
        account.deposit(BigDecimal.valueOf(-1L));
    }

    @Test(expected = InvalidOperationException.class)
    public void should_fail_deposit_zero() {
        Account account = new Account(1);
        account.deposit(BigDecimal.ZERO);
    }

    @Test
    public void should_update_balance_after_deposit() {
        BigDecimal expectedBalance = BigDecimal.TEN;
        Account account = new Account(1);
        account.deposit(BigDecimal.valueOf(5));
        account.deposit(BigDecimal.valueOf(5));
        Assert.assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    public void should_update_balance_after_withdraw() {
        Account account = new Account(1, BigDecimal.TEN);
        account.withdraw(BigDecimal.valueOf(9));
        BigDecimal expectedBalance =  BigDecimal.ONE;
        Assert.assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    public void should_have_zero_balance() {
        Account account = new Account(1, BigDecimal.valueOf(-1));
        Assert.assertEquals(BigDecimal.ZERO, account.getBalance());
    }
}
