package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.model.TransferRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferServiceImpl implements TransferService {

    private final TransferDAO transfers;
    private final AccountDAO accountDAO;

    Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    public TransferServiceImpl(TransferDAO transfers, AccountDAO accountDAO) {
        this.transfers = transfers;
        this.accountDAO = accountDAO;
    }

    @Override
    public boolean transfer(TransferRequest transferRequest) {
        AccountsTransferLock transferLock = accountDAO.lockAccountsForTransfer(transferRequest);
        try {
            transfers.storeTransfer(transferRequest);
            transferLock.tx.commit();
            return true;
        } catch (Exception e) {
            transferLock.tx.rollback();
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            transferLock.close();
        }
    }
}
