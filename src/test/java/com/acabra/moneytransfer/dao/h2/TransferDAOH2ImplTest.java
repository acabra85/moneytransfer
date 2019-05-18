package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Currency;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class TransferDAOH2ImplTest {

    public Sql2o sql2o;

    private TransferDAOH2Impl underTest;

    private LocalDateTime NOW = LocalDateTime.now(Clock.systemUTC());

    @Before
    public void before() {
        sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        underTest = new TransferDAOH2Impl(sql2o);
    }

    @Test
    public void should_return_empty_list() {
        //given
        long invalidAccountId = 29L;

        //when
        List<Transfer> transfers = underTest.retrieveTransfersByAccountId(invalidAccountId);

        //then
        Assert.assertTrue(transfers.isEmpty());
    }

    @Test
    public void should_return_persisted_transfer() {
        //given
        BigDecimal transferAmount = BigDecimal.TEN;
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        Account sourceAccount = accountDAO.createAccount(BigDecimal.TEN, Currency.EUR);
        Account destinationAccount = accountDAO.createAccount(BigDecimal.ZERO, Currency.EUR);
        TransferRequest transferRequest = new TransferRequest(sourceAccount.getId(), destinationAccount.getId(), transferAmount);

        Connection tx = sql2o.beginTransaction();

        //when
        Transfer executedTransfer = underTest.storeTransferAndCommitTransactional(transferRequest, tx);

        //then
        Assert.assertEquals(sourceAccount.getId(), executedTransfer.sourceAccountId);
        Assert.assertEquals(destinationAccount.getId(), executedTransfer.destinationAccountId);
        Assert.assertEquals(transferAmount, executedTransfer.transferAmount);
        Assert.assertTrue(NOW.compareTo(executedTransfer.timestamp) < 0);
    }

    @Test
    public void should_retrieve_transfers_by_id() {
        //given
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        Account accountA = accountDAO.createAccount(new BigDecimal("50"), Currency.EUR);
        Account accountB = accountDAO.createAccount(BigDecimal.ZERO, Currency.EUR);
        Account accountC = accountDAO.createAccount(BigDecimal.ZERO, Currency.EUR);
        TransferRequest transferFromAtoB = new TransferRequest(accountA.getId(), accountB.getId(), BigDecimal.TEN);
        TransferRequest transferFromBtoC = new TransferRequest(accountB.getId(), accountC.getId(), new BigDecimal("7.5"));
        TransferRequest transferFromCtoA = new TransferRequest(accountC.getId(), accountA.getId(), new BigDecimal("5"));

        underTest.storeTransferAndCommitTransactional(transferFromAtoB, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromAtoB, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromAtoB, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromAtoB, sql2o.beginTransaction());

        underTest.storeTransferAndCommitTransactional(transferFromBtoC, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromBtoC, sql2o.beginTransaction());

        underTest.storeTransferAndCommitTransactional(transferFromCtoA, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromCtoA, sql2o.beginTransaction());

        //when
        List<Transfer> transfersAccountA = underTest.retrieveTransfersByAccountId(accountA.getId());
        List<Transfer> transfersAccountB = underTest.retrieveTransfersByAccountId(accountB.getId());
        List<Transfer> transfersAccountC = underTest.retrieveTransfersByAccountId(accountC.getId());

        //then
        Assert.assertEquals(6, transfersAccountA.stream()
                .filter(tfx -> tfx.involvesAccount(accountA.getId()))
                .count());
        Assert.assertEquals(6, transfersAccountB.stream()
                .filter(tfx -> tfx.involvesAccount(accountB.getId()))
                .count());
        Assert.assertEquals(4, transfersAccountC.stream()
                .filter(tfx -> tfx.involvesAccount(accountC.getId()))
                .count());
    }

    @Test
    public void should_retrieve_all_transfers() {
        //given
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        Account accountA = accountDAO.createAccount(new BigDecimal("50"), Currency.EUR);
        Account accountB = accountDAO.createAccount(BigDecimal.ZERO, Currency.EUR);
        Account accountC = accountDAO.createAccount(BigDecimal.ZERO, Currency.EUR);
        TransferRequest transferFromAtoB = new TransferRequest(accountA.getId(), accountB.getId(), BigDecimal.TEN);
        TransferRequest transferFromBtoC = new TransferRequest(accountB.getId(), accountC.getId(), new BigDecimal("7.5"));
        TransferRequest transferFromCtoA = new TransferRequest(accountC.getId(), accountA.getId(), new BigDecimal("5"));


        underTest.storeTransferAndCommitTransactional(transferFromAtoB, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromAtoB, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromAtoB, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromAtoB, sql2o.beginTransaction());

        underTest.storeTransferAndCommitTransactional(transferFromBtoC, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromBtoC, sql2o.beginTransaction());

        underTest.storeTransferAndCommitTransactional(transferFromCtoA, sql2o.beginTransaction());
        underTest.storeTransferAndCommitTransactional(transferFromCtoA, sql2o.beginTransaction());

        //when
        List<Transfer> allTransfers = underTest.retrieveAllTransfers();

        //then
        Assert.assertEquals(8, allTransfers.size());
    }
}
