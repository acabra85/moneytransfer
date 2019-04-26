package com.acabra.moneystransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.h2.AccountDAOH2Impl;
import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.model.Account;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOH2ImplTest {

    private AccountDAO dao;

    @Before
    public void setup() {
        dao = new AccountDAOH2Impl(H2Sql2oHelper.ofLocalKeepOpenSql2o());
    }

    @Test
    public void should_create_account() {
        BigDecimal initialAmount = BigDecimal.TEN;
        Account account = dao.createAccount(initialAmount);
        Assert.assertEquals(initialAmount, account.getBalance());
    }

    @Test
    public void should_create_account_balance_zero() {
        BigDecimal initialAmount = BigDecimal.valueOf(-1L);
        Account account = dao.createAccount(initialAmount);
        Assert.assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    @Test
    public void should_retrieve_account_by_id() {
        BigDecimal initialAmount = BigDecimal.TEN;
        long accountId = dao.createAccount(initialAmount).getId();
        Account queryAccount = dao.retrieveAccountById(accountId);

        Assert.assertEquals(0, initialAmount.compareTo(queryAccount.getBalance()));
        Assert.assertEquals(accountId, queryAccount.getId());
    }

    @Test
    public void should_retrieve_created_accounts_with_initial_balance() {
        BigDecimal initialAmount = BigDecimal.ONE;
        int expectedAccountSize = 3;
        for (int i = 0; i < expectedAccountSize; i++) {
            dao.createAccount(initialAmount);
        }
        List<Account> accounts = dao.getAccounts();
        Assert.assertEquals(expectedAccountSize, accounts.size());
        for (Account account : accounts) {
            Assert.assertEquals(0, initialAmount.compareTo(account.getBalance()));
        }
    }

    @Test
    public void should_retrieve_created_accounts_by_id() {
        BigDecimal initialAmount = BigDecimal.ONE;
        int expectedAccountSize = 3;
        List<Long> ids = new ArrayList<>(expectedAccountSize);
        for (int i = 0; i < expectedAccountSize; i++) {
            ids.add(dao.createAccount(initialAmount).getId());
        }
        List<Account> accounts = dao.retrieveAccountsByIds(ids);
        Assert.assertEquals(expectedAccountSize, accounts.size());
        for (Account account : accounts) {
            Assert.assertEquals(0, initialAmount.compareTo(account.getBalance()));
        }
    }

    @Test
    public void should_update_account_balance() {
        BigDecimal initialAmount = BigDecimal.TEN;
        Account account = dao.createAccount(initialAmount);
        account.withdraw(BigDecimal.ONE);
        BigDecimal expectedAccountSize = initialAmount.subtract(BigDecimal.ONE);
        dao.updateAccountBalance(account);
        Assert.assertEquals(0, dao.retrieveAccountById(account.getId()).getBalance().compareTo(expectedAccountSize));
    }
}
