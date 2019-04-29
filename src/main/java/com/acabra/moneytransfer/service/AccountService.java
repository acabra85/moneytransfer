package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dto.AccountDTO;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    AccountDTO createAccount(BigDecimal amount);

    AccountDTO retrieveAccountById(Long accountId);

    List<AccountDTO> retrieveAccounts();
}
