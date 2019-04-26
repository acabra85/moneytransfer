package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.model.TransferRequest;

public interface TransferService {

    boolean transfer(AccountsTransferLock transferLock, TransferRequest transferRequest);
}
