package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.Currency;
import java.util.List;
import java.util.stream.Collectors;

public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDao;

    public AccountServiceImpl(AccountDAO accountDAO) {
        this.accountDao = accountDAO;
    }

    @Override
    public AccountDTO createAccount(CreateAccountRequestDTO createAccountRequestDTO) {
        validateCreateAccountRequest(createAccountRequestDTO);
        Currency currency = Currency.getCurrencyFromCode(createAccountRequestDTO.getCurrencyCode());
        Account account = accountDao.createAccount(createAccountRequestDTO.getInitialBalance(), currency);
        return AccountDTO.fromAccount(account);
    }

    private void validateCreateAccountRequest(CreateAccountRequestDTO createAccountRequestDTO) {
        if (null == createAccountRequestDTO) {
            throw new NullPointerException("Invalid create account request: null");
        }
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
