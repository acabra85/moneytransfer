package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.model.Account;
import java.math.BigDecimal;
import java.util.List;

public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDao;

    public AccountServiceImpl(AccountDAO accountDAO) {
        this.accountDao = accountDAO;
    }

    @Override
    public Account createAccount(BigDecimal amount) {
        return accountDao.createAccount(amount == null ? BigDecimal.ZERO : amount);
    }

    @Override
    public Account retrieveAccountById(Long accountId) {
        if (null == accountId) return null;
        return accountDao.retrieveAccountById(accountId);
    }

    @Override
    public List<Account> retrieveAccounts() {
        return accountDao.retrieveAllAccounts();
    }
}
