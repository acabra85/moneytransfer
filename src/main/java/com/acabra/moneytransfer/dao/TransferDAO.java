package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.model.TransferRequest;

public interface TransferDAO {

    void storeTransfer(TransferRequest request);
}
