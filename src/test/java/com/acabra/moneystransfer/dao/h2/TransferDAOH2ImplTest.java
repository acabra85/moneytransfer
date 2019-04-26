package com.acabra.moneystransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.h2.AccountDAOH2Impl;
import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.dao.h2.TransferDAOH2Impl;
import com.acabra.moneytransfer.dto.TransferDTO;
import com.acabra.moneytransfer.model.TransferRequest;
import org.junit.Assert;
import org.junit.Test;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransferDAOH2ImplTest {

    @Test
    public void should_return_empty_list() {
        TransferDAOH2Impl transferDAOH2 = new TransferDAOH2Impl(H2Sql2oHelper.ofLocalKeepOpenSql2o());
        Assert.assertTrue(transferDAOH2.retrieveTransfersByAccountId(29L).isEmpty());
    }

    @Test
    public void should_return_persist_transaction() {
        Sql2o sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        TransferDAOH2Impl transferDAOH2 = new TransferDAOH2Impl(sql2o);
        BigDecimal amount = BigDecimal.TEN;
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        long sourceAccountId = accountDAO.createAccount(amount).getId();
        long destinationAccountId = accountDAO.createAccount(BigDecimal.ZERO).getId();

        transferDAOH2.storeTransfer(new TransferRequest(sourceAccountId, destinationAccountId, amount, LocalDateTime.now()));

        List<TransferDTO> transferDTOS = transferDAOH2.retrieveTransfersByAccountId(sourceAccountId);
        Assert.assertEquals(1, transferDTOS.size());

        TransferDTO transferDTO = transferDTOS.get(0);
        Assert.assertEquals(sourceAccountId, transferDTO.sourceAccountId);
        Assert.assertEquals(destinationAccountId, transferDTO.destinationAccountId);
        Assert.assertEquals(0, amount.compareTo(transferDTO.amount));

        TransferDTO searchByDestinationAccountIdTransfer = transferDAOH2.retrieveTransfersByAccountId(destinationAccountId).get(0);
        Assert.assertEquals(searchByDestinationAccountIdTransfer, transferDTO);
    }
}
