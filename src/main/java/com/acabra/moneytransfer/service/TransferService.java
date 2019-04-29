package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import java.util.List;

public interface TransferService {

    Transfer transfer(TransferRequest transferRequest);

    List<Transfer> retrieveAllTransfers();

    List<Transfer> retrieveAllTransfersByAccountId(Long accountId);
}
