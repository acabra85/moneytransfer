package com.acabra.moneytransfer.dao;

import com.acabra.moneytransfer.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDAO {

    Account retrieveAccountById(long id);

    List<Account> getAccounts();

    List<Account> retrieveAccountsByIds(List<Long> ids);

    Account createAccount(BigDecimal initialBalance);
}
