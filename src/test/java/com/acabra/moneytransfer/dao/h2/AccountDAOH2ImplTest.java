package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.model.Account;
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
        Account account = underTest.createAccount(initialAmount);

        //then
        Assert.assertEquals(initialAmount, account.getBalance());
    }

    @Test
    public void should_create_account_negative_balance() {
        //given
        BigDecimal initialAmount = BigDecimal.valueOf(-1L);

        //when
        Account account = underTest.createAccount(initialAmount);

        //then
        Assert.assertEquals(initialAmount, account.getBalance());
    }

    @Test
    public void should_retrieve_account_by_id() {
        //given
        BigDecimal initialAmount = BigDecimal.TEN;
        long accountId = underTest.createAccount(initialAmount).getId();

        //when
        Account queryAccount = underTest.retrieveAccountById(accountId);

        //then
        Assert.assertEquals(0, initialAmount.compareTo(queryAccount.getBalance()));
        Assert.assertEquals(accountId, queryAccount.getId());
    }

    @Test
    public void should_retrieve_created_accounts_with_initial_balance() {
        //given
        BigDecimal initialAmount = BigDecimal.ONE;
        int expectedAccountSize = 3;

        for (int i = 0; i < expectedAccountSize; i++) {
            underTest.createAccount(initialAmount);
        }

        //when
        List<Account> accounts = underTest.retrieveAllAccounts();

        //then
        Assert.assertEquals(expectedAccountSize, accounts.size());
        Set<Long> seenAccountIds = new HashSet<>();
        for (Account account : accounts) {
            Assert.assertEquals(0, initialAmount.compareTo(account.getBalance()));
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
            ids.add(underTest.createAccount(initialAmount).getId());
        }
        List<Account> accounts = underTest.retrieveAccountsByIds(ids);
        Assert.assertEquals(expectedAccountSize, accounts.size());
        for (Account account : accounts) {
            Assert.assertEquals(0, initialAmount.compareTo(account.getBalance()));
        }
    }

    @Test
    public void should_update_account_balance() {
        BigDecimal initialAmount = BigDecimal.TEN;
        Account account = underTest.createAccount(initialAmount);
        account.withdraw(BigDecimal.ONE);
        BigDecimal expectedAccountSize = initialAmount.subtract(BigDecimal.ONE);
        underTest.updateAccountBalance(account);
        Assert.assertEquals(0, underTest.retrieveAccountById(account.getId()).getBalance().compareTo(expectedAccountSize));
    }

    @Test
    public void should_update_account_balance_transactional() {
        //given
        BigDecimal initialAmount = BigDecimal.TEN;
        Account account = underTest.createAccount(initialAmount);
        account.withdraw(BigDecimal.ONE);
        BigDecimal expectedAccountSize = initialAmount.subtract(BigDecimal.ONE);
        Connection tx = sql2o.beginTransaction();

        //when
        underTest.updateAccountBalanceTransactional(account, tx);
        tx.commit();

        //then
        Assert.assertEquals(0, underTest.retrieveAccountById(account.getId()).getBalance().compareTo(expectedAccountSize));
    }

    @Test
    public void should_retrieve_account_lock_for_transfer() {
        //given
        Account sourceAccount = underTest.createAccount(BigDecimal.TEN);
        Account destinationAccount = underTest.createAccount(BigDecimal.TEN);

        //when
        AccountsTransferLock accountsTransferLock = underTest.lockAccountsForTransfer(sourceAccount.getId(), destinationAccount.getId());

        //then
        Assert.assertEquals(sourceAccount, accountsTransferLock.getSourceAccount());
        Assert.assertEquals(destinationAccount, accountsTransferLock.getDestinationAccount());
    }

    @Test(expected = NoSuchElementException.class)
    public void should_fail_account_non_existent_account() {
        //given
        Account sourceAccount = underTest.createAccount(BigDecimal.TEN);
        long nonExistentAccountId = 10L;

        //when
        underTest.lockAccountsForTransfer(sourceAccount.getId(), nonExistentAccountId);

        //then
    }

    @Test
    public void should_transfer_from_account_list() {
        //given
        long sourceAccountId = underTest.createAccount(BigDecimal.TEN).getId();
        long destinationAccountId = underTest.createAccount(BigDecimal.TEN).getId();

        List<Account> accounts = underTest.retrieveAllAccounts();

        Account sourceAccount = accounts.get(0).getId() == sourceAccountId ? accounts.get(0) : accounts.get(1);
        Account destinationAccount = accounts.get(0).getId() == destinationAccountId ? accounts.get(0) : accounts.get(1);

        //when
        sourceAccount.withdraw(BigDecimal.ONE);
        destinationAccount.deposit(BigDecimal.ONE);

        //then
        Assert.assertEquals(0, sourceAccount.getBalance().compareTo(new BigDecimal("9")));
        Assert.assertEquals(0, destinationAccount.getBalance().compareTo(new BigDecimal("11")));
    }
}
