package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.model.TransferRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferServiceImpl implements TransferService {

    private final TransferDAO transfers;

    Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    public TransferServiceImpl(TransferDAO transfers) {
        this.transfers = transfers;
    }

    @Override
    public boolean transfer(AccountsTransferLock transferLock, TransferRequest transferRequest) {
        try {
            transfers.storeTransfer(transferRequest);
            transferLock.tx.commit();
            return true;
        } catch (Exception e) {
            transferLock.tx.rollback();
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
