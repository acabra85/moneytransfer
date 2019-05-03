package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Currency;
import com.acabra.moneytransfer.utils.TestUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class AccountDAOH2ImplTest {

    Sql2o sql2o;

    private AccountDAO underTest;

    @Before
    public void setup() {
        sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        underTest = new AccountDAOH2Impl(sql2o);
    }

    @Test
    public void should_create_account() {
        //given
        BigDecimal initialAmount = BigDecimal.TEN;

        //when
        Account account = underTest.createAccount(initialAmount, Currency.EUR);

        //then
        Assert.assertEquals(initialAmount, account.getBalance());
    }

    @Test
    public void should_create_account_negative_balance() {
        //given
        BigDecimal initialAmount = BigDecimal.valueOf(-1L);

        //when
        Account account = underTest.createAccount(initialAmount, Currency.EUR);

        //then
        Assert.assertEquals(initialAmount, account.getBalance());
    }

    @Test
    public void should_retrieve_account_by_id() {
        //given
        BigDecimal initialAmount = BigDecimal.TEN;
        long accountId = underTest.createAccount(initialAmount, Currency.EUR).getId();

        //when
        Account queryAccount = underTest.retrieveAccountById(accountId);

        //then
        TestUtils.assertBigDecimalEquals(initialAmount, queryAccount.getBalance());
        Assert.assertEquals(accountId, queryAccount.getId());
    }

    @Test
    public void should_retrieve_created_accounts_with_initial_balance() {
        //given
        BigDecimal initialAmount = BigDecimal.ONE;
        int expectedAccountSize = 3;

        for (int i = 0; i < expectedAccountSize; i++) {
            underTest.createAccount(initialAmount, Currency.EUR);
        }

        //when
        List<Account> accounts = underTest.retrieveAllAccounts();

        //then
        Assert.assertEquals(expectedAccountSize, accounts.size());
        Set<Long> seenAccountIds = new HashSet<>();
        for (Account account : accounts) {
            TestUtils.assertBigDecimalEquals(initialAmount, account.getBalance());
            Assert.assertFalse(seenAccountIds.contains(account.getId()));
            seenAccountIds.add(account.getId());
        }
    }

    @Test
    public void should_retrieve_created_accounts_by_id() {
        BigDecimal initialAmount = BigDecimal.ONE;
        int expectedAccountSize = 3;
        List<Long> ids = new ArrayList<>(expectedAccountSize);
        for (int i = 0; i < expectedAccountSize; i++) {
            ids.add(underTest.createAccount(initialAmount, Currency.EUR).getId());
        }
        List<Account> accounts = underTest.retrieveAccountsByIds(ids);
        Assert.assertEquals(expectedAccountSize, accounts.size());
        for (Account account : accounts) {
            TestUtils.assertBigDecimalEquals(initialAmount, account.getBalance());
        }
    }

    @Test
    public void should_update_account_balance() {
        //given
        BigDecimal initialAmount = BigDecimal.TEN;
        Account account = underTest.createAccount(initialAmount, Currency.EUR);
        account.withdraw(BigDecimal.ONE);
        BigDecimal expectedAccountBalance = initialAmount.subtract(BigDecimal.ONE);

        //when
        underTest.updateAccountBalance(account);

        //then
        TestUtils.assertBigDecimalEquals(expectedAccountBalance, underTest.retrieveAccountById(account.getId()).getBalance());
    }

    @Test
    public void should_update_account_balance_transactional() {
        //given
        BigDecimal initialAmount = BigDecimal.TEN;
        Account account = underTest.createAccount(initialAmount, Currency.EUR);
        account.withdraw(BigDecimal.ONE);
        BigDecimal expectedBalanceAfterTransfer = initialAmount.subtract(BigDecimal.ONE);
        Connection tx = sql2o.beginTransaction();

        //when
        underTest.updateAccountBalanceTransactional(account, tx);
        tx.commit();

        //then
        TestUtils.assertBigDecimalEquals(expectedBalanceAfterTransfer, underTest.retrieveAccountById(account.getId()).getBalance());
    }

    @Test
    public void should_retrieve_account_lock_for_transfer() {
        //given
        Account sourceAccount = underTest.createAccount(BigDecimal.TEN, Currency.EUR);
        Account destinationAccount = underTest.createAccount(BigDecimal.TEN, Currency.EUR);

        //when
        AccountsTransferLock accountsTransferLock = underTest.lockAccountsForTransfer(sourceAccount.getId(), destinationAccount.getId());

        //then
        Assert.assertEquals(sourceAccount, accountsTransferLock.getSourceAccount());
        Assert.assertEquals(destinationAccount, accountsTransferLock.getDestinationAccount());
    }

    @Test(expected = NoSuchElementException.class)
    public void should_fail_account_non_existent_account() {
        //given
        Account sourceAccount = underTest.createAccount(BigDecimal.TEN, Currency.EUR);
        long nonExistentAccountId = 10L;

        //when
        underTest.lockAccountsForTransfer(sourceAccount.getId(), nonExistentAccountId);

        //then
    }

    @Test
    public void should_transfer_from_account_list() {
        //given
        long sourceAccountId = underTest.createAccount(BigDecimal.TEN, Currency.EUR).getId();
        long destinationAccountId = underTest.createAccount(BigDecimal.TEN, Currency.EUR).getId();

        List<Account> accounts = underTest.retrieveAllAccounts();

        Account sourceAccount = accounts.get(0).getId() == sourceAccountId ? accounts.get(0) : accounts.get(1);
        Account destinationAccount = accounts.get(0).getId() == destinationAccountId ? accounts.get(0) : accounts.get(1);

        //when
        sourceAccount.withdraw(BigDecimal.ONE);
        destinationAccount.deposit(BigDecimal.ONE);

        //then
        TestUtils.assertBigDecimalEquals("9", sourceAccount.getBalance());
        TestUtils.assertBigDecimalEquals("11", destinationAccount.getBalance());

    }
}
