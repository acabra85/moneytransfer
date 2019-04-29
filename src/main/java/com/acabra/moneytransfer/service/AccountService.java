package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.model.Account;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    Account createAccount(BigDecimal amount);

    Account retrieveAccountById(Long accountId);

    List<Account> retrieveAccounts();
}
