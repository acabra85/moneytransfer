package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.model.Account;

import com.acabra.moneytransfer.model.Currency;
import java.math.BigDecimal;
import java.util.List;
import org.sql2o.Connection;

public interface AccountDAO {

    Account retrieveAccountById(long id);

    List<Account> retrieveAllAccounts();

    List<Account> retrieveAccountsByIds(List<Long> ids);

    Account createAccount(BigDecimal initialBalance, Currency currency);

    AccountsTransferLock lockAccountsForTransfer(long sourceAccountId, long destinationAccountId);

    void updateAccountBalanceTransactional(Account account, Connection tx);

    void updateAccountBalance(Account account);
}
