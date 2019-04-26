package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.dto.TransferDTO;
import com.acabra.moneytransfer.model.TransferRequest;

import java.util.List;

public interface TransferDAO {

    void storeTransfer(TransferRequest request);

    List<TransferDTO> retrieveTransfersByAccountId(long accountId);
}
