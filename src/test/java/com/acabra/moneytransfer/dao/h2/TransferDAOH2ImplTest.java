package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

@RunWith(MockitoJUnitRunner.class)
public class TransferDAOH2ImplTest {

    public Sql2o sql2o;

    private TransferDAOH2Impl underTest;

    private LocalDateTime NOW = LocalDateTime.now();

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
    public void should_return_persisted_transaction() {
        //given
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        Account sourceAccount = accountDAO.createAccount(BigDecimal.TEN);
        Account destinationAccount = accountDAO.createAccount(BigDecimal.ZERO);
        TransferRequest transferRequestMock = Mockito.mock(TransferRequest.class);

        BigDecimal transferAmount = BigDecimal.TEN;
        Connection tx = sql2o.beginTransaction();

        Mockito.when(transferRequestMock.getTransferAmount()).thenReturn(transferAmount);
        Mockito.when(transferRequestMock.getSourceAccountId()).thenReturn(sourceAccount.getId());
        Mockito.when(transferRequestMock.getDestinationAccountId()).thenReturn(destinationAccount.getId());
        Mockito.when(transferRequestMock.getTimestamp()).thenReturn(NOW);

        //when
        Transfer executedTransfer = underTest.storeTransferAndCommitTransactional(transferRequestMock, tx);

        //then
        Assert.assertEquals(sourceAccount.getId(), executedTransfer.sourceAccountId);
        Assert.assertEquals(destinationAccount.getId(), executedTransfer.destinationAccountId);
        Assert.assertEquals(transferAmount, executedTransfer.transferAmount);
        Assert.assertEquals(NOW, executedTransfer.timestamp);
    }

    @Test
    public void should_retrieve_transfers() {
        //given
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        BigDecimal sourceAccountInitialBalance = new BigDecimal("50");
        Account sourceAccount = accountDAO.createAccount(sourceAccountInitialBalance);
        Account destinationAccount = accountDAO.createAccount(BigDecimal.ZERO);
        TransferRequest transferRequestMock = Mockito.mock(TransferRequest.class);

        BigDecimal transferAmount = BigDecimal.TEN;

        Mockito.when(transferRequestMock.getTransferAmount()).thenReturn(transferAmount);
        Mockito.when(transferRequestMock.getSourceAccountId()).thenReturn(sourceAccount.getId());
        Mockito.when(transferRequestMock.getDestinationAccountId()).thenReturn(destinationAccount.getId());
        Mockito.when(transferRequestMock.getTimestamp()).thenReturn(NOW);
        int transferCount = 5;

        //deplete
        for (int i = 0; i < transferCount; i++) {
            underTest.storeTransferAndCommitTransactional(transferRequestMock, sql2o.beginTransaction());
        }

        //when
        List<Transfer> executedTransfers = underTest.retrieveTransfersByAccountId(sourceAccount.getId());

        //then
        Assert.assertEquals(transferCount, executedTransfers.size());

        for (Transfer executedTransfer : executedTransfers) {
            Assert.assertEquals(0, transferAmount.compareTo(executedTransfer.transferAmount));
            Assert.assertEquals(sourceAccount.getId(), executedTransfer.sourceAccountId);
            Assert.assertEquals(destinationAccount.getId(), executedTransfer.destinationAccountId);
            Assert.assertEquals(NOW, executedTransfer.timestamp);
        }

    }
}
