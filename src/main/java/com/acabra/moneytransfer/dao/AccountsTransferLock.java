package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.model.Account;
import org.sql2o.Connection;

public class AccountsTransferLock implements AutoCloseable {

    public final Connection tx;
    public final Account source;
    public final Account destination;

    public AccountsTransferLock(Connection tx, Account source, Account destination) {
        this.tx = tx;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void close() {
        tx.close();
    }
}
