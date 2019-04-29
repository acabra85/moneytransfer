package com.acabra.moneytransfer.dto;

import com.acabra.moneytransfer.model.Account;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO {

    public final long id;
    public final BigDecimal balance;

    @JsonCreator
    public AccountDTO(@JsonProperty("id") long id,
                      @JsonProperty("balance") BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(account.getId(), account.getBalance());
    }
}
