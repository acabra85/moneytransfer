package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Currency;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDao;

    public AccountServiceImpl(AccountDAO accountDAO) {
        this.accountDao = accountDAO;
    }

    @Override
    public AccountDTO createAccount(CreateAccountRequestDTO createAccountRequestDTO) {
        BigDecimal initialBalance = createAccountRequestDTO == null ? BigDecimal.ZERO : createAccountRequestDTO.getInitialBalance();
        Currency currency = Currency.getCurrencyFromCode(createAccountRequestDTO.getCurrencyCode());
        Account account = accountDao.createAccount(initialBalance, currency);
        return AccountDTO.fromAccount(account);
    }

    @Override
    public AccountDTO retrieveAccountById(Long accountId) {
        if (null == accountId) return null;
        Account account = accountDao.retrieveAccountById(accountId);
        if (null == account) return null;
        return AccountDTO.fromAccount(account);
    }

    @Override
    public List<AccountDTO> retrieveAccounts() {
        return accountDao.retrieveAllAccounts().stream()
                .map(AccountDTO::fromAccount)
                .collect(Collectors.toList());
    }
}
