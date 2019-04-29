package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dto.AccountDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDao;

    public AccountServiceImpl(AccountDAO accountDAO) {
        this.accountDao = accountDAO;
    }

    @Override
    public AccountDTO createAccount(BigDecimal amount) {
        return AccountDTO.fromAccount(accountDao.createAccount(amount == null ? BigDecimal.ZERO : amount));
    }

    @Override
    public AccountDTO retrieveAccountById(Long accountId) {
        if (null == accountId) return null;
        return AccountDTO.fromAccount(accountDao.retrieveAccountById(accountId));
    }

    @Override
    public List<AccountDTO> retrieveAccounts() {
        return accountDao.retrieveAllAccounts().stream()
                .map(AccountDTO::fromAccount)
                .collect(Collectors.toList());
    }
}
