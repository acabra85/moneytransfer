package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.model.Transfer;
import com.acabra.moneytransfer.request.TransferRequest;
import java.util.List;
import org.sql2o.Connection;

public interface TransferDAO {

    Transfer storeTransferAndCommitTransactional(TransferRequest transferRequest, Connection tx);

    List<Transfer> retrieveTransfersByAccountId(long accountId);

    List<Transfer> retrieveAllTransfers();
}
