package com.acabra.moneytransfer.dto;

import com.acabra.moneytransfer.model.Account;
import java.math.BigDecimal;

public class AccountDTO {

    public final long id;
    public final BigDecimal balance;

    public AccountDTO(long id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(account.getId(), account.getBalance());
    }
}
