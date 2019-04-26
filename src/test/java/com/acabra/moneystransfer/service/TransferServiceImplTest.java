package com.acabra.moneystransfer.service;

import com.acabra.moneytransfer.dao.h2.AccountDAOH2Impl;
import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.dao.h2.TransferDAOH2Impl;
import com.acabra.moneytransfer.exception.InvalidOperationException;
import com.acabra.moneytransfer.model.TransferRequest;
import com.acabra.moneytransfer.service.TransferServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferServiceImplTest {

    @Test
    public void should_persist_transfer_and_account_balances() {
        Sql2o sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        AccountDAOH2Impl accountDao = new AccountDAOH2Impl(sql2o);
        TransferDAOH2Impl transferDAO = new TransferDAOH2Impl(sql2o);
        TransferServiceImpl transferService = new TransferServiceImpl(transferDAO,accountDao);

        long sourceAccountId = accountDao.createAccount(BigDecimal.TEN).getId();
        long destinationAccountId = accountDao.createAccount(BigDecimal.ZERO).getId();
        TransferRequest transferRequest = new TransferRequest(sourceAccountId, destinationAccountId, BigDecimal.TEN, LocalDateTime.now());
        transferService.transfer(transferRequest);

        Assert.assertEquals(0, accountDao.retrieveAccountById(sourceAccountId).getBalance().compareTo(BigDecimal.ZERO));
        Assert.assertEquals(0, accountDao.retrieveAccountById(destinationAccountId).getBalance().compareTo(BigDecimal.TEN));

        Assert.assertEquals(1, transferDAO.retrieveTransfersByAccountId(sourceAccountId).size());
        Assert.assertEquals(1, transferDAO.retrieveTransfersByAccountId(destinationAccountId).size());
    }

    @Test(expected = InvalidOperationException.class)
    public void should_fail_transfer_insufficient_balance_source_account() {
        Sql2o sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        AccountDAOH2Impl accountDao = new AccountDAOH2Impl(sql2o);
        TransferServiceImpl transferService = new TransferServiceImpl(new TransferDAOH2Impl(sql2o),accountDao);

        long sourceAccountId = accountDao.createAccount(BigDecimal.TEN).getId();
        long destinationAccountId = accountDao.createAccount(BigDecimal.ZERO).getId();
        BigDecimal transferAmount = BigDecimal.valueOf(20L);
        TransferRequest transferRequest = new TransferRequest(sourceAccountId, destinationAccountId, transferAmount, LocalDateTime.now());
        transferService.transfer(transferRequest);
    }

    @Test(expected = InvalidOperationException.class)
    public void should_fail_transfer_amount_zero() {
        Sql2o sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        AccountDAOH2Impl accountDao = new AccountDAOH2Impl(sql2o);
        TransferServiceImpl transferService = new TransferServiceImpl(new TransferDAOH2Impl(sql2o),accountDao);

        long sourceAccountId = accountDao.createAccount(BigDecimal.TEN).getId();
        long destinationAccountId = accountDao.createAccount(BigDecimal.ZERO).getId();
        BigDecimal transferAmount = BigDecimal.ZERO;
        TransferRequest transferRequest = new TransferRequest(sourceAccountId, destinationAccountId, transferAmount, LocalDateTime.now());
        transferService.transfer(transferRequest);
    }
}
