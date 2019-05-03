package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.dto.AccountDTO;
import com.acabra.moneytransfer.dto.CreateAccountRequestDTO;
import java.util.List;

public interface AccountService {

    AccountDTO createAccount(CreateAccountRequestDTO createAccountRequestDTO);

    AccountDTO retrieveAccountById(Long accountId);

    List<AccountDTO> retrieveAccounts();
}
