package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.h2.AccountDAOH2Impl;
import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.dao.h2.TransferDAOH2Impl;
import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Sql2o;

public class TransferServiceImplConcurrencyTest {


    private TransferServiceImpl underTest;
    private AccountDAOH2Impl accountDAO;

    @Before
    public void before() {
        Sql2o sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        accountDAO = new AccountDAOH2Impl(sql2o);
        underTest = new TransferServiceImpl(new TransferDAOH2Impl(sql2o), accountDAO);
    }

    @Test
    public void should_preserve_data_integrity_in_face_of_concurrency() throws InterruptedException {

        long account1Id = accountDAO.createAccount(new BigDecimal("10000")).getId();
        long account2Id = accountDAO.createAccount(new BigDecimal("50000")).getId();
        long account3Id = accountDAO.createAccount(new BigDecimal("70000")).getId();

        Collection<Callable<Transfer>> concurrentJobs = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            concurrentJobs.add(()-> underTest.transfer(new TransferRequest(account1Id, account2Id, new BigDecimal("0.01"))));
            concurrentJobs.add(()-> underTest.transfer(new TransferRequest(account3Id, account1Id, new BigDecimal("0.03"))));
            concurrentJobs.add(()-> underTest.transfer(new TransferRequest(account2Id, account3Id, new BigDecimal("0.02"))));
        }

        Executors.newFixedThreadPool(4).invokeAll(concurrentJobs);

        Assert.assertEquals(0, accountDAO.retrieveAccountById(account1Id).getBalance().compareTo(new BigDecimal("10004.02")));
        Assert.assertEquals(0, accountDAO.retrieveAccountById(account2Id).getBalance().compareTo(new BigDecimal("49997.99")));
        Assert.assertEquals(0, accountDAO.retrieveAccountById(account3Id).getBalance().compareTo(new BigDecimal("69997.99")));
    }
}
