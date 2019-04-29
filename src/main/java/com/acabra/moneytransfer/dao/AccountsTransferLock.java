package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.model.Account;
import org.sql2o.Connection;

public class AccountsTransferLock {

    private final Connection tx;
    private final Account source;
    private final Account destination;

    public AccountsTransferLock(Connection tx, Account source, Account destination) {
        this.tx = tx;
        this.source = source;
        this.destination = destination;
    }

    public Account getSourceAccount() {
        return source;
    }

    public Account getDestinationAccount() {
        return destination;
    }

    public void close() {
        tx.close();
    }

    public void rollback() {
        tx.rollback();
    }

    public Connection getTx() {
        return tx;
    }
}
